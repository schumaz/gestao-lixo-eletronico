package com.residuos.lixoeletronico.client;

import com.residuos.lixoeletronico.dto.ApiErrorResponse;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoStatusUpdateRequest;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class LoteResiduoApiClient {

    private static final ParameterizedTypeReference<List<LoteResiduoResponse>>
        LISTA_LOTES_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    @Autowired
    public LoteResiduoApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public LoteResiduoResponse registrarRecepcao(
        Long empresaId,
        LoteResiduoCreateRequest request
    ) {
        try {
            return restClient
                .post()
                .uri("/api/empresas/{empresaId}/lotes", empresaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LoteResiduoResponse.class);
        } catch (RestClientResponseException ex) {
            throw traduzirErroHttp(ex, "Não foi possível registrar a recepção.");
        } catch (ResourceAccessException ex) {
            throw erroConexao(ex);
        }
    }

    public List<LoteResiduoResponse> listarLotesEmEstoque(
        Long empresaId,
        CategoriaMaterial categoriaMaterial,
        UrgenciaDescarte urgenciaDescarte
    ) {
        try {
            List<LoteResiduoResponse> resposta = restClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/empresas/{empresaId}/lotes/estoque");
                    if (categoriaMaterial != null) {
                        uriBuilder.queryParam(
                            "categoriaMaterial",
                            categoriaMaterial.name()
                        );
                    }
                    if (urgenciaDescarte != null) {
                        uriBuilder.queryParam(
                            "urgenciaDescarte",
                            urgenciaDescarte.name()
                        );
                    }
                    return uriBuilder.build(empresaId);
                })
                .retrieve()
                .body(LISTA_LOTES_TYPE);

            return resposta != null ? resposta : List.of();
        } catch (RestClientResponseException ex) {
            throw traduzirErroHttp(
                ex,
                "Não foi possível consultar os lotes em estoque."
            );
        } catch (ResourceAccessException ex) {
            throw erroConexao(ex);
        }
    }

    public List<LoteResiduoResponse> listarLotes(
        Long empresaId,
        StatusLote status,
        CategoriaMaterial categoriaMaterial,
        UrgenciaDescarte urgenciaDescarte
    ) {
        try {
            List<LoteResiduoResponse> resposta = restClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/empresas/{empresaId}/lotes");
                    if (status != null) {
                        uriBuilder.queryParam("status", status.name());
                    }
                    if (categoriaMaterial != null) {
                        uriBuilder.queryParam(
                            "categoriaMaterial",
                            categoriaMaterial.name()
                        );
                    }
                    if (urgenciaDescarte != null) {
                        uriBuilder.queryParam(
                            "urgenciaDescarte",
                            urgenciaDescarte.name()
                        );
                    }
                    return uriBuilder.build(empresaId);
                })
                .retrieve()
                .body(LISTA_LOTES_TYPE);

            return resposta != null ? resposta : List.of();
        } catch (RestClientResponseException ex) {
            throw traduzirErroHttp(
                ex,
                "Não foi possível consultar os lotes da empresa."
            );
        } catch (ResourceAccessException ex) {
            throw erroConexao(ex);
        }
    }

    public LoteResiduoResponse atualizarStatusLote(
        Long empresaId,
        Long loteId,
        StatusLote novoStatus
    ) {
        try {
            return restClient
                .patch()
                .uri("/api/empresas/{empresaId}/lotes/{loteId}/status", empresaId, loteId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoteResiduoStatusUpdateRequest(novoStatus))
                .retrieve()
                .body(LoteResiduoResponse.class);
        } catch (RestClientResponseException ex) {
            throw traduzirErroHttp(
                ex,
                "Não foi possível atualizar o status do lote."
            );
        } catch (ResourceAccessException ex) {
            throw erroConexao(ex);
        }
    }

    public EmpresaResponse buscarPerfilEmpresa(Long empresaId) {
        try {
            return restClient
                .get()
                .uri("/api/empresas/{empresaId}/perfil", empresaId)
                .retrieve()
                .body(EmpresaResponse.class);
        } catch (RestClientResponseException ex) {
            throw traduzirErroHttp(
                ex,
                "Não foi possível consultar o perfil da empresa."
            );
        } catch (ResourceAccessException ex) {
            throw erroConexao(ex);
        }
    }

    public Optional<UsuarioResponse> buscarUsuarioPorEmail(String email) {
        try {
            UsuarioResponse usuario = restClient
                .get()
                .uri(uriBuilder ->
                    uriBuilder
                        .path("/api/usuarios/buscar")
                        .queryParam("email", email)
                        .build()
                )
                .retrieve()
                .body(UsuarioResponse.class);

            return Optional.ofNullable(usuario);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            throw traduzirErroHttp(
                ex,
                "Não foi possível consultar o usuário informado."
            );
        } catch (ResourceAccessException ex) {
            throw erroConexao(ex);
        }
    }

    private ApiClientException traduzirErroHttp(
        RestClientResponseException ex,
        String mensagemPadrao
    ) {
        int status = ex.getStatusCode().value();
        try {
            ApiErrorResponse erro = ex.getResponseBodyAs(ApiErrorResponse.class);
            if (erro != null && erro.message() != null && !erro.message().isBlank()) {
                return new ApiClientException(
                    status,
                    erro.message(),
                    erro.errors(),
                    ex
                );
            }
        } catch (Exception ignored) {
            // Corpo não está no formato ApiErrorResponse; utiliza mensagem padrão por status
        }

        String mensagem = switch (status) {
            case 400 -> "Dados inválidos enviados à API.";
            case 404 -> "Registro não encontrado no servidor.";
            case 409 -> "Conflito ao processar a requisição.";
            default -> status >= 500
                ? "Erro interno no servidor (HTTP " + status + ")."
                : mensagemPadrao;
        };

        return new ApiClientException(status, mensagem, List.of(), ex);
    }

    private ApiClientException erroConexao(ResourceAccessException ex) {
        return new ApiClientException(
            0,
            "Não foi possível conectar à API do servidor. Verifique se o sistema está em execução.",
            ex
        );
    }
}
