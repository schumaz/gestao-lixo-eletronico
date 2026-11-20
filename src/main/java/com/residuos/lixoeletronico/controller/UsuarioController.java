package com.residuos.lixoeletronico.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.residuos.lixoeletronico.dto.usuario.ItemHistoricoDTO;
import com.residuos.lixoeletronico.dto.usuario.UsuarioCreateRequest;
import com.residuos.lixoeletronico.dto.usuario.UsuarioMapper;
import com.residuos.lixoeletronico.dto.usuario.UsuarioPerfilResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioUpdateRequest;
import com.residuos.lixoeletronico.model.Usuario;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import com.residuos.lixoeletronico.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UsuarioService usuarioService;
    private final LoteResiduoService loteResiduoService;

    public UsuarioController(
            UsuarioService usuarioService,
            LoteResiduoService loteResiduoService
    ) {
        this.usuarioService = usuarioService;
        this.loteResiduoService = loteResiduoService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrarUsuario(@RequestBody @Valid UsuarioCreateRequest request) {
        UsuarioResponse response = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioUpdateRequest request
    ) {
        UsuarioResponse response = usuarioService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<UsuarioPerfilResponse> consultarPerfil(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        List<ItemHistoricoDTO> historico = loteResiduoService.listarHistoricoPorUsuario(id);

        double totalKg = loteResiduoService.calcularTotalKg(historico);
        String materialMaisDescartado = loteResiduoService.obterMaterialMaisDescartado(historico);

        String ultimoDeposito = null;
        if (!historico.isEmpty()) {
            ItemHistoricoDTO ultimo = historico.get(0);
            String data = ultimo.dataDescarte() != null ? ultimo.dataDescarte().format(DATE_FORMATTER) : "";
            ultimoDeposito = data + " (" + ultimo.nomeEmpresaDestino() + ")";
        }

        UsuarioPerfilResponse response = UsuarioMapper.toPerfilResponse(
                usuario,
                ultimoDeposito,
                materialMaisDescartado,
                totalKg,
                historico
        );

        return ResponseEntity.ok(response);
    }
}