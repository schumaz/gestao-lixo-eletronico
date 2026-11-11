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
        // Verifica se o e-mail já existe para evitar duplicidade
        // (Requer adicionar o método boolean existsByEmail(String email) no EmpresaRepository)
        
        Empresa novaEmpresa = EmpresaMapper.toEntity(request);
        Empresa empresaSalva = repository.save(novaEmpresa);
        
        return EmpresaMapper.toResponse(empresaSalva);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscarPerfilPorId(Long id) {
        Empresa empresa = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada com o ID: " + id));
                
        return EmpresaMapper.toResponse(empresa);
    }
}