package com.residuos.lixoeletronico.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.residuos.lixoeletronico.client.ApiClientException;
import com.residuos.lixoeletronico.client.LoteResiduoApiClient;
import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;

class RegistroRecepcaoControllerTest {

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    @BeforeAll
    static void inicializarJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            assertTrue(
                latch.await(10, TimeUnit.SECONDS),
                "JavaFX Toolkit não iniciou no tempo esperado"
            );
        } catch (IllegalStateException ignored) {
            // Toolkit já inicializado
        }
        Platform.setImplicitExit(false);
    }

    @Test
    @DisplayName(
        "Deve carregar RegistroRecepcao.fxml, inicializar campos/filtros e carregar inventário pela API"
    )
    void deveCarregarFxmlEInicializarInventarioViaApi() throws Exception {
        LoteResiduoApiClient apiClient = mock(LoteResiduoApiClient.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        EmpresaResponse empresaAtualizada = criarEmpresa(
            1L,
            new BigDecimal("120.000")
        );
        userSession.setEmpresaLogada(criarEmpresa(1L, new BigDecimal("100.000")));

        LoteResiduoResponse loteExistente = new LoteResiduoResponse(
            10L,
            1L,
            "Carlos Lima",
            "carlos@email.com",
            LocalDate.now(),
            CategoriaMaterial.PILHAS_BATERIAS,
            "Baterias",
            new BigDecimal("18.500"),
            UrgenciaDescarte.CRITICA,
            StatusLote.EM_ESTOQUE,
            Instant.now(),
            null
        );
        when(apiClient.buscarPerfilEmpresa(1L)).thenReturn(empresaAtualizada);
        when(apiClient.listarLotes(1L, null, null, null)).thenReturn(
            List.of(loteExistente)
        );

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            apiClient,
            userSession,
            appContext,
            DIRECT_EXECUTOR
        );

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/RegistroRecepcao.fxml")
            );
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        assertNotNull(root);
        executarNoJavaFx(() -> {
            Label lblMassaTotal = (Label) root.lookup("#lblMassaTotalEmpresa");
            Label lblMassaEstoque = (Label) root.lookup("#lblMassaEmEstoque");
            Label lblTituloEstoque = (Label) root.lookup(
                "#lblTituloMassaEstoque"
            );
            ComboBox<?> cbCategoria = (ComboBox<?>) root.lookup(
                "#cbCategoriaMaterial"
            );
            ComboBox<?> cbUrgencia = (ComboBox<?>) root.lookup(
                "#cbUrgenciaDescarte"
            );
            ComboBox<?> cbFiltroStatus = (ComboBox<?>) root.lookup(
                "#cbFiltroStatus"
            );
            ComboBox<?> cbFiltroCategoria = (ComboBox<?>) root.lookup(
                "#cbFiltroCategoria"
            );
            ComboBox<?> cbFiltroUrgencia = (ComboBox<?>) root.lookup(
                "#cbFiltroUrgencia"
            );
            DatePicker dpData = (DatePicker) root.lookup("#dpDataRecepcao");
            TableView<?> tabela = (TableView<?>) root.lookup(
                "#tblLotesEstoque"
            );
            Button btnAvancarStatus = (Button) root.lookup(
                "#btnAvancarStatus"
            );

            assertEquals("120 kg", lblMassaTotal.getText());
            assertEquals("18.5 kg", lblMassaEstoque.getText());
            assertEquals("EM ESTOQUE ATUAL", lblTituloEstoque.getText());
            assertEquals(
                CategoriaMaterial.values().length,
                cbCategoria.getItems().size()
            );
            assertEquals(
                UrgenciaDescarte.values().length,
                cbUrgencia.getItems().size()
            );
            assertEquals(
                StatusLote.values().length + 1,
                cbFiltroStatus.getItems().size()
            );
            assertNull(cbFiltroStatus.getValue());
            assertEquals(
                CategoriaMaterial.values().length + 1,
                cbFiltroCategoria.getItems().size()
            );
            assertEquals(
                UrgenciaDescarte.values().length + 1,
                cbFiltroUrgencia.getItems().size()
            );
            assertEquals(UrgenciaDescarte.MEDIA, cbUrgencia.getValue());
            assertEquals(LocalDate.now(), dpData.getValue());
            assertEquals(1, tabela.getItems().size());
            assertEquals(6, tabela.getColumns().size());
            assertTrue(btnAvancarStatus.isDisable());
            return null;
        });
    }

    @Test
    @DisplayName(
        "Deve buscar usuário por e-mail, validar formulário e registrar recepção pela API atualizando massa e sessão"
    )
    @SuppressWarnings("unchecked")
    void deveVincularUsuarioValidarERegistrarRecepcaoComSucesso()
        throws Exception {
        LoteResiduoApiClient apiClient = mock(LoteResiduoApiClient.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(1L, new BigDecimal("50.000")));

        LoteResiduoResponse loteSalvo = new LoteResiduoResponse(
            22L,
            1L,
            "Mariana Oliveira",
            "(34) 99123-4567",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Notebooks corporativos",
            new BigDecimal("12.500"),
            UrgenciaDescarte.ALTA,
            StatusLote.EM_ESTOQUE,
            Instant.now(),
            null
        );

        UsuarioResponse usuarioEncontrado = new UsuarioResponse(
            7L,
            "Mariana Oliveira",
            "mariana@email.com",
            "(34) 99123-4567",
            null,
            null
        );

        when(apiClient.buscarPerfilEmpresa(1L))
            .thenReturn(criarEmpresa(1L, new BigDecimal("50.000")))
            .thenReturn(criarEmpresa(1L, new BigDecimal("62.500")));
        when(apiClient.listarLotes(1L, null, null, null))
            .thenReturn(List.of())
            .thenReturn(List.of(loteSalvo));
        when(apiClient.buscarUsuarioPorEmail("mariana@email.com")).thenReturn(
            Optional.of(usuarioEncontrado)
        );
        when(
            apiClient.registrarRecepcao(
                eq(1L),
                any(LoteResiduoCreateRequest.class)
            )
        ).thenReturn(loteSalvo);

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            apiClient,
            userSession,
            appContext,
            DIRECT_EXECUTOR
        );

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/RegistroRecepcao.fxml")
            );
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            Label lblMensagem = (Label) root.lookup("#lblMensagem");
            Label lblUsuarioVinculado = (Label) root.lookup(
                "#lblUsuarioVinculado"
            );
            TextField txtBuscarEmail = (TextField) root.lookup(
                "#txtBuscarEmailUsuario"
            );
            TextField txtNome = (TextField) root.lookup("#txtNomeUsuario");
            TextField txtContato = (TextField) root.lookup(
                "#txtContatoUsuario"
            );
            DatePicker dpData = (DatePicker) root.lookup("#dpDataRecepcao");
            ComboBox<CategoriaMaterial> cbCategoria =
                (ComboBox<CategoriaMaterial>) root.lookup(
                    "#cbCategoriaMaterial"
                );
            ComboBox<UrgenciaDescarte> cbUrgencia =
                (ComboBox<UrgenciaDescarte>) root.lookup("#cbUrgenciaDescarte");
            TextField txtTipo = (TextField) root.lookup("#txtTipoResiduo");
            TextField txtPeso = (TextField) root.lookup("#txtPesoKg");
            Label lblMassaTotal = (Label) root.lookup("#lblMassaTotalEmpresa");
            Label lblMassaEstoque = (Label) root.lookup("#lblMassaEmEstoque");
            TableView<?> tabela = (TableView<?>) root.lookup(
                "#tblLotesEstoque"
            );

            // 1. Tentativa inválida (campos vazios)
            controller.handleRegistrarRecepcao();
            assertEquals(
                "Informe o nome do usuário entregador.",
                lblMensagem.getText()
            );
            verify(apiClient, never()).registrarRecepcao(any(), any());

            // 2. Busca e vínculo de usuário cadastrado via API
            txtBuscarEmail.setText("mariana@email.com");
            controller.handleBuscarUsuario();
            assertEquals("Mariana Oliveira", txtNome.getText());
            assertEquals("(34) 99123-4567", txtContato.getText());
            assertTrue(lblUsuarioVinculado.isVisible());

            // 3. Validação de data futura e peso inválido
            dpData.setValue(LocalDate.now().plusDays(1));
            cbCategoria.setValue(CategoriaMaterial.INFORMATICA);
            cbUrgencia.setValue(UrgenciaDescarte.ALTA);
            txtTipo.setText("Notebooks corporativos");
            txtPeso.setText("0");

            controller.handleRegistrarRecepcao();
            assertEquals(
                "Data de recepção não pode ser futura.",
                lblMensagem.getText()
            );

            dpData.setValue(LocalDate.now());
            controller.handleRegistrarRecepcao();
            assertEquals(
                "Peso deve ser maior que zero (mínimo 0.001 kg).",
                lblMensagem.getText()
            );
            verify(apiClient, never()).registrarRecepcao(any(), any());

            // 4. Preenchimento válido com separador decimal vírgula
            txtPeso.setText("12,500");

            controller.handleRegistrarRecepcao();

            assertTrue(
                lblMensagem
                    .getText()
                    .contains("Lote #22 registrado com sucesso")
            );
            assertEquals("62.5 kg", lblMassaTotal.getText());
            assertEquals("12.5 kg", lblMassaEstoque.getText());
            assertEquals(1, tabela.getItems().size());
            assertEquals("", txtNome.getText());
            assertEquals("", txtPeso.getText());
            assertFalse(lblUsuarioVinculado.isVisible());
            assertEquals(
                0,
                new BigDecimal("62.500").compareTo(
                    userSession.getEmpresaLogada().totalKgDescartado()
                )
            );

            // 5. Botão Limpar deve resetar mensagem e campos
            txtNome.setText("Teste");
            controller.handleLimpar();
            assertEquals("", txtNome.getText());
            assertEquals("", lblMensagem.getText());
            return null;
        });

        ArgumentCaptor<LoteResiduoCreateRequest> captor = ArgumentCaptor.forClass(
            LoteResiduoCreateRequest.class
        );
        verify(apiClient, times(1)).registrarRecepcao(eq(1L), captor.capture());
        assertEquals(7L, captor.getValue().usuarioId());
        assertEquals("Mariana Oliveira", captor.getValue().nomeUsuario());
        verify(apiClient, times(2)).buscarPerfilEmpresa(1L);
    }

    @Test
    @DisplayName(
        "Deve filtrar lotes pela API por status, categoria e urgência e permitir limpar filtros"
    )
    @SuppressWarnings("unchecked")
    void deveFiltrarELimparFiltrosEstoqueViaApi() throws Exception {
        LoteResiduoApiClient apiClient = mock(LoteResiduoApiClient.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(1L, new BigDecimal("80.000")));

        LoteResiduoResponse loteCritico = new LoteResiduoResponse(
            15L,
            1L,
            "João Silva",
            "joao@email.com",
            LocalDate.now(),
            CategoriaMaterial.PILHAS_BATERIAS,
            "Baterias inchadas",
            new BigDecimal("5.500"),
            UrgenciaDescarte.CRITICA,
            StatusLote.EM_ESTOQUE,
            Instant.now(),
            null
        );
        LoteResiduoResponse loteProcessando = new LoteResiduoResponse(
            16L,
            1L,
            "Ana Paula",
            "ana@email.com",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Teclados",
            new BigDecimal("10.000"),
            UrgenciaDescarte.BAIXA,
            StatusLote.EM_PROCESSAMENTO,
            Instant.now(),
            null
        );

        when(apiClient.buscarPerfilEmpresa(1L)).thenReturn(
            criarEmpresa(1L, new BigDecimal("80.000"))
        );
        when(apiClient.listarLotes(1L, null, null, null)).thenReturn(
            List.of(loteCritico, loteProcessando)
        );
        when(
            apiClient.listarLotes(
                1L,
                StatusLote.EM_PROCESSAMENTO,
                CategoriaMaterial.INFORMATICA,
                UrgenciaDescarte.BAIXA
            )
        ).thenReturn(List.of(loteProcessando));

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            apiClient,
            userSession,
            appContext,
            DIRECT_EXECUTOR
        );

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/RegistroRecepcao.fxml")
            );
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            ComboBox<StatusLote> cbFiltroStatus =
                (ComboBox<StatusLote>) root.lookup("#cbFiltroStatus");
            ComboBox<CategoriaMaterial> cbFiltroCategoria =
                (ComboBox<CategoriaMaterial>) root.lookup("#cbFiltroCategoria");
            ComboBox<UrgenciaDescarte> cbFiltroUrgencia =
                (ComboBox<UrgenciaDescarte>) root.lookup("#cbFiltroUrgencia");
            Label lblMassaEstoque = (Label) root.lookup("#lblMassaEmEstoque");
            Label lblTituloEstoque = (Label) root.lookup(
                "#lblTituloMassaEstoque"
            );
            TableView<?> tabela = (TableView<?>) root.lookup(
                "#tblLotesEstoque"
            );

            assertEquals(2, tabela.getItems().size());
            assertEquals("5.5 kg", lblMassaEstoque.getText());
            assertEquals("EM ESTOQUE ATUAL", lblTituloEstoque.getText());

            cbFiltroStatus.setValue(StatusLote.EM_PROCESSAMENTO);
            cbFiltroCategoria.setValue(CategoriaMaterial.INFORMATICA);
            cbFiltroUrgencia.setValue(UrgenciaDescarte.BAIXA);
            controller.handleFiltrarEstoque();

            assertEquals(1, tabela.getItems().size());
            assertEquals("10 kg", lblMassaEstoque.getText());
            assertEquals(
                "EM PROCESSAMENTO (FILTRADO)",
                lblTituloEstoque.getText()
            );

            controller.handleLimparFiltros();

            assertNull(cbFiltroStatus.getValue());
            assertNull(cbFiltroCategoria.getValue());
            assertNull(cbFiltroUrgencia.getValue());
            assertEquals(2, tabela.getItems().size());
            assertEquals("5.5 kg", lblMassaEstoque.getText());
            assertEquals("EM ESTOQUE ATUAL", lblTituloEstoque.getText());
            return null;
        });

        verify(apiClient, times(1)).listarLotes(
            1L,
            StatusLote.EM_PROCESSAMENTO,
            CategoriaMaterial.INFORMATICA,
            UrgenciaDescarte.BAIXA
        );
        verify(apiClient, times(2)).listarLotes(1L, null, null, null);
    }

    @Test
    @DisplayName(
        "Deve avançar status do lote selecionado (Em Estoque -> Em Processamento -> Destinado) e atualizar balanço em estoque"
    )
    @SuppressWarnings("unchecked")
    void deveAvancarStatusDoLoteSelecionadoPelaApi() throws Exception {
        LoteResiduoApiClient apiClient = mock(LoteResiduoApiClient.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(1L, new BigDecimal("50.000")));

        LoteResiduoResponse loteEstoque = new LoteResiduoResponse(
            10L,
            1L,
            "Mariana",
            "3499999",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Notebooks",
            new BigDecimal("20.000"),
            UrgenciaDescarte.MEDIA,
            StatusLote.EM_ESTOQUE,
            Instant.now(),
            null
        );
        LoteResiduoResponse loteProcessando = new LoteResiduoResponse(
            10L,
            1L,
            "Mariana",
            "3499999",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Notebooks",
            new BigDecimal("20.000"),
            UrgenciaDescarte.MEDIA,
            StatusLote.EM_PROCESSAMENTO,
            Instant.now(),
            Instant.now()
        );
        LoteResiduoResponse loteDestinado = new LoteResiduoResponse(
            10L,
            1L,
            "Mariana",
            "3499999",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Notebooks",
            new BigDecimal("20.000"),
            UrgenciaDescarte.MEDIA,
            StatusLote.DESTINADO,
            Instant.now(),
            Instant.now()
        );

        when(apiClient.buscarPerfilEmpresa(1L)).thenReturn(
            criarEmpresa(1L, new BigDecimal("50.000"))
        );
        when(apiClient.listarLotes(1L, null, null, null))
            .thenReturn(List.of(loteEstoque))
            .thenReturn(List.of(loteProcessando))
            .thenReturn(List.of(loteDestinado));
        when(
            apiClient.atualizarStatusLote(
                1L,
                10L,
                StatusLote.EM_PROCESSAMENTO
            )
        ).thenReturn(loteProcessando);
        when(apiClient.atualizarStatusLote(1L, 10L, StatusLote.DESTINADO))
            .thenReturn(loteDestinado);

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            apiClient,
            userSession,
            appContext,
            DIRECT_EXECUTOR
        );

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/RegistroRecepcao.fxml")
            );
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            TableView<LoteResiduoResponse> tabela =
                (TableView<LoteResiduoResponse>) root.lookup("#tblLotesEstoque");
            Button btnAvancarStatus = (Button) root.lookup(
                "#btnAvancarStatus"
            );
            Label lblLoteSelecionado = (Label) root.lookup(
                "#lblLoteSelecionado"
            );
            Label lblMassaEstoque = (Label) root.lookup("#lblMassaEmEstoque");
            Label lblMensagem = (Label) root.lookup("#lblMensagem");

            assertTrue(btnAvancarStatus.isDisable());
            assertEquals("20 kg", lblMassaEstoque.getText());

            // Seleciona lote EM_ESTOQUE -> botão habilita "Iniciar Processamento"
            tabela.getSelectionModel().select(0);
            assertFalse(btnAvancarStatus.isDisable());
            assertEquals("Iniciar Processamento", btnAvancarStatus.getText());
            assertTrue(lblLoteSelecionado.getText().contains("Em Estoque"));

            // Avança para EM_PROCESSAMENTO
            controller.handleAvancarStatus();
            assertEquals("0 kg", lblMassaEstoque.getText());
            assertFalse(btnAvancarStatus.isDisable());
            assertEquals("Marcar como Destinado", btnAvancarStatus.getText());
            assertTrue(
                lblMensagem.getText().contains("Em Processamento")
            );

            // Avança para DESTINADO
            controller.handleAvancarStatus();
            assertTrue(btnAvancarStatus.isDisable());
            assertEquals("Lote Destinado", btnAvancarStatus.getText());
            assertTrue(lblMensagem.getText().contains("Destinado"));
            return null;
        });

        verify(apiClient, times(1)).atualizarStatusLote(
            1L,
            10L,
            StatusLote.EM_PROCESSAMENTO
        );
        verify(apiClient, times(1)).atualizarStatusLote(
            1L,
            10L,
            StatusLote.DESTINADO
        );
    }

    @Test
    @DisplayName(
        "Deve exibir erro e manter controles consistentes quando falhar a atualização de status na API"
    )
    @SuppressWarnings("unchecked")
    void deveExibirErroQuandoAtualizarStatusFalhar() throws Exception {
        LoteResiduoApiClient apiClient = mock(LoteResiduoApiClient.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(1L, new BigDecimal("50.000")));

        LoteResiduoResponse loteEstoque = new LoteResiduoResponse(
            10L,
            1L,
            "Mariana",
            "3499999",
            LocalDate.now(),
            CategoriaMaterial.INFORMATICA,
            "Notebooks",
            new BigDecimal("20.000"),
            UrgenciaDescarte.MEDIA,
            StatusLote.EM_ESTOQUE,
            Instant.now(),
            null
        );

        when(apiClient.buscarPerfilEmpresa(1L)).thenReturn(
            criarEmpresa(1L, new BigDecimal("50.000"))
        );
        when(apiClient.listarLotes(1L, null, null, null)).thenReturn(
            List.of(loteEstoque)
        );
        when(
            apiClient.atualizarStatusLote(
                1L,
                10L,
                StatusLote.EM_PROCESSAMENTO
            )
        ).thenThrow(
            new ApiClientException(409, "Transição de status inválida.")
        );

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            apiClient,
            userSession,
            appContext,
            DIRECT_EXECUTOR
        );

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/RegistroRecepcao.fxml")
            );
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            TableView<LoteResiduoResponse> tabela =
                (TableView<LoteResiduoResponse>) root.lookup("#tblLotesEstoque");
            Button btnAvancarStatus = (Button) root.lookup(
                "#btnAvancarStatus"
            );
            Label lblMensagem = (Label) root.lookup("#lblMensagem");

            tabela.getSelectionModel().select(0);
            controller.handleAvancarStatus();

            assertTrue(
                lblMensagem.getText().contains("Transição de status inválida.")
            );
            assertFalse(btnAvancarStatus.isDisable());
            return null;
        });
    }

    @Test
    @DisplayName(
        "Deve exibir mensagem amigável e reabilitar botões quando a API falhar ao registrar"
    )
    @SuppressWarnings("unchecked")
    void deveExibirErroQuandoApiFalhar() throws Exception {
        LoteResiduoApiClient apiClient = mock(LoteResiduoApiClient.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(1L, new BigDecimal("50.000")));

        when(apiClient.buscarPerfilEmpresa(1L)).thenReturn(
            criarEmpresa(1L, new BigDecimal("50.000"))
        );
        when(apiClient.listarLotes(1L, null, null, null)).thenReturn(
            List.of()
        );
        when(
            apiClient.registrarRecepcao(
                eq(1L),
                any(LoteResiduoCreateRequest.class)
            )
        ).thenThrow(
            new ApiClientException(
                0,
                "Não foi possível conectar à API do servidor."
            )
        );

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            apiClient,
            userSession,
            appContext,
            DIRECT_EXECUTOR
        );

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/RegistroRecepcao.fxml")
            );
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            Label lblMensagem = (Label) root.lookup("#lblMensagem");
            Button btnRegistrar = (Button) root.lookup("#btnRegistrar");
            TextField txtNome = (TextField) root.lookup("#txtNomeUsuario");
            TextField txtContato = (TextField) root.lookup(
                "#txtContatoUsuario"
            );
            ComboBox<CategoriaMaterial> cbCategoria =
                (ComboBox<CategoriaMaterial>) root.lookup(
                    "#cbCategoriaMaterial"
                );
            ComboBox<UrgenciaDescarte> cbUrgencia =
                (ComboBox<UrgenciaDescarte>) root.lookup("#cbUrgenciaDescarte");
            TextField txtTipo = (TextField) root.lookup("#txtTipoResiduo");
            TextField txtPeso = (TextField) root.lookup("#txtPesoKg");

            txtNome.setText("Mariana Oliveira");
            txtContato.setText("(34) 99123-4567");
            cbCategoria.setValue(CategoriaMaterial.INFORMATICA);
            cbUrgencia.setValue(UrgenciaDescarte.MEDIA);
            txtTipo.setText("Notebooks");
            txtPeso.setText("5.000");

            controller.handleRegistrarRecepcao();

            assertTrue(
                lblMensagem
                    .getText()
                    .contains("Não foi possível conectar à API do servidor.")
            );
            assertFalse(btnRegistrar.isDisable());
            return null;
        });
    }

    @Test
    @DisplayName(
        "Deve carregar PerfilEmpresa.fxml com botão de acesso ao Registro de Recepção"
    )
    void deveCarregarPerfilEmpresaComBotaoRegistrarRecepcao() throws Exception {
        ApplicationContext appContext = mock(ApplicationContext.class);
        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(
            criarEmpresa(1L, new BigDecimal("1000.000"))
        );

        PerfilEmpresaController perfilController = new PerfilEmpresaController(
            userSession,
            appContext
        );

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/PerfilEmpresa.fxml")
            );
            loader.setControllerFactory(clazz -> perfilController);
            return loader.load();
        });

        assertNotNull(root.lookup("#btnRegistrarRecepcao"));
    }

    private static EmpresaResponse criarEmpresa(Long id, BigDecimal totalKg) {
        return new EmpresaResponse(
            id,
            "CODEL Reciclagem",
            "contato@codel.com",
            null,
            null,
            "Quem somos",
            "Por que reciclamos",
            "Instruções",
            List.of(),
            Set.of(CategoriaMaterial.INFORMATICA),
            totalKg,
            Instant.now(),
            null
        );
    }

    @FunctionalInterface
    private interface JavaFxCallable<T> {
        T call() throws Exception;
    }

    private static <T> T executarNoJavaFx(
        JavaFxCallable<T> task
    ) throws Exception {
        CompletableFuture<T> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future.get(10, TimeUnit.SECONDS);
    }
}
