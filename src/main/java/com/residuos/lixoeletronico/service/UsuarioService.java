package com.residuos.lixoeletronico.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.dto.usuario.UsuarioCreateRequest;
import com.residuos.lixoeletronico.dto.usuario.UsuarioMapper;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioUpdateRequest;
import com.residuos.lixoeletronico.model.Usuario;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    @Autowired
    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
    }

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this(usuarioRepository, null);
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioCreateRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase(Locale.ROOT);
        String cpfNormalizado = request.cpf() != null ? request.cpf().replaceAll("\\D", "") : null;

        boolean emailEmEmpresa = empresaRepository != null
                && empresaRepository.existsByEmailIgnoreCase(emailNormalizado);

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado) || emailEmEmpresa) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este e-mail já está cadastrado no sistema."
            );
        }

        if (cpfNormalizado != null && !cpfNormalizado.isBlank()
                && usuarioRepository.existsByCpf(cpfNormalizado)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este CPF já está cadastrado no sistema."
            );
        }

        Usuario usuario = UsuarioMapper.toEntity(request);
        usuario.setEmail(emailNormalizado);
        usuario.setCpf(cpfNormalizado);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuarioSalvo);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarPorId(id);
        UsuarioMapper.updateEntityFromRequest(usuario, request);

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuarioAtualizado);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "E-mail e senha são obrigatórios."
            );
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos."
            ));

        if (!usuario.getSenha().equals(senha)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos."
            );
        }

        return UsuarioMapper.toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuário não encontrado com o ID: " + id
            ));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarResponsePorId(Long id) {
        return UsuarioMapper.toResponse(buscarPorId(id));
    }
}