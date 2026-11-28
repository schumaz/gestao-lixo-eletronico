package com.residuos.lixoeletronico.dto.avaliacao;

import java.time.Instant;

public record AvaliacaoResponse(
    Long id,
    Long empresaId,
    String nomeUsuario,
    Integer nota,
    String comentario,
    Instant criadoEm
) {}
