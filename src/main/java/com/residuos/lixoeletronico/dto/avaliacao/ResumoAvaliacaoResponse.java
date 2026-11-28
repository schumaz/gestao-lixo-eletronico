package com.residuos.lixoeletronico.dto.avaliacao;

import java.util.List;

public record ResumoAvaliacaoResponse(
    Long empresaId,
    Double mediaNota,
    long totalAvaliacoes,
    List<AvaliacaoResponse> avaliacoes
) {}
