package com.residuos.lixoeletronico.service;

import com.residuos.lixoeletronico.dto.empresa.EmpresaCreateRequest;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository repository;

    @InjectMocks
    private EmpresaService service;

    @Test
    @DisplayName("Deve criar nova empresa com sucesso quando e-mail não existir")
    void deveCriarEmpresaComSucesso() {
        EmpresaCreateRequest request = new EmpresaCreateRequest("Eco Nova", "nova@eco.com", "senha123", null, null);

        when(repository.existsByEmail("nova@eco.com")).thenReturn(false);
        when(repository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa e = invocation.getArgument(0);
            e.setId(10L);
            return e;
        });

        EmpresaResponse response = service.criarEmpresa(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Eco Nova", response.nome());
        assertEquals("nova@eco.com", response.email());
        verify(repository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Deve lançar 409 Conflict ao tentar cadastrar empresa com e-mail já existente")
    void deveLancarConflictQuandoEmailJaExiste() {
        EmpresaCreateRequest request = new EmpresaCreateRequest("Eco Duplicada", "jaexiste@eco.com", "senha123", null, null);

        when(repository.existsByEmail("jaexiste@eco.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.criarEmpresa(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(repository, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Deve buscar perfil por ID com sucesso")
    void deveBuscarPerfilPorIdComSucesso() {
        Empresa empresa = new Empresa(1L, "Codel Reciclagem", "codel123", "contato@codel.com");
        when(repository.findPerfilById(1L)).thenReturn(Optional.of(empresa));

        EmpresaResponse response = service.buscarPerfilPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Codel Reciclagem", response.nome());
    }

    @Test
    @DisplayName("Deve lançar 404 Not Found quando ID da empresa não existir")
    void deveLancarNotFoundQuandoIdNaoExiste() {
        when(repository.findPerfilById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.buscarPerfilPorId(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve autenticar empresa com sucesso com credenciais válidas")
    void deveAutenticarComSucesso() {
        Empresa empresa = new Empresa(1L, "Empresa Modelo", "senhaCorreta", "usuario@modelo.com");
        when(repository.findByEmail("usuario@modelo.com")).thenReturn(Optional.of(empresa));
        when(repository.findPerfilById(1L)).thenReturn(Optional.of(empresa));

        EmpresaResponse response = service.autenticar("usuario@modelo.com", "senhaCorreta");

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("usuario@modelo.com", response.email());
    }

    @Test
    @DisplayName("Deve autenticar com sucesso usando o alias 'admin@admin' mapeando para 'admin@admin.com'")
    void deveAutenticarComAliasAdmin() {
        Empresa admin = new Empresa(2L, "Eco Admin", "admin", "admin@admin.com");
        when(repository.findByEmail("admin@admin.com")).thenReturn(Optional.of(admin));
        when(repository.findPerfilById(2L)).thenReturn(Optional.of(admin));

        EmpresaResponse response = service.autenticar("admin@admin", "admin");

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("admin@admin.com", response.email());
    }

    @Test
    @DisplayName("Deve lançar 401 Unauthorized quando o e-mail não existir")
    void deveLancarUnauthorizedQuandoEmailNaoExiste() {
        when(repository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.autenticar("inexistente@email.com", "qualquerSenha"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar 401 Unauthorized quando a senha estiver incorreta")
    void deveLancarUnauthorizedQuandoSenhaIncorreta() {
        Empresa empresa = new Empresa(1L, "Empresa", "senhaCerta", "user@email.com");
        when(repository.findByEmail("user@email.com")).thenReturn(Optional.of(empresa));
        when(repository.findPerfilById(1L)).thenReturn(Optional.of(empresa));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.autenticar("user@email.com", "senhaErrada"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
