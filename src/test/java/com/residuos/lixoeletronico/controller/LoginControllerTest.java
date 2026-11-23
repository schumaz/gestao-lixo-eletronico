package com.residuos.lixoeletronico.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.service.EmpresaService;
import com.residuos.lixoeletronico.service.UsuarioService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class LoginControllerTest {

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
    @DisplayName("Deve carregar login.fxml e autenticar empresa redirecionando para PerfilEmpresa")
    void deveCarregarLoginEAutenticarEmpresa() throws Exception {
        EmpresaService empresaService = mock(EmpresaService.class);
        UsuarioService usuarioService = mock(UsuarioService.class);
        ApplicationContext appContext = mock(ApplicationContext.class);
        UserSession userSession = new UserSession();

        EmpresaResponse empresa = new EmpresaResponse(
            1L,
            "CODEL Reciclagem",
            "contato@codelreciclagem.com.br",
            null,
            null,
            "Quem somos",
            "Por que reciclamos",
            "Instruções",
            List.of(),
            Set.of(CategoriaMaterial.INFORMATICA),
            new BigDecimal("67000.000"),
            Instant.now(),
            null
        );

        when(usuarioService.autenticar("contato@codelreciclagem.com.br", "codel123"))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));
        when(empresaService.autenticar("contato@codelreciclagem.com.br", "codel123"))
            .thenReturn(empresa);
        when(appContext.getBean(PerfilEmpresaController.class))
            .thenReturn(new PerfilEmpresaController(userSession, appContext));

        LoginController loginController = new LoginController(
            empresaService,
            usuarioService,
            userSession,
            appContext
        );

        executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(clazz -> loginController);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            TextField txtUsuario = (TextField) root.lookup("#txtUsuario");
            PasswordField txtSenha = (PasswordField) root.lookup("#txtSenha");
            txtUsuario.setText("contato@codelreciclagem.com.br");
            txtSenha.setText("codel123");

            loginController.handleLogin();

            assertNotNull(userSession.getEmpresaLogada());
            assertEquals("CODEL Reciclagem", userSession.getEmpresaLogada().nome());
            assertNull(userSession.getUsuarioLogado());
            assertNotNull(stage.getScene().getRoot().lookup("#btnRegistrarRecepcao"));
            stage.close();
            return null;
        });
    }

    @Test
    @DisplayName("Deve exibir erro quando nem usuário nem empresa autenticarem")
    void deveExibirErroQuandoCredenciaisInvalidas() throws Exception {
        EmpresaService empresaService = mock(EmpresaService.class);
        UsuarioService usuarioService = mock(UsuarioService.class);
        ApplicationContext appContext = mock(ApplicationContext.class);
        UserSession userSession = new UserSession();

        when(usuarioService.autenticar("errado@email.com", "123"))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        when(empresaService.autenticar("errado@email.com", "123"))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        LoginController loginController = new LoginController(
            empresaService,
            usuarioService,
            userSession,
            appContext
        );

        executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(clazz -> loginController);
            Parent root = loader.load();

            TextField txtUsuario = (TextField) root.lookup("#txtUsuario");
            PasswordField txtSenha = (PasswordField) root.lookup("#txtSenha");
            Label lblMensagem = (Label) root.lookup("#lblMensagem");
            txtUsuario.setText("errado@email.com");
            txtSenha.setText("123");

            loginController.handleLogin();

            assertEquals("Usuário ou senha inválidos.", lblMensagem.getText());
            assertFalse(userSession.isAutenticado());
            return null;
        });
    }

    private static <T> T executarNoJavaFx(JavaFxCallable<T> task) throws Exception {
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

    @FunctionalInterface
    private interface JavaFxCallable<T> {
        T call() throws Exception;
    }
}
