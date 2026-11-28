package com.residuos.lixoeletronico.controller;

import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoCreateRequest;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoResponse;
import com.residuos.lixoeletronico.dto.avaliacao.ResumoAvaliacaoResponse;
import com.residuos.lixoeletronico.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresas/{empresaId}/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponse> registrarAvaliacao(
            @PathVariable Long empresaId,
            @RequestBody @Valid AvaliacaoCreateRequest request
    ) {
        AvaliacaoResponse response = service.registrarAvaliacao(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ResumoAvaliacaoResponse> consultarAvaliacoes(@PathVariable Long empresaId) {
        return ResponseEntity.ok(service.obterResumoAvaliacoes(empresaId));
    }
}
