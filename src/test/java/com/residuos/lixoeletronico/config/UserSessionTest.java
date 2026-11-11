package com.residuos.lixoeletronico.config;

import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    private UserSession session;

    @BeforeEach
    void setUp() {
        session = new UserSession();
    }

    @Test
    @DisplayName("Deve iniciar sem usuário autenticado")
    void deveIniciarSemUsuario() {
        assertNull(session.getEmpresaLogada());
        assertFalse(session.isAutenticado());
    }

    @Test
    @DisplayName("Deve armazenar e recuperar empresa autenticada")
    void deveArmazenarERecuperarEmpresa() {
        EmpresaResponse empresa = new EmpresaResponse(
                1L, "Empresa Teste", "teste@email.com",
                null, null, null, null, null,
                List.of(), Set.of(), BigDecimal.ZERO, Instant.now(), null
        );

        session.setEmpresaLogada(empresa);

        assertTrue(session.isAutenticado());
        assertEquals(empresa, session.getEmpresaLogada());
        assertEquals("Empresa Teste", session.getEmpresaLogada().nome());
    }

    @Test
    @DisplayName("Deve limpar sessão ao fazer logout")
    void deveLimparSessaoNoLogout() {
        EmpresaResponse empresa = new EmpresaResponse(
                1L, "Empresa Teste", "teste@email.com",
                null, null, null, null, null,
                List.of(), Set.of(), BigDecimal.ZERO, Instant.now(), null
        );
        session.setEmpresaLogada(empresa);
        assertTrue(session.isAutenticado());

        session.logout();

        assertNull(session.getEmpresaLogada());
        assertFalse(session.isAutenticado());
    }
}
