package com.residuos.lixoeletronico.config;

import org.springframework.stereotype.Component;

import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;

@Component
public class UserSession {

    private EmpresaResponse empresaLogada;
    private UsuarioResponse usuarioLogado;

    public EmpresaResponse getEmpresaLogada() {
        return empresaLogada;
    }

    public void setEmpresaLogada(EmpresaResponse empresa) {
        this.empresaLogada = empresa;
    }

    public UsuarioResponse getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(UsuarioResponse usuario) {
        this.usuarioLogado = usuario;
    }

    public boolean isAutenticado() {
        return empresaLogada != null || usuarioLogado != null;
    }

    public void limparSessao() {
        this.empresaLogada = null;
        this.usuarioLogado = null;
    }

    public void logout() {
        limparSessao();
    }
}
