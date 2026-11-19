package com.residuos.lixoeletronico.dto;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoMapper;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.LoteResiduo;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoteResiduoMapperTest {

    @Test
    @DisplayName("Deve converter LoteResiduoCreateRequest para entidade LoteResiduo com status inicial EM_ESTOQUE")
    void deveConverterCreateRequestParaEntidade() {
        Empresa empresa = new Empresa(1L, "CODEL Reciclagem", "codel123", "contato@codel.com");
        LocalDate dataRecepcao = LocalDate.now();
        LoteResiduoCreateRequest request = new LoteResiduoCreateRequest(
                "  Ana Paula  ",
                "  (34) 99123-0000  ",
                dataRecepcao,
                CategoriaMaterial.INFORMATICA,
                "  Placas-mãe e notebooks  ",
                new BigDecimal("15.750"),
                UrgenciaDescarte.ALTA
        );

        LoteResiduo lote = LoteResiduoMapper.toEntity(request, empresa);

        assertNotNull(lote);
        assertEquals(empresa, lote.getEmpresa());
        assertEquals("Ana Paula", lote.getNomeUsuario());
        assertEquals("(34) 99123-0000", lote.getContatoUsuario());
        assertEquals(dataRecepcao, lote.getDataRecepcao());
        assertEquals(CategoriaMaterial.INFORMATICA, lote.getCategoriaMaterial());
        assertEquals("Placas-mãe e notebooks", lote.getTipoResiduo());
        assertEquals(new BigDecimal("15.750"), lote.getPesoKg());
        assertEquals(UrgenciaDescarte.ALTA, lote.getUrgenciaDescarte());
        assertEquals(StatusLote.EM_ESTOQUE, lote.getStatus());
    }

    @Test
    @DisplayName("Deve converter LoteResiduo para LoteResiduoResponse")
    void deveConverterEntidadeParaResponse() {
        Empresa empresa = new Empresa(5L, "EcoPonto", "senha123", "eco@ponto.com");
        LocalDate dataRecepcao = LocalDate.now().minusDays(1);
        LoteResiduo lote = new LoteResiduo(
                empresa,
                "Carlos Silva",
                "carlos@email.com",
                dataRecepcao,
                CategoriaMaterial.PILHAS_BATERIAS,
                "Baterias de lítio",
                new BigDecimal("8.500"),
                UrgenciaDescarte.CRITICA
        );
        lote.setId(42L);

        LoteResiduoResponse response = LoteResiduoMapper.toResponse(lote);

        assertNotNull(response);
        assertEquals(42L, response.id());
        assertEquals(5L, response.empresaId());
        assertEquals("Carlos Silva", response.nomeUsuario());
        assertEquals("carlos@email.com", response.contatoUsuario());
        assertEquals(dataRecepcao, response.dataRecepcao());
        assertEquals(CategoriaMaterial.PILHAS_BATERIAS, response.categoriaMaterial());
        assertEquals("Baterias de lítio", response.tipoResiduo());
        assertEquals(new BigDecimal("8.500"), response.pesoKg());
        assertEquals(UrgenciaDescarte.CRITICA, response.urgenciaDescarte());
        assertEquals(StatusLote.EM_ESTOQUE, response.status());
    }

    @Test
    @DisplayName("Deve retornar nulo quando argumentos do mapper forem nulos")
    void deveTratarNulosComSeguranca() {
        assertNull(LoteResiduoMapper.toEntity(null, new Empresa()));
        assertNull(LoteResiduoMapper.toResponse(null));
    }
}
