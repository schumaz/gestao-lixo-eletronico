package com.residuos.lixoeletronico.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.service.EmpresaService;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

class RegistroRecepcaoControllerTest {

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
        "Deve carregar RegistroRecepcao.fxml, inicializar campos e carregar inventário da empresa"
    )
    void deveCarregarFxmlEInicializarInventario() throws Exception {
        LoteResiduoService loteService = mock(LoteResiduoService.class);
        EmpresaService empresaService = mock(EmpresaService.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(
            criarEmpresa(1L, new BigDecimal("100.000"))
        );

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
        when(loteService.listarLotesEmEstoque(1L, null, null)).thenReturn(
            List.of(loteExistente)
        );

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            loteService,
            empresaService,
            userSession,
            appContext
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
            ComboBox<?> cbCategoria = (ComboBox<?>) root.lookup(
                "#cbCategoriaMaterial"
            );
            ComboBox<?> cbUrgencia = (ComboBox<?>) root.lookup(
                "#cbUrgenciaDescarte"
            );
            DatePicker dpData = (DatePicker) root.lookup("#dpDataRecepcao");
            TableView<?> tabela = (TableView<?>) root.lookup(
                "#tblLotesEstoque"
            );

            assertEquals("100 kg", lblMassaTotal.getText());
            assertEquals("18.5 kg", lblMassaEstoque.getText());
            assertEquals(
                CategoriaMaterial.values().length,
                cbCategoria.getItems().size()
            );
            assertEquals(
                UrgenciaDescarte.values().length,
                cbUrgencia.getItems().size()
            );
            assertEquals(UrgenciaDescarte.MEDIA, cbUrgencia.getValue());
            assertEquals(LocalDate.now(), dpData.getValue());
            assertEquals(1, tabela.getItems().size());
            return null;
        });
    }

    @Test
    @DisplayName(
        "Deve validar formulário e registrar recepção atualizando massa total, estoque e sessão"
    )
    @SuppressWarnings("unchecked")
    void deveValidarERegistrarRecepcaoComSucesso() throws Exception {
        LoteResiduoService loteService = mock(LoteResiduoService.class);
        EmpresaService empresaService = mock(EmpresaService.class);
        ApplicationContext appContext = mock(ApplicationContext.class);

        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(
            criarEmpresa(1L, new BigDecimal("50.000"))
        );

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

        when(loteService.listarLotesEmEstoque(1L, null, null))
            .thenReturn(List.of())
            .thenReturn(List.of(loteSalvo));
        when(
            loteService.registrarRecepcao(
                eq(1L),
                any(LoteResiduoCreateRequest.class)
            )
        ).thenReturn(loteSalvo);
        when(empresaService.buscarPerfilPorId(1L)).thenReturn(
            criarEmpresa(1L, new BigDecimal("62.500"))
        );

        RegistroRecepcaoController controller = new RegistroRecepcaoController(
            loteService,
            empresaService,
            userSession,
            appContext
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
            verify(loteService, never()).registrarRecepcao(any(), any());

            // 2. Validação de data futura e peso inválido
            txtNome.setText("Mariana Oliveira");
            txtContato.setText("(34) 99123-4567");
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
            verify(loteService, never()).registrarRecepcao(any(), any());

            // 3. Preenchimento válido com separador decimal vírgula
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
            assertEquals(
                0,
                new BigDecimal("62.500").compareTo(
                    userSession.getEmpresaLogada().totalKgDescartado()
                )
            );

            // 4. Botão Limpar deve resetar mensagem e campos
            txtNome.setText("Teste");
            controller.handleLimpar();
            assertEquals("", txtNome.getText());
            assertEquals("", lblMensagem.getText());
            return null;
        });

        verify(loteService, times(1)).registrarRecepcao(
            eq(1L),
            any(LoteResiduoCreateRequest.class)
        );
        verify(empresaService, times(1)).buscarPerfilPorId(1L);
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
