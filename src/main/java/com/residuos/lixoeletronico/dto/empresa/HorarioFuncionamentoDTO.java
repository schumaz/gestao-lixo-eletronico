package com.residuos.lixoeletronico.dto.empresa;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioFuncionamentoDTO(
    @NotNull(message = "O dia da semana é obrigatório")
    DayOfWeek diaSemana,

    @NotNull(message = "O horário de abertura é obrigatório")
    LocalTime abertura,

    @NotNull(message = "O horário de fechamento é obrigatório")
    LocalTime fechamento
) {}
