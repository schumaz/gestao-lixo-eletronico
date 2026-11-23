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
        if (empresaId != null && !empresaRepository.existsById(empresaId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Empresa não encontrada com o ID: " + empresaId
            );
        }

        Specification<LoteResiduo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), StatusLote.EM_ESTOQUE));

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
