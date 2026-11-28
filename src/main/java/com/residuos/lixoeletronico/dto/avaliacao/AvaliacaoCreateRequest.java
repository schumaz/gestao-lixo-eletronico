package com.residuos.lixoeletronico.dto.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoCreateRequest(
    @NotBlank(message = "Nome do usuário é obrigatório")
    @Size(max = 150, message = "Nome do usuário deve ter no máximo 150 caracteres")
    String nomeUsuario,

    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "A nota mínima é 1")
    @Max(value = 5, message = "A nota máxima é 5")
    Integer nota,

    @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
    String comentario
) {}
