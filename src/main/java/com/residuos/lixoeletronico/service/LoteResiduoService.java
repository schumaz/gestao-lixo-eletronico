package com.residuos.lixoeletronico.service;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoMapper;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.LoteResiduo;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.LoteResiduoRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoteResiduoService {

    private final LoteResiduoRepository loteResiduoRepository;
    private final EmpresaRepository empresaRepository;

    public LoteResiduoService(LoteResiduoRepository loteResiduoRepository, EmpresaRepository empresaRepository) {
        this.loteResiduoRepository = loteResiduoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<LoteResiduoResponse> listarLotesEmEstoque(
            Long empresaId,
            CategoriaMaterial categoriaMaterial,
            UrgenciaDescarte urgenciaDescarte
    ) {
        if (empresaId != null && !empresaRepository.existsById(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada com o ID: " + empresaId);
        }

        Specification<LoteResiduo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), StatusLote.EM_ESTOQUE));

            if (empresaId != null) {
                predicates.add(cb.equal(root.get("empresa").get("id"), empresaId));
            }
            if (categoriaMaterial != null) {
                predicates.add(cb.equal(root.get("categoriaMaterial"), categoriaMaterial));
            }
            if (urgenciaDescarte != null) {
                predicates.add(cb.equal(root.get("urgenciaDescarte"), urgenciaDescarte));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Order.desc("dataRecepcao"), Sort.Order.desc("id"));

        return loteResiduoRepository.findAll(spec, sort)
                .stream()
                .map(LoteResiduoMapper::toResponse)
                .toList();
    }
}
