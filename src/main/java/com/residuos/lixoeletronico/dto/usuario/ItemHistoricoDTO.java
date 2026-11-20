package com.residuos.lixoeletronico.dto.usuario;

import java.time.LocalDate;

public record ItemHistoricoDTO(
    String material,
    double pesoKg,
    String nomeEmpresaDestino,
    LocalDate dataDescarte
) {}