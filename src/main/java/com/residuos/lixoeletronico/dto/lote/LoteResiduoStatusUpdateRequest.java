package com.residuos.lixoeletronico.dto.lote;

import com.residuos.lixoeletronico.model.enums.StatusLote;
import jakarta.validation.constraints.NotNull;

public record LoteResiduoStatusUpdateRequest(
    @NotNull(message = "Novo status do lote é obrigatório")
    StatusLote status
) {}
