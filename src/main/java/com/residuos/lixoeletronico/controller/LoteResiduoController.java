package com.residuos.lixoeletronico.controller;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoteResiduoController {

    private final LoteResiduoService service;

    public LoteResiduoController(LoteResiduoService service) {
        this.service = service;
    }

    @GetMapping("/lotes/estoque")
    public ResponseEntity<List<LoteResiduoResponse>> consultarLotesEmEstoque(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) CategoriaMaterial categoriaMaterial,
            @RequestParam(required = false) UrgenciaDescarte urgenciaDescarte
    ) {
        return ResponseEntity.ok(service.listarLotesEmEstoque(empresaId, categoriaMaterial, urgenciaDescarte));
    }

    @GetMapping("/empresas/{empresaId}/lotes/estoque")
    public ResponseEntity<List<LoteResiduoResponse>> consultarLotesEmEstoquePorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(required = false) CategoriaMaterial categoriaMaterial,
            @RequestParam(required = false) UrgenciaDescarte urgenciaDescarte
    ) {
        return ResponseEntity.ok(service.listarLotesEmEstoque(empresaId, categoriaMaterial, urgenciaDescarte));
    }
}
