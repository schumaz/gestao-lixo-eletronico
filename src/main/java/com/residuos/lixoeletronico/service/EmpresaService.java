package com.residuos.lixoeletronico.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.dto.empresa.EmpresaCreateRequest;
import com.residuos.lixoeletronico.dto.empresa.EmpresaMapper;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.UsuarioRepository;

@Service
public class EmpresaService {

    private final EmpresaRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public EmpresaService(
            EmpresaRepository repository,
            UsuarioRepository usuarioRepository
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public EmpresaService(EmpresaRepository repository) {
        this(repository, null);
    }

    @Transactional
    public EmpresaResponse criarEmpresa(EmpresaCreateRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase(Locale.ROOT);
        String cnpjNormalizado = request.cnpj() != null ? request.cnpj().replaceAll("\\D", "") : null;

        boolean emailEmUsuario = usuarioRepository != null
                && usuarioRepository.existsByEmailIgnoreCase(emailNormalizado);

        if (repository.existsByEmailIgnoreCase(emailNormalizado) || emailEmUsuario) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este e-mail já está cadastrado no sistema."
            );
        }

        if (cnpjNormalizado != null && !cnpjNormalizado.isBlank()
                && repository.existsByCnpj(cnpjNormalizado)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este CNPJ já está cadastrado no sistema."
            );
        }

        Empresa novaEmpresa = EmpresaMapper.toEntity(request);
        novaEmpresa.setEmail(emailNormalizado);
        novaEmpresa.setCnpj(cnpjNormalizado);

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