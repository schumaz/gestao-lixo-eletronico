package com.residuos.lixoeletronico.config;

import org.springframework.stereotype.Component;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;

@Component
public class UserSession {

    private EmpresaResponse empresaLogada;

    public void setEmpresaLogada(EmpresaResponse empresa) {
        this.empresaLogada = empresa;
    }

    public EmpresaResponse getEmpresaLogada() {
        return empresaLogada;
    }

    public boolean isAutenticado() {
        return empresaLogada != null;
    }

    public void logout() {
        this.empresaLogada = null;
    }
}
