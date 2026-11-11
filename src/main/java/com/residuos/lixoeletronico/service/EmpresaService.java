package com.residuos.lixoeletronico.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.dto.empresa.EmpresaCreateRequest;
import com.residuos.lixoeletronico.dto.empresa.EmpresaMapper;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.repository.EmpresaRepository;

@Service
public class EmpresaService {

    private final EmpresaRepository repository;

    public EmpresaService(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmpresaResponse criarEmpresa(EmpresaCreateRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado: " + request.email());
        }

        Empresa novaEmpresa = EmpresaMapper.toEntity(request);
        Empresa empresaSalva = repository.save(novaEmpresa);

        return EmpresaMapper.toResponse(empresaSalva);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscarPerfilPorId(Long id) {
        Empresa empresa = repository.findPerfilById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada com o ID: " + id));

        return EmpresaMapper.toResponse(empresa);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse autenticar(String email, String senha) {
        String emailFormatado = email != null ? email.trim() : "";
        String emailBusca = "admin@admin".equalsIgnoreCase(emailFormatado) ? "admin@admin.com" : emailFormatado;

        Empresa empresa = repository.findPerfilById(
                repository.findByEmail(emailBusca)
                        .or(() -> repository.findByEmail(emailFormatado))
                        .map(Empresa::getId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."))
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));

        if (!empresa.getSenha().equals(senha)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }

        return EmpresaMapper.toResponse(empresa);
    }
}
