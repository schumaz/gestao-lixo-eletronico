package com.residuos.lixoeletronico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contato {

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 255)
    private String site;

    @Column(length = 100)
    private String instagram;
}
