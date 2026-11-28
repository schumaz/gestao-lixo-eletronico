package com.residuos.lixoeletronico.repository;

import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoCreateRequest;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoResponse;
import com.residuos.lixoeletronico.dto.avaliacao.ResumoAvaliacaoResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.service.AvaliacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AvaliacaoRepositoryIntegrationTest {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("Deve persistir avaliações no banco e calcular média e contagem corretamente")
    void devePersistirAvaliacoesECalcularMedia() {
        Empresa empresa = empresaRepository.save(
                new Empresa(null, "EcoCentro Avaliado", "senha123", "avaliado@ecocentro.com")
        );
        Long empresaId = empresa.getId();

        AvaliacaoResponse r1 = avaliacaoService.registrarAvaliacao(
                empresaId,
                new AvaliacaoCreateRequest("João Silva", 5, "Excelente organização!")
        );
        AvaliacaoResponse r2 = avaliacaoService.registrarAvaliacao(
                empresaId,
                new AvaliacaoCreateRequest("Maria Souza", 4, "Bom atendimento.")
        );
        AvaliacaoResponse r3 = avaliacaoService.registrarAvaliacao(
                empresaId,
                new AvaliacaoCreateRequest("Pedro Santos", 5, "Muito confiável.")
        );

        assertNotNull(r1.id());
        assertNotNull(r2.id());
        assertNotNull(r3.id());

        assertEquals(3L, avaliacaoRepository.countByEmpresaId(empresaId));

        ResumoAvaliacaoResponse resumo = avaliacaoService.obterResumoAvaliacoes(empresaId);
        assertEquals(3L, resumo.totalAvaliacoes());
        assertEquals(4.7, resumo.mediaNota());
        assertEquals(3, resumo.avaliacoes().size());
        assertEquals("Pedro Santos", resumo.avaliacoes().get(0).nomeUsuario());
    }
}
