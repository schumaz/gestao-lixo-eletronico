package com.residuos.lixoeletronico.dto.empresa;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaCreateRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String nome,

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}", message = "CNPJ inválido")
    String cnpj,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String senha,

    @Valid
    ContatoDTO contato,

    @Valid
    EnderecoDTO endereco
) {
    public EmpresaCreateRequest(
            String nome,
            String email,
            String senha,
            ContatoDTO contato,
            EnderecoDTO endereco
    ) {
        this(nome, null, email, senha, contato, endereco);
    }
}