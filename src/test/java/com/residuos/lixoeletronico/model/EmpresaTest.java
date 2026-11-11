package com.residuos.lixoeletronico.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaTest {

    @Test
    @DisplayName("Deve somar valor corretamente ao total de kg descartados")
    void deveAdicionarKgComSucesso() {
        Empresa empresa = new Empresa();
        assertEquals(BigDecimal.ZERO, empresa.getTotalKgDescartado());

        empresa.adicionarKg(new BigDecimal("150.500"));
        assertEquals(new BigDecimal("150.500"), empresa.getTotalKgDescartado());

        empresa.adicionarKg(new BigDecimal("50.250"));
        assertEquals(new BigDecimal("200.750"), empresa.getTotalKgDescartado());
    }

    @Test
    @DisplayName("Não deve alterar total de kg ao tentar adicionar valor nulo ou menor igual a zero")
    void naoDeveAdicionarKgInvalido() {
        Empresa empresa = new Empresa();
        empresa.adicionarKg(new BigDecimal("100.000"));

        empresa.adicionarKg(null);
        assertEquals(new BigDecimal("100.000"), empresa.getTotalKgDescartado());

        empresa.adicionarKg(BigDecimal.ZERO);
        assertEquals(new BigDecimal("100.000"), empresa.getTotalKgDescartado());

        empresa.adicionarKg(new BigDecimal("-25.000"));
        assertEquals(new BigDecimal("100.000"), empresa.getTotalKgDescartado());
    }

    @Test
    @DisplayName("Deve garantir igualdade e consistência de hashCode baseados no ID")
    void deveGarantirIgualdadePorId() {
        Empresa e1 = new Empresa(1L, "Empresa A", "123", "a@email.com");
        Empresa e2 = new Empresa(1L, "Empresa A (Modificada)", "456", "outro@email.com");
        Empresa e3 = new Empresa(2L, "Empresa B", "123", "b@email.com");

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
