package com.residuos.lixoeletronico.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class LoteResiduoApiClientTest {

    private MockRestServiceServer mockServer;
    private LoteResiduoApiClient apiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(
            "http://localhost:8080"
        );
        mockServer = MockRestServiceServer.bindTo(builder).build();
        apiClient = new LoteResiduoApiClient(builder.build());
    }

    @Test
    @DisplayName("Deve enviar POST /api/empresas/{id}/lotes e desserializar LoteResiduoResponse")
    void deveRegistrarRecepcaoViaHttpPost() {
        LocalDate hoje = LocalDate.now();
        LoteResiduoCreateRequest request = new LoteResiduoCreateRequest(
            "Mariana Oliveira",
            "(34) 99123-4567",
            hoje,
            CategoriaMaterial.INFORMATICA,
            "Notebooks corporativos",
            new BigDecimal("12.500"),
            UrgenciaDescarte.ALTA,
            7L
        );

        String responseJson = """
        {
          "id": 22,
          "empresaId": 1,
          "nomeUsuario": "Mariana Oliveira",
          "contatoUsuario": "(34) 99123-4567",
          "dataRecepcao": "%s",
          "categoriaMaterial": "INFORMATICA",
          "tipoResiduo": "Notebooks corporativos",
          "pesoKg": 12.500,
          "urgenciaDescarte": "ALTA",
          "status": "EM_ESTOQUE",
          "criadoEm": "2026-11-23T12:00:00Z",
          "atualizadoEm": null
        }
        """.formatted(hoje);

        mockServer
            .expect(requestTo("http://localhost:8080/api/empresas/1/lotes"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.nomeUsuario").value("Mariana Oliveira"))
            .andExpect(jsonPath("$.usuarioId").value(7))
            .andRespond(
                withStatus(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson)
            );

        LoteResiduoResponse response = apiClient.registrarRecepcao(1L, request);

        assertNotNull(response);
        assertEquals(22L, response.id());
        assertEquals(StatusLote.EM_ESTOQUE, response.status());
        assertEquals(0, new BigDecimal("12.500").compareTo(response.pesoKg()));
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve enviar GET /api/empresas/{id}/lotes/estoque com filtros de categoria e urgência")
    void deveListarLotesEmEstoqueComFiltros() {
        String responseJson = """
        [
          {
            "id": 10,
            "empresaId": 1,
            "nomeUsuario": "Carlos Lima",
            "contatoUsuario": "carlos@email.com",
            "dataRecepcao": "2026-11-22",
            "categoriaMaterial": "PILHAS_BATERIAS",
            "tipoResiduo": "Baterias",
            "pesoKg": 18.250,
            "urgenciaDescarte": "CRITICA",
            "status": "EM_ESTOQUE",
            "criadoEm": "2026-11-22T10:00:00Z",
            "atualizadoEm": null
          }
        ]
        """;

        mockServer
            .expect(
                requestTo(
                    "http://localhost:8080/api/empresas/1/lotes/estoque?categoriaMaterial=PILHAS_BATERIAS&urgenciaDescarte=CRITICA"
                )
            )
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<LoteResiduoResponse> lotes = apiClient.listarLotesEmEstoque(
            1L,
            CategoriaMaterial.PILHAS_BATERIAS,
            UrgenciaDescarte.CRITICA
        );

        assertEquals(1, lotes.size());
        assertEquals(10L, lotes.get(0).id());
        assertEquals(
            CategoriaMaterial.PILHAS_BATERIAS,
            lotes.get(0).categoriaMaterial()
        );
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve consultar perfil da empresa via GET /api/empresas/{id}/perfil")
    void deveBuscarPerfilEmpresa() {
        String responseJson = """
        {
          "id": 1,
          "nome": "CODEL Reciclagem",
          "email": "contato@codel.com",
          "contato": null,
          "endereco": null,
          "quemSomos": "Especialistas",
          "porQueReciclamos": "Sustentabilidade",
          "instrucoesDescarte": "Separar cabos",
          "horarios": [],
          "materiaisAceitos": ["INFORMATICA"],
          "totalKgDescartado": 67012.500,
          "criadoEm": "2026-11-20T10:00:00Z",
          "atualizadoEm": null
        }
        """;

        mockServer
            .expect(requestTo("http://localhost:8080/api/empresas/1/perfil"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        EmpresaResponse empresa = apiClient.buscarPerfilEmpresa(1L);

        assertEquals("CODEL Reciclagem", empresa.nome());
        assertEquals(
            0,
            new BigDecimal("67012.500").compareTo(empresa.totalKgDescartado())
        );
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve retornar Optional.empty quando buscarUsuarioPorEmail receber 404")
    void deveRetornarVazioQuandoUsuarioNaoEncontrado() {
        mockServer
            .expect(
                requestTo(
                    "http://localhost:8080/api/usuarios/buscar?email=naoexiste@email.com"
                )
            )
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<UsuarioResponse> resultado = apiClient.buscarUsuarioPorEmail(
            "naoexiste@email.com"
        );

        assertTrue(resultado.isEmpty());
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve traduzir ApiErrorResponse em ApiClientException ao receber HTTP 400")
    void deveTraduzirErroHttp400ComDetalhes() {
        String errorJson = """
        {
          "status": 400,
          "message": "Peso em kg deve ser maior que zero",
          "errors": ["Peso em kg deve ser maior que zero"],
          "timestamp": "2026-11-23T12:00:00Z"
        }
        """;

        mockServer
            .expect(requestTo("http://localhost:8080/api/empresas/1/lotes"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withBadRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson)
            );

        LoteResiduoCreateRequest requestInvalido = new LoteResiduoCreateRequest(
            "Mariana",
            "3499999",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Notebook",
            BigDecimal.ZERO,
            UrgenciaDescarte.MEDIA
        );

        ApiClientException ex = assertThrows(ApiClientException.class, () ->
            apiClient.registrarRecepcao(1L, requestInvalido)
        );

        assertEquals(400, ex.getStatusCode());
        assertEquals("Peso em kg deve ser maior que zero", ex.getMessage());
        assertEquals(1, ex.getDetails().size());
        mockServer.verify();
    }
}
