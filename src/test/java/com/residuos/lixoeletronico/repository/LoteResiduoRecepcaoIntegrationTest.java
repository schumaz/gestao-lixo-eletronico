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

    @Test
    @DisplayName("Deve transicionar status dos lotes no ciclo de vida, filtrar por status e manter balanço de massa")
    void deveAtualizarStatusEFiltrarPorStatusMantendoBalancoDeMassa() {
        Empresa empresa = empresaRepository.save(
                new Empresa(null, "Eco Ciclo Teste", "senha123", "ciclo@eco.com")
        );
        Long empresaId = empresa.getId();

        LoteResiduoResponse loteA = loteResiduoService.registrarRecepcao(
                empresaId,
                new LoteResiduoCreateRequest(
                        "Ana",
                        "ana@email.com",
                        LocalDate.now(),
                        CategoriaMaterial.INFORMATICA,
                        "Notebooks",
                        new BigDecimal("20.000"),
                        UrgenciaDescarte.ALTA
                )
        );
        LoteResiduoResponse loteB = loteResiduoService.registrarRecepcao(
                empresaId,
                new LoteResiduoCreateRequest(
                        "Bruno",
                        "bruno@email.com",
                        LocalDate.now().minusDays(1),
                        CategoriaMaterial.PILHAS_BATERIAS,
                        "Baterias",
                        new BigDecimal("10.500"),
                        UrgenciaDescarte.CRITICA
                )
        );

        // Transição válida: loteA EM_ESTOQUE -> EM_PROCESSAMENTO
        LoteResiduoResponse loteAProcessando = loteResiduoService.atualizarStatus(
                empresaId,
                loteA.id(),
                StatusLote.EM_PROCESSAMENTO
        );
        assertEquals(StatusLote.EM_PROCESSAMENTO, loteAProcessando.status());

        // Transição inválida: pular EM_ESTOQUE -> DESTINADO no loteB
        var exPulo = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> loteResiduoService.atualizarStatus(empresaId, loteB.id(), StatusLote.DESTINADO)
        );
        assertEquals(org.springframework.http.HttpStatus.CONFLICT, exPulo.getStatusCode());

        // Transição válida: loteA EM_PROCESSAMENTO -> DESTINADO
        LoteResiduoResponse loteADestinado = loteResiduoService.atualizarStatus(
                empresaId,
                loteA.id(),
                StatusLote.DESTINADO
        );
        assertEquals(StatusLote.DESTINADO, loteADestinado.status());

        // Transição válida: loteB EM_ESTOQUE -> EM_PROCESSAMENTO
        LoteResiduoResponse loteBProcessando = loteResiduoService.atualizarStatus(
                empresaId,
                loteB.id(),
                StatusLote.EM_PROCESSAMENTO
        );
        assertEquals(StatusLote.EM_PROCESSAMENTO, loteBProcessando.status());

        // Filtros por status
        assertTrue(loteResiduoService.listarLotes(empresaId, StatusLote.EM_ESTOQUE, null, null).isEmpty());

        List<LoteResiduoResponse> emProcessamento = loteResiduoService.listarLotes(
                empresaId,
                StatusLote.EM_PROCESSAMENTO,
                null,
                null
        );
        assertEquals(1, emProcessamento.size());
        assertEquals(loteB.id(), emProcessamento.get(0).id());

        List<LoteResiduoResponse> destinados = loteResiduoService.listarLotes(
                empresaId,
                StatusLote.DESTINADO,
                CategoriaMaterial.INFORMATICA,
                UrgenciaDescarte.ALTA
        );
        assertEquals(1, destinados.size());
        assertEquals(loteA.id(), destinados.get(0).id());

        // Sem filtro de status (null) retorna todos os lotes da empresa ordenados
        List<LoteResiduoResponse> todos = loteResiduoService.listarLotes(empresaId, null, null, null);
        assertEquals(2, todos.size());
        assertEquals(loteA.id(), todos.get(0).id());
        assertEquals(loteB.id(), todos.get(1).id());

        // Balanço de massa por status e total acumulado
        assertEquals(0, BigDecimal.ZERO.compareTo(
                loteResiduoRepository.somarPesoKgPorEmpresaEStatus(empresaId, StatusLote.EM_ESTOQUE)
        ));
        assertEquals(0, new BigDecimal("10.500").compareTo(
                loteResiduoRepository.somarPesoKgPorEmpresaEStatus(empresaId, StatusLote.EM_PROCESSAMENTO)
        ));
        assertEquals(0, new BigDecimal("20.000").compareTo(
                loteResiduoRepository.somarPesoKgPorEmpresaEStatus(empresaId, StatusLote.DESTINADO)
        ));
        Empresa empresaAtualizada = empresaRepository.findById(empresaId).orElseThrow();
        assertEquals(0, new BigDecimal("30.500").compareTo(empresaAtualizada.getTotalKgDescartado()));
    }
}
