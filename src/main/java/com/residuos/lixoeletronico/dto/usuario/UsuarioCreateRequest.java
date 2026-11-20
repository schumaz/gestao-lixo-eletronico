package com.residuos.lixoeletronico.dto.usuario;

import com.residuos.lixoeletronico.dto.empresa.EnderecoDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String nome,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String senha,

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    String telefone,

    @Size(max = 500, message = "URL da foto deve ter no máximo 500 caracteres")
    String urlFoto,

    @Valid
    EnderecoDTO endereco
) {}