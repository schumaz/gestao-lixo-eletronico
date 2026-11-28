package com.residuos.lixoeletronico.service;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.LoteResiduo;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.LoteResiduoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteResiduoServiceTest {

    @Mock
    private LoteResiduoRepository loteResiduoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private LoteResiduoService service;

    @Test
    @DisplayName("Deve registrar recepção de lote em estoque e incrementar massa da empresa")
    void deveRegistrarRecepcaoComSucesso() {
        Empresa empresa = new Empresa(1L, "CODEL Reciclagem", "codel123", "contato@codel.com");
        LocalDate dataRecepcao = LocalDate.now();
        LoteResiduoCreateRequest request = new LoteResiduoCreateRequest(
                "Mariana Oliveira",
                "(34) 99123-4567",
                dataRecepcao,
                CategoriaMaterial.INFORMATICA,
                "Notebooks corporativos",
                new BigDecimal("25.500"),
                UrgenciaDescarte.MEDIA
        );

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(loteResiduoRepository.save(any(LoteResiduo.class))).thenAnswer(invocation -> {
            LoteResiduo lote = invocation.getArgument(0);
            lote.setId(100L);
            return lote;
        });

        LoteResiduoResponse response = service.registrarRecepcao(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(1L, response.empresaId());
        assertEquals("Mariana Oliveira", response.nomeUsuario());
        assertEquals(CategoriaMaterial.INFORMATICA, response.categoriaMaterial());
        assertEquals(new BigDecimal("25.500"), response.pesoKg());
        assertEquals(StatusLote.EM_ESTOQUE, response.status());

        verify(loteResiduoRepository, times(1)).save(any(LoteResiduo.class));
        verify(empresaRepository, times(1)).adicionarKgDescartado(1L, new BigDecimal("25.500"));
    }

    @Test
    @DisplayName("Deve lançar 404 Not Found ao tentar registrar recepção para empresa inexistente")
    void deveLancarNotFoundQuandoEmpresaNaoExiste() {
        LoteResiduoCreateRequest request = new LoteResiduoCreateRequest(
                "João Pedro",
                "joao@email.com",
                LocalDate.now(),
                CategoriaMaterial.CELULARES_TABLETS,
                "Smartphones",
                new BigDecimal("4.200"),
                UrgenciaDescarte.ALTA
        );

        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarRecepcao(999L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(loteResiduoRepository, never()).save(any());
        verify(empresaRepository, never()).adicionarKgDescartado(any(), any());
    }

    @Test
    @DisplayName("Deve atualizar status de EM_ESTOQUE para EM_PROCESSAMENTO")
    void deveAtualizarStatusDeEmEstoqueParaEmProcessamento() {
        LoteResiduo lote = criarLote(10L, 1L, StatusLote.EM_ESTOQUE);
        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(loteResiduoRepository.findByIdAndEmpresaId(10L, 1L)).thenReturn(Optional.of(lote));
        when(loteResiduoRepository.save(any(LoteResiduo.class))).thenAnswer(inv -> inv.getArgument(0));

        LoteResiduoResponse response = service.atualizarStatus(1L, 10L, StatusLote.EM_PROCESSAMENTO);

        assertEquals(StatusLote.EM_PROCESSAMENTO, response.status());
        verify(loteResiduoRepository, times(1)).save(lote);
    }

    @Test
    @DisplayName("Deve atualizar status de EM_PROCESSAMENTO para DESTINADO sem informar empresaId")
    void deveAtualizarStatusDeEmProcessamentoParaDestinadoSemEmpresaId() {
        LoteResiduo lote = criarLote(10L, 1L, StatusLote.EM_PROCESSAMENTO);
        when(loteResiduoRepository.findById(10L)).thenReturn(Optional.of(lote));
        when(loteResiduoRepository.save(any(LoteResiduo.class))).thenAnswer(inv -> inv.getArgument(0));

        LoteResiduoResponse response = service.atualizarStatus(10L, StatusLote.DESTINADO);

        assertEquals(StatusLote.DESTINADO, response.status());
        verify(empresaRepository, never()).existsById(any());
        verify(loteResiduoRepository, times(1)).save(lote);
    }

    @ParameterizedTest
    @CsvSource({
        "EM_ESTOQUE, EM_ESTOQUE",
        "EM_ESTOQUE, DESTINADO",
        "EM_PROCESSAMENTO, EM_ESTOQUE",
        "EM_PROCESSAMENTO, EM_PROCESSAMENTO",
        "DESTINADO, EM_ESTOQUE",
        "DESTINADO, EM_PROCESSAMENTO",
        "DESTINADO, DESTINADO"
    })
    @DisplayName("Deve lançar 409 Conflict para transições de status inválidas")
    void deveRejeitarTransicaoDeStatusInvalida(StatusLote statusAtual, StatusLote novoStatus) {
        LoteResiduo lote = criarLote(10L, 1L, statusAtual);
        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(loteResiduoRepository.findByIdAndEmpresaId(10L, 1L)).thenReturn(Optional.of(lote));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(1L, 10L, novoStatus)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(statusAtual, lote.getStatus());
        verify(loteResiduoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar 400 Bad Request ao atualizar status com loteId ou novoStatus nulo")
    void deveRejeitarParametrosNulosAoAtualizarStatus() {
        ResponseStatusException exLoteNulo = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(1L, null, StatusLote.EM_PROCESSAMENTO)
        );
        ResponseStatusException exStatusNulo = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(1L, 10L, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exLoteNulo.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exStatusNulo.getStatusCode());
        verifyNoInteractions(loteResiduoRepository);
    }

    @Test
    @DisplayName("Deve lançar 404 Not Found ao atualizar status quando empresa ou lote não existir")
    void deveLancarNotFoundQuandoEmpresaOuLoteNaoExistir() {
        when(empresaRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exEmpresa = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(99L, 10L, StatusLote.EM_PROCESSAMENTO)
        );
        assertEquals(HttpStatus.NOT_FOUND, exEmpresa.getStatusCode());

        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(loteResiduoRepository.findByIdAndEmpresaId(999L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exLote = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(1L, 999L, StatusLote.EM_PROCESSAMENTO)
        );
        assertEquals(HttpStatus.NOT_FOUND, exLote.getStatusCode());
        verify(loteResiduoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar lotes filtrando por status e ordenando por dataRecepcao e id decrescentes")
    @SuppressWarnings("unchecked")
    void deveListarLotesComFiltroPorStatus() {
        LoteResiduo lote = criarLote(15L, 1L, StatusLote.EM_PROCESSAMENTO);
        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(loteResiduoRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(lote));

        List<LoteResiduoResponse> resultado = service.listarLotes(
                1L,
                StatusLote.EM_PROCESSAMENTO,
                CategoriaMaterial.INFORMATICA,
                UrgenciaDescarte.MEDIA
        );

        assertEquals(1, resultado.size());
        assertEquals(15L, resultado.get(0).id());
        assertEquals(StatusLote.EM_PROCESSAMENTO, resultado.get(0).status());
        verify(loteResiduoRepository, times(1)).findAll(
                any(Specification.class),
                eq(Sort.by(Sort.Order.desc("dataRecepcao"), Sort.Order.desc("id")))
        );
    }

    @Test
    @DisplayName("Deve lançar 404 Not Found ao listar lotes para empresa inexistente")
    void deveLancarNotFoundAoListarLotesDeEmpresaInexistente() {
        when(empresaRepository.existsById(999L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.listarLotes(999L, StatusLote.DESTINADO, null, null)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(loteResiduoRepository);
    }

    private LoteResiduo criarLote(Long loteId, Long empresaId, StatusLote status) {
        Empresa empresa = new Empresa(empresaId, "CODEL Reciclagem", "codel123", "contato@codel.com");
        LoteResiduo lote = new LoteResiduo(
                empresa,
                "Mariana Oliveira",
                "(34) 99123-4567",
                LocalDate.now(),
                CategoriaMaterial.INFORMATICA,
                "Notebooks corporativos",
                new BigDecimal("25.500"),
                UrgenciaDescarte.MEDIA
        );
        lote.setId(loteId);
        lote.setStatus(status);
        return lote;
    }
}
