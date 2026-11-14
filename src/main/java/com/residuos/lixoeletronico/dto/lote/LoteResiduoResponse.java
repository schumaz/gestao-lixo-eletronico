package com.residuos.lixoeletronico.dto.lote;

import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record LoteResiduoResponse(
    Long id,
    Long empresaId,
    String nomeUsuario,
    String contatoUsuario,
    LocalDate dataRecepcao,
    CategoriaMaterial categoriaMaterial,
    String tipoResiduo,
    BigDecimal pesoKg,
    UrgenciaDescarte urgenciaDescarte,
    StatusLote status,
    Instant criadoEm,
    Instant atualizadoEm
) {}
