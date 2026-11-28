package com.residuos.lixoeletronico.controller;

import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoCreateRequest;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoResponse;
import com.residuos.lixoeletronico.dto.avaliacao.ResumoAvaliacaoResponse;
import com.residuos.lixoeletronico.service.AvaliacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AvaliacaoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AvaliacaoService avaliacaoService;

    @InjectMocks
    private AvaliacaoController avaliacaoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(avaliacaoController).build();
    }

    @Test
    @DisplayName("POST /api/empresas/{empresaId}/avaliacoes deve registrar avaliação e retornar 201 Created")
    void deveRegistrarAvaliacaoComSucesso() throws Exception {
        String payload = """
                {
                  "nomeUsuario": "Mariana Oliveira",
                  "nota": 5,
                  "comentario": "Ponto de coleta muito organizado"
                }
                """;

        AvaliacaoResponse response = new AvaliacaoResponse(
                10L, 1L, "Mariana Oliveira", 5, "Ponto de coleta muito organizado", Instant.now()
        );

        when(avaliacaoService.registrarAvaliacao(eq(1L), any(AvaliacaoCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/empresas/1/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.empresaId").value(1L))
                .andExpect(jsonPath("$.nomeUsuario").value("Mariana Oliveira"))
                .andExpect(jsonPath("$.nota").value(5));
    }

    @Test
    @DisplayName("POST /api/empresas/{empresaId}/avaliacoes com nota inválida deve retornar 400 Bad Request")
    void deveRejeitarAvaliacaoInvalida() throws Exception {
        String payloadInvalido = """
                {
                  "nomeUsuario": "",
                  "nota": 6,
                  "comentario": "Nota fora do limite"
                }
                """;

        mockMvc.perform(post("/api/empresas/1/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadInvalido))
                .andExpect(status().isBadRequest());

        verify(avaliacaoService, never()).registrarAvaliacao(any(), any());
    }

    @Test
    @DisplayName("GET /api/empresas/{empresaId}/avaliacoes deve retornar resumo com média e avaliações")
    void deveConsultarResumoAvaliacoes() throws Exception {
        AvaliacaoResponse av = new AvaliacaoResponse(10L, 1L, "Carlos", 4, "Bom local", Instant.now());
        ResumoAvaliacaoResponse resumo = new ResumoAvaliacaoResponse(1L, 4.5, 2L, List.of(av));

        when(avaliacaoService.obterResumoAvaliacoes(1L)).thenReturn(resumo);

        mockMvc.perform(get("/api/empresas/1/avaliacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(1L))
                .andExpect(jsonPath("$.mediaNota").value(4.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2))
                .andExpect(jsonPath("$.avaliacoes[0].nomeUsuario").value("Carlos"));
    }

    @Test
    @DisplayName("GET /api/empresas/{empresaId}/avaliacoes para empresa inexistente deve retornar 404 Not Found")
    void deveRetornarNotFoundQuandoEmpresaNaoExistir() throws Exception {
        when(avaliacaoService.obterResumoAvaliacoes(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        mockMvc.perform(get("/api/empresas/999/avaliacoes"))
                .andExpect(status().isNotFound());
    }
}
