package com.residuos.lixoeletronico.client;

import static org.junit.jupiter.api.Assertions.*;

import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.Usuario;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.LoteResiduoRepository;
import com.residuos.lixoeletronico.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoteResiduoApiClientIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoteResiduoRepository loteResiduoRepository;

    private LoteResiduoApiClient apiClient;
    private Long empresaCriadaId;
    private Long usuarioCriadoId;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .build();
        apiClient = new LoteResiduoApiClient(restClient);
    }

    @AfterEach
    void tearDown() {
        if (empresaCriadaId != null) {
            loteResiduoRepository.deleteAll(
                loteResiduoRepository
                    .findAll()
                    .stream()
                    .filter(l ->
                        l.getEmpresa() != null &&
                        empresaCriadaId.equals(l.getEmpresa().getId())
                    )
                    .toList()
            );
            empresaRepository.deleteById(empresaCriadaId);
            empresaCriadaId = null;
        }
        if (usuarioCriadoId != null) {
            usuarioRepository.deleteById(usuarioCriadoId);
            usuarioCriadoId = null;
        }
    }

    @Test
    @DisplayName(
        "Deve integrar cliente HTTP com endpoints reais de busca de usuário, registro, listagem com filtros e perfil"
    )
    void deveIntegrarClienteHttpPontaAPontaComApiReal() {
        Empresa empresa = empresaRepository.save(
            new Empresa(
                null,
                "Eco Integracao HTTP",
                "senha123",
                "http.integracao@recicla.com"
            )
        );
        empresaCriadaId = empresa.getId();

        Usuario usuario = new Usuario();
        usuario.setNome("Beatriz Santos");
        usuario.setCpf("12345678901");
        usuario.setEmail("beatriz.http@email.com");
        usuario.setSenha("senha123");
        usuario.setTelefone("(34) 99888-7766");
        usuario = usuarioRepository.save(usuario);
        usuarioCriadoId = usuario.getId();

        Optional<UsuarioResponse> usuarioBuscado =
            apiClient.buscarUsuarioPorEmail("beatriz.http@email.com");
        assertTrue(usuarioBuscado.isPresent());
        assertEquals(usuarioCriadoId, usuarioBuscado.get().id());

        assertTrue(
            apiClient.buscarUsuarioPorEmail("inexistente@email.com").isEmpty()
        );

        LoteResiduoResponse lote1 = apiClient.registrarRecepcao(
            empresaCriadaId,
            new LoteResiduoCreateRequest(
                usuarioBuscado.get().nome(),
                usuarioBuscado.get().telefone(),
                LocalDate.now(),
                CategoriaMaterial.INFORMATICA,
                "Servidores antigos",
                new BigDecimal("25.500"),
                UrgenciaDescarte.ALTA,
                usuarioBuscado.get().id()
            )
        );

        LoteResiduoResponse lote2 = apiClient.registrarRecepcao(
            empresaCriadaId,
            new LoteResiduoCreateRequest(
                "Marcos Paulo",
                "(34) 99111-0000",
                LocalDate.now(),
                CategoriaMaterial.PILHAS_BATERIAS,
                "Baterias automotivas pequenas",
                new BigDecimal("4.500"),
                UrgenciaDescarte.CRITICA
            )
        );

        assertNotNull(lote1.id());
        assertNotNull(lote2.id());
        assertEquals(StatusLote.EM_ESTOQUE, lote1.status());

        List<LoteResiduoResponse> todos = apiClient.listarLotesEmEstoque(
            empresaCriadaId,
            null,
            null
        );
        assertEquals(2, todos.size());

        List<LoteResiduoResponse> filtrados = apiClient.listarLotesEmEstoque(
            empresaCriadaId,
            CategoriaMaterial.PILHAS_BATERIAS,
            UrgenciaDescarte.CRITICA
        );
        assertEquals(1, filtrados.size());
        assertEquals(lote2.id(), filtrados.get(0).id());

        EmpresaResponse perfilAtualizado = apiClient.buscarPerfilEmpresa(
            empresaCriadaId
        );
        assertEquals(
            0,
            new BigDecimal("30.000").compareTo(
                perfilAtualizado.totalKgDescartado()
            )
        );

        ApiClientException erroValidacao = assertThrows(
            ApiClientException.class,
            () ->
                apiClient.registrarRecepcao(
                    empresaCriadaId,
                    new LoteResiduoCreateRequest(
                        "",
                        "",
                        LocalDate.now(),
                        null,
                        "",
                        BigDecimal.ZERO,
                        null
                    )
                )
        );
        assertEquals(400, erroValidacao.getStatusCode());
        assertFalse(erroValidacao.getDetails().isEmpty());

        // Atualização de status no ciclo de vida (EM_ESTOQUE -> EM_PROCESSAMENTO -> DESTINADO)
        LoteResiduoResponse lote1Processando = apiClient.atualizarStatusLote(
            empresaCriadaId,
            lote1.id(),
            StatusLote.EM_PROCESSAMENTO
        );
        assertEquals(StatusLote.EM_PROCESSAMENTO, lote1Processando.status());

        LoteResiduoResponse lote1Destinado = apiClient.atualizarStatusLote(
            empresaCriadaId,
            lote1.id(),
            StatusLote.DESTINADO
        );
        assertEquals(StatusLote.DESTINADO, lote1Destinado.status());

        // Transição inválida (EM_ESTOQUE -> DESTINADO direto) deve retornar 409
        ApiClientException erroTransicao = assertThrows(
            ApiClientException.class,
            () ->
                apiClient.atualizarStatusLote(
                    empresaCriadaId,
                    lote2.id(),
                    StatusLote.DESTINADO
                )
        );
        assertEquals(409, erroTransicao.getStatusCode());

        // Consulta com filtro por status
        List<LoteResiduoResponse> destinados = apiClient.listarLotes(
            empresaCriadaId,
            StatusLote.DESTINADO,
            null,
            null
        );
        assertEquals(1, destinados.size());
        assertEquals(lote1.id(), destinados.get(0).id());

        List<LoteResiduoResponse> todosStatus = apiClient.listarLotes(
            empresaCriadaId,
            null,
            null,
            null
        );
        assertEquals(2, todosStatus.size());
    }
}
