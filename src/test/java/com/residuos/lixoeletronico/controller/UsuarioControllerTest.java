package com.residuos.lixoeletronico.controller;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
    }

    @Test
    @DisplayName("GET /api/usuarios - Deve retornar 200 OK com lista de usuários")
    void deveListarTodosOsUsuarios() throws Exception {
        when(usuarioService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/usuarios")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(usuarioService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Deve retornar 200 OK quando usuário existir")
    void deveBuscarUsuarioPorId() throws Exception {
        when(usuarioService.buscarResponsePorId(1L)).thenReturn(null);

        mockMvc.perform(get("/api/usuarios/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).buscarResponsePorId(1L);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Deve retornar 404 Not Found quando ID não existir")
    void deveRetornar404QuandoIdNaoExistir() throws Exception {
        when(usuarioService.buscarResponsePorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        mockMvc.perform(get("/api/usuarios/{id}", 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).buscarResponsePorId(99L);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id}/perfil - Deve retornar 200 OK com perfil completo")
    void deveBuscarPerfilCompletoPorId() throws Exception {
        when(usuarioService.buscarPerfilCompletoPorId(1L)).thenReturn(null);

        mockMvc.perform(get("/api/usuarios/{id}/perfil", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).buscarPerfilCompletoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id}/perfil - Deve retornar 404 Not Found para ID inexistente")
    void deveRetornar404NoPerfilQuandoIdNaoExistir() throws Exception {
        when(usuarioService.buscarPerfilCompletoPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        mockMvc.perform(get("/api/usuarios/{id}/perfil", 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).buscarPerfilCompletoPorId(99L);
    }

    @Test
    @DisplayName("GET /api/usuarios/buscar - Deve retornar 200 OK ao buscar por e-mail válido")
    void deveBuscarUsuarioPorEmail() throws Exception {
        when(usuarioService.buscarResponsePorEmail("gianluca@email.com")).thenReturn(null);

        mockMvc.perform(get("/api/usuarios/buscar")
                        .param("email", "gianluca@email.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).buscarResponsePorEmail("gianluca@email.com");
    }

    @Test
    @DisplayName("GET /api/usuarios/buscar - Deve retornar 400 Bad Request quando e-mail estiver em branco")
    void deveRetornar400QuandoEmailEmBranco() throws Exception {
        when(usuarioService.buscarResponsePorEmail("   "))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido."));

        mockMvc.perform(get("/api/usuarios/buscar")
                        .param("email", "   ")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(usuarioService, times(1)).buscarResponsePorEmail("   ");
    }

    @Test
    @DisplayName("GET /api/usuarios/buscar - Deve retornar 400 Bad Request sem parâmetro email")
    void deveRetornar400SemParametroEmail() throws Exception {
        mockMvc.perform(get("/api/usuarios/buscar")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }
}