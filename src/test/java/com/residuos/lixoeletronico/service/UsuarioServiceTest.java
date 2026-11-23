package com.residuos.lixoeletronico.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.dto.usuario.ItemHistoricoDTO;
import com.residuos.lixoeletronico.model.Usuario;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private LoteResiduoService loteResiduoService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setNome("Gianluca Agostinho");
        usuarioMock.setEmail("gianluca@email.com");
    }

    @Test
    @DisplayName("Deve retornar a lista de todos os usuários cadastrados")
    void deveListarTodosOsUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioMock));

        var resultado = usuarioService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
        verifyNoInteractions(empresaRepository);
    }

    @Test
    @DisplayName("Deve buscar entidade Usuario por ID quando existir")
    void deveBuscarUsuarioPorIdComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Gianluca Agostinho", resultado.getNome());
        assertEquals("gianluca@email.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar UsuarioResponse por ID quando existir")
    void deveBuscarResponsePorIdComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        var resultado = usuarioService.buscarResponsePorId(1L);

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar 404 NOT_FOUND ao buscar ID inexistente")
    void deveLancar404QuandoIdNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> usuarioService.buscarResponsePorId(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(usuarioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve normalizar e-mail (trim e lowercase) e retornar UsuarioResponse")
    void deveBuscarResponsePorEmailComSucesso() {
        when(usuarioRepository.findByEmailIgnoreCase("gianluca@email.com"))
                .thenReturn(Optional.of(usuarioMock));

        var resultado = usuarioService.buscarResponsePorEmail("  GIANLUCA@EMAIL.COM  ");

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findByEmailIgnoreCase("gianluca@email.com");
    }

    @Test
    @DisplayName("Deve lançar 400 BAD_REQUEST quando e-mail for nulo ou em branco")
    void deveLancar400QuandoEmailInvalido() {
        ResponseStatusException exBranco = assertThrows(ResponseStatusException.class,
                () -> usuarioService.buscarResponsePorEmail("   "));
        ResponseStatusException exNulo = assertThrows(ResponseStatusException.class,
                () -> usuarioService.buscarResponsePorEmail(null));

        assertEquals(HttpStatus.BAD_REQUEST, exBranco.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exNulo.getStatusCode());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("Deve lançar 404 NOT_FOUND quando e-mail não estiver cadastrado")
    void deveLancar404QuandoEmailNaoEncontrado() {
        when(usuarioRepository.findByEmailIgnoreCase("inexistente@email.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> usuarioService.buscarResponsePorEmail("inexistente@email.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(usuarioRepository, times(1)).findByEmailIgnoreCase("inexistente@email.com");
    }

    @Test
    @DisplayName("Deve montar perfil completo com métricas zeradas quando histórico estiver vazio")
    void deveBuscarPerfilCompletoComHistoricoVazio() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(loteResiduoService.listarHistoricoPorUsuario(1L)).thenReturn(Collections.emptyList());
        when(loteResiduoService.calcularTotalKg(Collections.emptyList())).thenReturn(0.0);
        when(loteResiduoService.obterMaterialMaisDescartado(Collections.emptyList())).thenReturn("-");

        var perfil = usuarioService.buscarPerfilCompletoPorId(1L);

        assertNotNull(perfil);
        verify(loteResiduoService, times(1)).listarHistoricoPorUsuario(1L);
        verify(loteResiduoService, times(1)).calcularTotalKg(Collections.emptyList());
        verify(loteResiduoService, times(1)).obterMaterialMaisDescartado(Collections.emptyList());
    }

    @Test
    @DisplayName("Deve montar perfil completo formatando data e empresa do último depósito")
    void deveBuscarPerfilCompletoComHistoricoPreenchido() {
        ItemHistoricoDTO ultimoItem = mock(ItemHistoricoDTO.class);
        when(ultimoItem.dataDescarte()).thenReturn(LocalDate.of(2026, 11, 20));
        when(ultimoItem.nomeEmpresaDestino()).thenReturn("Codel Reciclagem");

        List<ItemHistoricoDTO> historico = List.of(ultimoItem);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(loteResiduoService.listarHistoricoPorUsuario(1L)).thenReturn(historico);
        when(loteResiduoService.calcularTotalKg(historico)).thenReturn(15.5);
        when(loteResiduoService.obterMaterialMaisDescartado(historico)).thenReturn("Computadores");

        var perfil = usuarioService.buscarPerfilCompletoPorId(1L);

        assertNotNull(perfil);
        verify(ultimoItem, times(2)).dataDescarte();
        verify(ultimoItem, times(1)).nomeEmpresaDestino();
        verify(loteResiduoService, times(1)).calcularTotalKg(historico);
        verify(loteResiduoService, times(1)).obterMaterialMaisDescartado(historico);
    }
}