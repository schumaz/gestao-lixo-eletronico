package com.residuos.lixoeletronico.dto.empresa;

import jakarta.validation.constraints.Size;

public class ContatoDTO {
    @Size(max = 20)
    private String telefone;

    @Size(max = 20)
    private String whatsapp;

    @Size(max = 255)
    private String site;

    @Size(max = 100)
    private String instagram;

    public ContatoDTO() {
    }

    public ContatoDTO(String telefone, String whatsapp, String site, String instagram) {
        this.telefone = telefone;
        this.whatsapp = whatsapp;
        this.site = site;
        this.instagram = instagram;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public String getSite() {
        return site;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }
}