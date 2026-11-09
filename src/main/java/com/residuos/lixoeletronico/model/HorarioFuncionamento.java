package com.residuos.lixoeletronico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "diaSemana")
public class HorarioFuncionamento {

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", length = 10, nullable = false)
    private DayOfWeek diaSemana;

    @Column(nullable = false)
    private LocalTime abertura;

    @Column(nullable = false)
    private LocalTime fechamento;
}
