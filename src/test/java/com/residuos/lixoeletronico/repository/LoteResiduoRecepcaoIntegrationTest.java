package com.residuos.lixoeletronico.repository;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LoteResiduoRecepcaoIntegrationTest {

    @Autowired
    private LoteResiduoService loteResiduoService;

    @Autowired
    private LoteResiduoRepository loteResiduoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    @DisplayName("Deve registrar recepção de lotes, listar no inventário em estoque e atualizar balanço de massa da empresa")
    void deveRegistrarRecepcaoAtualizandoInventarioEBalancoDeMassa() {
        Empresa empresa = empresaRepository.save(
                new Empresa(null, "Recicla Teste Integrado", "senha123", "integrado@recicla.com")
        );
        Long empresaId = empresa.getId();

        LoteResiduoCreateRequest lote1Request = new LoteResiduoCreateRequest(
                "Ana Beatriz",
                "(34) 99111-2222",
                LocalDate.now(),
                CategoriaMaterial.INFORMATICA,
                "Monitores e teclados",
                new BigDecimal("30.250"),
                UrgenciaDescarte.MEDIA
        );

        LoteResiduoCreateRequest lote2Request = new LoteResiduoCreateRequest(
                "Lucas Ferreira",
                "lucas@email.com",
                LocalDate.now().minusDays(1),
                CategoriaMaterial.PILHAS_BATERIAS,
                "Baterias de nobreak",
                new BigDecimal("14.750"),
                UrgenciaDescarte.CRITICA
        );

        LoteResiduoResponse resp1 = loteResiduoService.registrarRecepcao(empresaId, lote1Request);
        LoteResiduoResponse resp2 = loteResiduoService.registrarRecepcao(empresaId, lote2Request);

        assertNotNull(resp1.id());
        assertNotNull(resp2.id());
        assertEquals(StatusLote.EM_ESTOQUE, resp1.status());
        assertEquals(StatusLote.EM_ESTOQUE, resp2.status());

        List<LoteResiduoResponse> estoque = loteResiduoService.listarLotesEmEstoque(empresaId, null, null);
        assertEquals(2, estoque.size());
        assertEquals(resp1.id(), estoque.get(0).id());
        assertEquals(resp2.id(), estoque.get(1).id());

        BigDecimal massaEmEstoque = loteResiduoRepository.somarPesoKgPorEmpresaEStatus(empresaId, StatusLote.EM_ESTOQUE);
        assertEquals(0, new BigDecimal("45.000").compareTo(massaEmEstoque));

        Empresa empresaAtualizada = empresaRepository.findById(empresaId).orElseThrow();
        assertEquals(0, new BigDecimal("45.000").compareTo(empresaAtualizada.getTotalKgDescartado()));
    }
}
