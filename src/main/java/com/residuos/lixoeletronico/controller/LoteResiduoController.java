package com.residuos.lixoeletronico.controller;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoStatusUpdateRequest;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoteResiduoController {

    private final LoteResiduoService service;

    public LoteResiduoController(LoteResiduoService service) {
        this.service = service;
    }

    @PostMapping("/empresas/{empresaId}/lotes")
    public ResponseEntity<LoteResiduoResponse> registrarRecepcao(
        @PathVariable Long empresaId,
        @RequestBody @Valid LoteResiduoCreateRequest request
    ) {
        LoteResiduoResponse response = service.registrarRecepcao(
            empresaId,
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/lotes/estoque")
    public ResponseEntity<List<LoteResiduoResponse>> consultarLotesEmEstoque(
        @RequestParam(required = false) Long empresaId,
        @RequestParam(required = false) CategoriaMaterial categoriaMaterial,
        @RequestParam(required = false) UrgenciaDescarte urgenciaDescarte
    ) {
        return ResponseEntity.ok(
            service.listarLotesEmEstoque(
                empresaId,
                categoriaMaterial,
                urgenciaDescarte
            )
        );
    }

    @GetMapping("/empresas/{empresaId}/lotes/estoque")
    public ResponseEntity<
        List<LoteResiduoResponse>
    > consultarLotesEmEstoquePorEmpresa(
        @PathVariable Long empresaId,
        @RequestParam(required = false) CategoriaMaterial categoriaMaterial,
        @RequestParam(required = false) UrgenciaDescarte urgenciaDescarte
    ) {
        return ResponseEntity.ok(
            service.listarLotesEmEstoque(
                empresaId,
                categoriaMaterial,
                urgenciaDescarte
            )
        );
    }

    @GetMapping("/lotes")
    public ResponseEntity<List<LoteResiduoResponse>> consultarLotes(
        @RequestParam(required = false) Long empresaId,
        @RequestParam(required = false) StatusLote status,
        @RequestParam(required = false) CategoriaMaterial categoriaMaterial,
        @RequestParam(required = false) UrgenciaDescarte urgenciaDescarte
    ) {
        return ResponseEntity.ok(
            service.listarLotes(
                empresaId,
                status,
                categoriaMaterial,
                urgenciaDescarte
            )
        );
    }

    @GetMapping("/empresas/{empresaId}/lotes")
    public ResponseEntity<List<LoteResiduoResponse>> consultarLotesPorEmpresa(
        @PathVariable Long empresaId,
        @RequestParam(required = false) StatusLote status,
        @RequestParam(required = false) CategoriaMaterial categoriaMaterial,
        @RequestParam(required = false) UrgenciaDescarte urgenciaDescarte
    ) {
        return ResponseEntity.ok(
            service.listarLotes(
                empresaId,
                status,
                categoriaMaterial,
                urgenciaDescarte
            )
        );
    }

    @PatchMapping("/lotes/{loteId}/status")
    public ResponseEntity<LoteResiduoResponse> atualizarStatus(
        @PathVariable Long loteId,
        @RequestBody @Valid LoteResiduoStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
            service.atualizarStatus(loteId, request.status())
        );
    }

    @PatchMapping("/empresas/{empresaId}/lotes/{loteId}/status")
    public ResponseEntity<LoteResiduoResponse> atualizarStatusPorEmpresa(
        @PathVariable Long empresaId,
        @PathVariable Long loteId,
        @RequestBody @Valid LoteResiduoStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
            service.atualizarStatus(empresaId, loteId, request.status())
        );
    }
}
