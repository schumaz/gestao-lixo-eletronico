package com.residuos.lixoeletronico.dto.lote;

import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteResiduoCreateRequest(
    @NotBlank(message = "Nome do usuário é obrigatório")
    @Size(max = 150, message = "Nome do usuário deve ter no máximo 150 caracteres")
    String nomeUsuario,

    @NotBlank(message = "Contato do usuário é obrigatório")
    @Size(max = 100, message = "Contato do usuário deve ter no máximo 100 caracteres")
    String contatoUsuario,

    @NotNull(message = "Data de recepção é obrigatória")
    @PastOrPresent(message = "Data de recepção não pode ser futura")
    LocalDate dataRecepcao,

    @NotNull(message = "Material é obrigatório")
    CategoriaMaterial categoriaMaterial,

    @NotBlank(message = "Tipo do resíduo é obrigatório")
    @Size(max = 150, message = "Tipo do resíduo deve ter no máximo 150 caracteres")
    String tipoResiduo,

    @NotNull(message = "Peso em kg é obrigatório")
    @DecimalMin(value = "0.001", message = "Peso deve ser maior que zero")
    @Digits(integer = 9, fraction = 3, message = "Peso deve ter no máximo 9 dígitos inteiros e 3 decimais")
    BigDecimal pesoKg,

    @NotNull(message = "Urgência de descarte é obrigatória")
    UrgenciaDescarte urgenciaDescarte
) {}
