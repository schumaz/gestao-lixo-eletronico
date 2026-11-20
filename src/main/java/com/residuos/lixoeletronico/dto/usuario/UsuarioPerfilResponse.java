package com.residuos.lixoeletronico.dto.usuario;

import java.util.List;

import com.residuos.lixoeletronico.dto.empresa.EnderecoDTO;

public record UsuarioPerfilResponse(
    Long id,
    String nome,
    String email,
    String telefone,
    String urlFoto,
    EnderecoDTO endereco,
    String ultimoDeposito,
    String materialMaisDescartado,
    double totalKgDescartado,
    List<ItemHistoricoDTO> historico
) {}