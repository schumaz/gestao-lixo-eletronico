package com.residuos.lixoeletronico.dto.usuario;

import java.util.List;

import com.residuos.lixoeletronico.dto.empresa.EnderecoDTO;

public record UsuarioPerfilResponse(
    Long id,
    String nome,
    String cpf,
    String email,
    String telefone,
    String urlFoto,
    EnderecoDTO endereco,
    double totalKgDescartado,
    String materialMaisDescartado,
    String ultimoDeposito,
    List<ItemHistoricoDTO> historico
) {}