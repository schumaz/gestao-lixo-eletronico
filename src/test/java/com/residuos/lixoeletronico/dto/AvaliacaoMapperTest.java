package com.residuos.lixoeletronico.dto;

import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoCreateRequest;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoMapper;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoResponse;
import com.residuos.lixoeletronico.model.Avaliacao;
import com.residuos.lixoeletronico.model.Empresa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvaliacaoMapperTest {

    @Test
    @DisplayName("Deve converter AvaliacaoCreateRequest para entidade Avaliacao fazendo trim nos textos")
    void deveConverterCreateRequestParaEntidade() {
        Empresa empresa = new Empresa(1L, "CODEL Reciclagem", "codel123", "contato@codel.com");
        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(
                "  Ana Paula  ",
                5,
                "  Local super organizado!  "
        );

        Avaliacao avaliacao = AvaliacaoMapper.toEntity(request, empresa);

        assertNotNull(avaliacao);
        assertEquals(empresa, avaliacao.getEmpresa());
        assertEquals("Ana Paula", avaliacao.getNomeUsuario());
        assertEquals(5, avaliacao.getNota());
        assertEquals("Local super organizado!", avaliacao.getComentario());
    }

    @Test
    @DisplayName("Deve converter comentário em branco para null no mapper")
    void deveConverterComentarioEmBrancoParaNull() {
        Empresa empresa = new Empresa(1L, "CODEL Reciclagem", "codel123", "contato@codel.com");
        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest("Carlos", 4, "   ");

        Avaliacao avaliacao = AvaliacaoMapper.toEntity(request, empresa);

        assertNotNull(avaliacao);
        assertNull(avaliacao.getComentario());
    }

    @Test
    @DisplayName("Deve converter entidade Avaliacao para AvaliacaoResponse")
    void deveConverterEntidadeParaResponse() {
        Empresa empresa = new Empresa(3L, "EcoPonto", "senha123", "eco@ponto.com");
        Avaliacao avaliacao = new Avaliacao(empresa, "Lucas", 4, "Bom atendimento");
        avaliacao.setId(15L);

        AvaliacaoResponse response = AvaliacaoMapper.toResponse(avaliacao);

        assertNotNull(response);
        assertEquals(15L, response.id());
        assertEquals(3L, response.empresaId());
        assertEquals("Lucas", response.nomeUsuario());
        assertEquals(4, response.nota());
        assertEquals("Bom atendimento", response.comentario());
    }

    @Test
    @DisplayName("Deve validar intervalo de nota (1 a 5) na entidade Avaliacao")
    void deveRejeitarNotaForaDoIntervaloNaEntidade() {
        Empresa empresa = new Empresa(1L, "EcoPonto", "senha123", "eco@ponto.com");

        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(empresa, "Lucas", 0, "Inválida"));
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(empresa, "Lucas", 6, "Inválida"));
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(empresa, "Lucas", null, "Inválida"));
    }

    @Test
    @DisplayName("Deve tratar argumentos nulos com segurança")
    void deveTratarNulosComSeguranca() {
        assertNull(AvaliacaoMapper.toEntity(null, new Empresa()));
        assertNull(AvaliacaoMapper.toResponse(null));
    }
}
