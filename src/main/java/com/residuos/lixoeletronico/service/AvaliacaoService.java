package com.residuos.lixoeletronico.service;

import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoCreateRequest;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoMapper;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoResponse;
import com.residuos.lixoeletronico.dto.avaliacao.ResumoAvaliacaoResponse;
import com.residuos.lixoeletronico.model.Avaliacao;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.repository.AvaliacaoRepository;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final EmpresaRepository empresaRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, EmpresaRepository empresaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public AvaliacaoResponse registrarAvaliacao(Long empresaId, AvaliacaoCreateRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada com o ID: " + empresaId));

        Avaliacao avaliacao = AvaliacaoMapper.toEntity(request, empresa);
        Avaliacao salva = avaliacaoRepository.save(avaliacao);

        return AvaliacaoMapper.toResponse(salva);
    }

    @Transactional(readOnly = true)
    public ResumoAvaliacaoResponse obterResumoAvaliacoes(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada com o ID: " + empresaId);
        }

        long total = avaliacaoRepository.countByEmpresaId(empresaId);
        double mediaArredondada = 0.0;
        if (total > 0) {
            Double mediaBruta = avaliacaoRepository.calcularMediaNotasPorEmpresa(empresaId);
            if (mediaBruta != null) {
                mediaArredondada = BigDecimal.valueOf(mediaBruta)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }
        }

        List<AvaliacaoResponse> avaliacoes = avaliacaoRepository.findByEmpresaIdOrderByCriadoEmDescIdDesc(empresaId)
                .stream()
                .map(AvaliacaoMapper::toResponse)
                .toList();

        return new ResumoAvaliacaoResponse(empresaId, mediaArredondada, total, avaliacoes);
    }
}
