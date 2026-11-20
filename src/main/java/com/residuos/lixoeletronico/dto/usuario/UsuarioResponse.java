package com.residuos.lixoeletronico.dto.usuario;

import com.residuos.lixoeletronico.dto.empresa.EnderecoDTO;

public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    String telefone,
    String urlFoto,
    EnderecoDTO endereco
) {}