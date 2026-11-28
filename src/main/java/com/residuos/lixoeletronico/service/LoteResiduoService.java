package com.residuos.lixoeletronico.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoMapper;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.dto.usuario.ItemHistoricoDTO;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.LoteResiduo;
import com.residuos.lixoeletronico.model.Usuario;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.LoteResiduoRepository;
import com.residuos.lixoeletronico.repository.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class LoteResiduoService {

    private final LoteResiduoRepository loteResiduoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public LoteResiduoService(
        LoteResiduoRepository loteResiduoRepository,
        EmpresaRepository empresaRepository,
        UsuarioRepository usuarioRepository
    ) {
        this.loteResiduoRepository = loteResiduoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public LoteResiduoService(
        LoteResiduoRepository loteResiduoRepository,
        EmpresaRepository empresaRepository
    ) {
        this(loteResiduoRepository, empresaRepository, null);
    }

    @Transactional
    public LoteResiduoResponse registrarRecepcao(
        Long empresaId,
        LoteResiduoCreateRequest request
    ) {
        Empresa empresa = empresaRepository
            .findById(empresaId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa não encontrada com o ID: " + empresaId
                )
            );

        LoteResiduo lote = LoteResiduoMapper.toEntity(request, empresa);

        if (request.usuarioId() != null && usuarioRepository != null) {
            Usuario usuario = usuarioRepository
                .findById(request.usuarioId())
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado com o ID: " + request.usuarioId()
                    )
                );
            lote.setUsuario(usuario);
        }

        LoteResiduo loteSalvo = loteResiduoRepository.save(lote);
        LoteResiduoResponse response = LoteResiduoMapper.toResponse(loteSalvo);

        empresaRepository.adicionarKgDescartado(
            empresa.getId(),
            loteSalvo.getPesoKg()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<LoteResiduoResponse> listarLotesEmEstoque(
        Long empresaId,
        CategoriaMaterial categoriaMaterial,
        UrgenciaDescarte urgenciaDescarte
    ) {
        return listarLotes(
            empresaId,
            StatusLote.EM_ESTOQUE,
            categoriaMaterial,
            urgenciaDescarte
        );
    }

    /**
     * Lista lotes aplicando filtros opcionais de empresa, status, categoria de material
     * e urgência de descarte, ordenados da recepção mais recente para a mais antiga.
     */
    @Transactional(readOnly = true)
    public List<LoteResiduoResponse> listarLotes(
        Long empresaId,
        StatusLote status,
        CategoriaMaterial categoriaMaterial,
        UrgenciaDescarte urgenciaDescarte
    ) {
        if (empresaId != null && !empresaRepository.existsById(empresaId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Empresa não encontrada com o ID: " + empresaId
            );
        }

        Specification<LoteResiduo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (empresaId != null) {
                predicates.add(
                    cb.equal(root.get("empresa").get("id"), empresaId)
                );
            }
            if (categoriaMaterial != null) {
                predicates.add(
                    cb.equal(root.get("categoriaMaterial"), categoriaMaterial)
                );
            }
            if (urgenciaDescarte != null) {
                predicates.add(
                    cb.equal(root.get("urgenciaDescarte"), urgenciaDescarte)
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Sort sort = Sort.by(
            Sort.Order.desc("dataRecepcao"),
            Sort.Order.desc("id")
        );

        return loteResiduoRepository
            .findAll(spec, sort)
            .stream()
            .map(LoteResiduoMapper::toResponse)
            .toList();
    }

    /**
     * Atualiza o status de um lote pertencente a uma empresa, validando o ciclo de vida
     * {@code EM_ESTOQUE -> EM_PROCESSAMENTO -> DESTINADO}.
     */
    @Transactional
    public LoteResiduoResponse atualizarStatus(
        Long empresaId,
        Long loteId,
        StatusLote novoStatus
    ) {
        if (loteId == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID do lote é obrigatório."
            );
        }
        if (novoStatus == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Novo status do lote é obrigatório."
            );
        }

        LoteResiduo lote = buscarLoteParaAtualizacao(empresaId, loteId);
        StatusLote statusAtual = lote.getStatus();

        if (statusAtual == null || !statusAtual.podeTransicionarPara(novoStatus)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Transição de status inválida de " +
                    statusAtual +
                    " para " +
                    novoStatus +
                    ". Fluxo permitido: EM_ESTOQUE -> EM_PROCESSAMENTO -> DESTINADO."
            );
        }

        lote.setStatus(novoStatus);
        LoteResiduo loteAtualizado = loteResiduoRepository.save(lote);
        return LoteResiduoMapper.toResponse(loteAtualizado);
    }

    /**
     * Atualiza o status de um lote pelo seu ID, validando o ciclo de vida
     * {@code EM_ESTOQUE -> EM_PROCESSAMENTO -> DESTINADO}.
     */
    @Transactional
    public LoteResiduoResponse atualizarStatus(
        Long loteId,
        StatusLote novoStatus
    ) {
        return atualizarStatus(null, loteId, novoStatus);
    }

    private LoteResiduo buscarLoteParaAtualizacao(Long empresaId, Long loteId) {
        if (empresaId != null) {
            if (!empresaRepository.existsById(empresaId)) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa não encontrada com o ID: " + empresaId
                );
            }

            return loteResiduoRepository
                .findByIdAndEmpresaId(loteId, empresaId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lote não encontrado com o ID: " +
                            loteId +
                            " para a empresa: " +
                            empresaId
                    )
                );
        }

        return loteResiduoRepository
            .findById(loteId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lote não encontrado com o ID: " + loteId
                )
            );
    }

    @Transactional(readOnly = true)
    public List<ItemHistoricoDTO> listarHistoricoPorUsuario(Long usuarioId) {
        Specification<LoteResiduo> spec = (root, query, cb) ->
            cb.equal(root.get("usuario").get("id"), usuarioId);

        Sort sort = Sort.by(
            Sort.Order.desc("dataRecepcao"),
            Sort.Order.desc("id")
        );

        return loteResiduoRepository
            .findAll(spec, sort)
            .stream()
            .map(lote ->
                new ItemHistoricoDTO(
                    lote.getCategoriaMaterial() != null
                        ? lote.getCategoriaMaterial().name()
                        : "Não especificado",
                    lote.getPesoKg() != null ? lote.getPesoKg().doubleValue() : 0.0,
                    lote.getEmpresa() != null
                        ? lote.getEmpresa().getNome()
                        : "Empresa não informada",
                    lote.getDataRecepcao()
                )
            )
            .toList();
    }

    public double calcularTotalKg(List<ItemHistoricoDTO> historico) {
        return historico.stream().mapToDouble(ItemHistoricoDTO::pesoKg).sum();
    }

    public String obterMaterialMaisDescartado(List<ItemHistoricoDTO> historico) {
        return historico
            .stream()
            .collect(
                Collectors.groupingBy(
                    ItemHistoricoDTO::material,
                    Collectors.summingDouble(ItemHistoricoDTO::pesoKg)
                )
            )
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("-");
    }
}
