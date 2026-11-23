package com.residuos.lixoeletronico.dto.usuario;

import java.util.List;

import com.residuos.lixoeletronico.dto.empresa.EmpresaMapper;
import com.residuos.lixoeletronico.model.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario toEntity(UsuarioCreateRequest request) {
        if (request == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(request.senha());
        usuario.setTelefone(request.telefone());
        usuario.setUrlFoto(request.urlFoto());

        if (request.endereco() != null) {
            usuario.setEndereco(EmpresaMapper.toEnderecoEntity(request.endereco()));
        }

        return usuario;
    }

    public static void updateEntityFromRequest(Usuario usuario, UsuarioUpdateRequest request) {
        if (usuario == null || request == null) {
            return;
        }

        usuario.setNome(request.nome());
        usuario.setTelefone(request.telefone());
        usuario.setUrlFoto(request.urlFoto());

        if (request.endereco() != null) {
            usuario.setEndereco(EmpresaMapper.toEnderecoEntity(request.endereco()));
        }
    }

    public static UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getTelefone(),
            usuario.getUrlFoto(),
            EmpresaMapper.toEnderecoDTO(usuario.getEndereco())
        );
    }

    public static UsuarioPerfilResponse toPerfilResponse(
        Usuario usuario,
        String ultimoDeposito,
        String materialMaisDescartado,
        double totalKgDescartado,
        List<ItemHistoricoDTO> historico
    ) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioPerfilResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getCpf(),
            usuario.getEmail(),
            usuario.getTelefone(),
            usuario.getUrlFoto(),
            EmpresaMapper.toEnderecoDTO(usuario.getEndereco()),
            totalKgDescartado,
            materialMaisDescartado,
            ultimoDeposito,
            historico != null ? historico : List.of()
        );
    }
}