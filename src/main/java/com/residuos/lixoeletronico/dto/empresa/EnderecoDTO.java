package com.residuos.lixoeletronico.dto.empresa;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve estar no formato 00000-000 ou 00000000")
    String cep,

    @Size(max = 255)
    String logradouro,

    @Size(max = 20)
    String numero,

    @Size(max = 100)
    String complemento,

    @Size(max = 100)
    String bairro,

    @Size(max = 100)
    String cidade,

    @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
    String uf,

    Double latitude,
    Double longitude
) {}
