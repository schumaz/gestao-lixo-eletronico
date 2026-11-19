package com.residuos.lixoeletronico.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class LoteResiduoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoteResiduoService loteResiduoService;

    @InjectMocks
    private LoteResiduoController loteResiduoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            loteResiduoController
        ).build();
    }

    @Test
    @DisplayName(
        "POST /api/empresas/{empresaId}/lotes deve registrar recepção e retornar 201 Created"
    )
    void deveRegistrarRecepcaoComSucesso() throws Exception {
        LocalDate dataRecepcao = LocalDate.now();
        String payload = """
        {
          "nomeUsuario": "Mariana Oliveira",
          "contatoUsuario": "(34) 99123-4567",
          "dataRecepcao": "%s",
          "categoriaMaterial": "INFORMATICA",
          "tipoResiduo": "Notebooks corporativos e placas-mãe",
          "pesoKg": 42.500,
          "urgenciaDescarte": "MEDIA"
        }
        """.formatted(dataRecepcao);

        LoteResiduoResponse response = new LoteResiduoResponse(
            10L,
            1L,
            "Mariana Oliveira",
            "(34) 99123-4567",
            dataRecepcao,
            CategoriaMaterial.INFORMATICA,
            "Notebooks corporativos e placas-mãe",
            new BigDecimal("42.500"),
            UrgenciaDescarte.MEDIA,
            StatusLote.EM_ESTOQUE,
            Instant.now(),
            null
        );

        when(
            loteResiduoService.registrarRecepcao(
                eq(1L),
                any(LoteResiduoCreateRequest.class)
            )
        ).thenReturn(response);

        mockMvc
            .perform(
                post("/api/empresas/1/lotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.empresaId").value(1L))
            .andExpect(jsonPath("$.nomeUsuario").value("Mariana Oliveira"))
            .andExpect(jsonPath("$.categoriaMaterial").value("INFORMATICA"))
            .andExpect(jsonPath("$.pesoKg").value(42.5))
            .andExpect(jsonPath("$.status").value("EM_ESTOQUE"));
    }

    @Test
    @DisplayName(
        "POST /api/empresas/{empresaId}/lotes com dados inválidos deve retornar 400 Bad Request"
    )
    void deveRejeitarFormularioInvalido() throws Exception {
        String payloadInvalido = """
        {
          "nomeUsuario": "",
          "contatoUsuario": "",
          "dataRecepcao": "%s",
          "categoriaMaterial": null,
          "tipoResiduo": "",
          "pesoKg": 0,
          "urgenciaDescarte": null
        }
        """.formatted(LocalDate.now().plusDays(1));

        mockMvc
            .perform(
                post("/api/empresas/1/lotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadInvalido)
            )
            .andExpect(status().isBadRequest());

        verify(loteResiduoService, never()).registrarRecepcao(any(), any());
    }

    @Test
    @DisplayName(
        "POST /api/empresas/{empresaId}/lotes para empresa inexistente deve retornar 404 Not Found"
    )
    void deveRetornarNotFoundQuandoEmpresaNaoExistir() throws Exception {
        String payload = """
        {
          "nomeUsuario": "Carlos Lima",
          "contatoUsuario": "carlos@email.com",
          "dataRecepcao": "%s",
          "categoriaMaterial": "PILHAS_BATERIAS",
          "tipoResiduo": "Baterias de lítio",
          "pesoKg": 5.000,
          "urgenciaDescarte": "CRITICA"
        }
        """.formatted(LocalDate.now());

        when(
            loteResiduoService.registrarRecepcao(
                eq(999L),
                any(LoteResiduoCreateRequest.class)
            )
        ).thenThrow(
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Empresa não encontrada"
            )
        );

        mockMvc
            .perform(
                post("/api/empresas/999/lotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GET /api/empresas/{empresaId}/lotes/estoque deve retornar 200 OK com lotes em estoque"
    )
    void deveConsultarLotesEmEstoquePorEmpresa() throws Exception {
        LoteResiduoResponse lote = new LoteResiduoResponse(
            10L,
            1L,
            "Mariana Oliveira",
            "(34) 99123-4567",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Notebooks",
            new BigDecimal("42.500"),
            UrgenciaDescarte.MEDIA,
            StatusLote.EM_ESTOQUE,
            Instant.now(),
            null
        );

        when(
            loteResiduoService.listarLotesEmEstoque(1L, null, null)
        ).thenReturn(List.of(lote));

        mockMvc
            .perform(get("/api/empresas/1/lotes/estoque"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(10L))
            .andExpect(jsonPath("$[0].status").value("EM_ESTOQUE"));
    }
}
