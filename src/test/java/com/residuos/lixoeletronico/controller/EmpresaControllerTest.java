package com.residuos.lixoeletronico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuos.lixoeletronico.dto.empresa.EmpresaCreateRequest;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.service.EmpresaService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmpresaControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private EmpresaController empresaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(empresaController).build();
    }

    @Test
    @DisplayName("POST /api/empresas deve cadastrar empresa e retornar 201 Created")
    void deveCadastrarEmpresaComSucesso() throws Exception {
        EmpresaCreateRequest request = new EmpresaCreateRequest("Tech Recicla", "tech@recicla.com", "senhaForte123", null, null);
        EmpresaResponse response = new EmpresaResponse(
                1L, "Tech Recicla", "tech@recicla.com",
                null, null, null, null, null,
                List.of(), Set.of(), BigDecimal.ZERO, Instant.now(), null
        );

        when(empresaService.criarEmpresa(any(EmpresaCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Tech Recicla"))
                .andExpect(jsonPath("$.email").value("tech@recicla.com"));
    }

    @Test
    @DisplayName("GET /api/empresas/{id}/perfil deve retornar 200 OK com dados da empresa")
    void deveRetornarPerfilComSucesso() throws Exception {
        EmpresaResponse response = new EmpresaResponse(
                1L, "CODEL Reciclagem", "contato@codel.com",
                null, null, "Quem somos nós", "Por que reciclamos", null,
                List.of(), Set.of(), new BigDecimal("67000.000"), Instant.now(), null
        );

        when(empresaService.buscarPerfilPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/empresas/1/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("CODEL Reciclagem"))
                .andExpect(jsonPath("$.quemSomos").value("Quem somos nós"));
    }

    @Test
    @DisplayName("GET /api/empresas/{id}/perfil com ID inexistente deve retornar 404 Not Found")
    void deveRetornarNotFoundQuandoPerfilNaoExistir() throws Exception {
        when(empresaService.buscarPerfilPorId(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        mockMvc.perform(get("/api/empresas/999/perfil"))
                .andExpect(status().isNotFound());
    }
}
