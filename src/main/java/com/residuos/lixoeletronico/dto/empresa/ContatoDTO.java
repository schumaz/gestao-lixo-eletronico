package com.residuos.lixoeletronico.dto.empresa;

import jakarta.validation.constraints.Size;

public record ContatoDTO(
    @Size(max = 20)
    String telefone,

    @Size(max = 20)
    String whatsapp,

    @Size(max = 255)
    String site,

    @Size(max = 100)
    String instagram
) {}
