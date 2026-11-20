package com.residuos.lixoeletronico.dto.lote;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LoteResiduoCreateRequest(
    @NotBlank(message = "Nome do usuário é obrigatório")
    @Size(max = 150, message = "Nome do usuário deve ter no máximo 150 caracteres")
    String nomeUsuario,

    @NotBlank(message = "Contato do usuário é obrigatório")
    @Size(max = 100, message = "Contato do usuário deve ter no máximo 100 caracteres")
    String contatoUsuario,

    @NotNull(message = "Data de recepção é obrigatória")
    LocalDate dataRecepcao,

    @NotNull(message = "Categoria do material é obrigatória")
    CategoriaMaterial categoriaMaterial,

    @NotBlank(message = "Tipo de resíduo é obrigatório")
    @Size(max = 150, message = "Tipo de resíduo deve ter no máximo 150 caracteres")
    String tipoResiduo,

    @NotNull(message = "Peso em kg é obrigatório")
    @Positive(message = "Peso em kg deve ser maior que zero")
    BigDecimal pesoKg,

    @NotNull(message = "Urgência de descarte é obrigatória")
    UrgenciaDescarte urgenciaDescarte,

    Long usuarioId
) {
    public LoteResiduoCreateRequest(
        String nomeUsuario,
        String contatoUsuario,
        LocalDate dataRecepcao,
        CategoriaMaterial categoriaMaterial,
        String tipoResiduo,
        BigDecimal pesoKg,
        UrgenciaDescarte urgenciaDescarte
    ) {
        this(
            nomeUsuario,
            contatoUsuario,
            dataRecepcao,
            categoriaMaterial,
            tipoResiduo,
            pesoKg,
            urgenciaDescarte,
            null
        );
    }
}