package com.residuos.lixoeletronico.service;

import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoCreateRequest;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoResponse;
import com.residuos.lixoeletronico.dto.avaliacao.ResumoAvaliacaoResponse;
import com.residuos.lixoeletronico.model.Avaliacao;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.repository.AvaliacaoRepository;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private AvaliacaoService service;

    @Test
    @DisplayName("Deve registrar avaliação com sucesso para empresa existente")
    void deveRegistrarAvaliacaoComSucesso() {
        Empresa empresa = new Empresa(1L, "CODEL Reciclagem", "codel123", "contato@codel.com");
        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(
                "Mariana Oliveira",
                5,
                "Excelente organização e atendimento."
        );

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> {
            Avaliacao a = invocation.getArgument(0);
            a.setId(50L);
            return a;
        });

        AvaliacaoResponse response = service.registrarAvaliacao(1L, request);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals(1L, response.empresaId());
        assertEquals("Mariana Oliveira", response.nomeUsuario());
        assertEquals(5, response.nota());
        assertEquals("Excelente organização e atendimento.", response.comentario());
        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
    }

    @Test
    @DisplayName("Deve lançar 404 Not Found ao tentar avaliar empresa inexistente")
    void deveLancarNotFoundAoAvaliarEmpresaInexistente() {
        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest("Mariana", 5, "Ótimo");
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarAvaliacao(999L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve obter resumo com média arredondada e lista de avaliações")
    void deveObterResumoAvaliacoesComSucesso() {
        Empresa empresa = new Empresa(1L, "CODEL Reciclagem", "codel123", "contato@codel.com");
        Avaliacao av1 = new Avaliacao(empresa, "Mariana", 5, "Muito bom");
        av1.setId(1L);
        Avaliacao av2 = new Avaliacao(empresa, "Carlos", 4, "Bom");
        av2.setId(2L);

        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(avaliacaoRepository.countByEmpresaId(1L)).thenReturn(2L);
        when(avaliacaoRepository.calcularMediaNotasPorEmpresa(1L)).thenReturn(4.6666);
        when(avaliacaoRepository.findByEmpresaIdOrderByCriadoEmDescIdDesc(1L)).thenReturn(List.of(av1, av2));

        ResumoAvaliacaoResponse resumo = service.obterResumoAvaliacoes(1L);

        assertEquals(1L, resumo.empresaId());
        assertEquals(4.7, resumo.mediaNota());
        assertEquals(2L, resumo.totalAvaliacoes());
        assertEquals(2, resumo.avaliacoes().size());
    }

    @Test
    @DisplayName("Deve retornar média 0.0 quando empresa ainda não tiver avaliações")
    void deveRetornarMediaZeroQuandoSemAvaliacoes() {
        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(avaliacaoRepository.countByEmpresaId(1L)).thenReturn(0L);
        when(avaliacaoRepository.findByEmpresaIdOrderByCriadoEmDescIdDesc(1L)).thenReturn(List.of());

        ResumoAvaliacaoResponse resumo = service.obterResumoAvaliacoes(1L);

        assertEquals(0.0, resumo.mediaNota());
        assertEquals(0L, resumo.totalAvaliacoes());
        assertTrue(resumo.avaliacoes().isEmpty());
        verify(avaliacaoRepository, never()).calcularMediaNotasPorEmpresa(any());
    }
}
