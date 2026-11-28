package com.residuos.lixoeletronico.controller;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.avaliacao.AvaliacaoResponse;
import com.residuos.lixoeletronico.dto.avaliacao.ResumoAvaliacaoResponse;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.service.AvaliacaoService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilEmpresaControllerTest {

    @BeforeAll
    static void inicializarJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX Toolkit não iniciou no tempo esperado");
        } catch (IllegalStateException ignored) {
            // Toolkit já inicializado
        }
        Platform.setImplicitExit(false);
    }

    @Test
    @DisplayName("Deve exibir nota média, estrelas, contagem e avaliações recentes do centro de coleta")
    void deveExibirNotaMediaEAvaliacoesRecentes() throws Exception {
        ApplicationContext appContext = mock(ApplicationContext.class);
        AvaliacaoService avaliacaoService = mock(AvaliacaoService.class);
        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(1L));

        List<AvaliacaoResponse> avaliacoes = List.of(
                new AvaliacaoResponse(1L, 1L, "Mariana Oliveira", 5, "Muito organizado!", Instant.now()),
                new AvaliacaoResponse(2L, 1L, "Carlos Lima", 4, "Bom atendimento.", Instant.now()),
                new AvaliacaoResponse(3L, 1L, "Ana Souza", 4, "Fácil acesso.", Instant.now())
        );
        when(avaliacaoService.obterResumoAvaliacoes(1L))
                .thenReturn(new ResumoAvaliacaoResponse(1L, 4.3, 3L, avaliacoes));

        PerfilEmpresaController controller = new PerfilEmpresaController(userSession, appContext, avaliacaoService);

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PerfilEmpresa.fxml"));
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            assertNotNull(root.lookup("#boxAvaliacaoCentro"));
            Label lblNota = (Label) root.lookup("#lblNotaMedia");
            Label lblEstrelas = (Label) root.lookup("#lblEstrelasNota");
            Label lblTotal = (Label) root.lookup("#lblTotalAvaliacoes");
            VBox vboxComentarios = (VBox) root.lookup("#vboxComentariosAvaliacoes");

            assertEquals("4.3 / 5.0", lblNota.getText());
            assertEquals("★★★★☆", lblEstrelas.getText());
            assertEquals("(3 avaliações)", lblTotal.getText());
            assertEquals(2, vboxComentarios.getChildren().size());

            Label primeiroComentario = (Label) vboxComentarios.getChildren().get(0);
            assertTrue(primeiroComentario.getText().contains("Mariana Oliveira"));
            assertTrue(primeiroComentario.getText().contains("Muito organizado!"));
            return null;
        });

        verify(avaliacaoService, times(1)).obterResumoAvaliacoes(1L);
    }

    @Test
    @DisplayName("Deve exibir texto padrão quando centro de coleta ainda não possuir avaliações")
    void deveExibirEstadoVazioQuandoSemAvaliacoes() throws Exception {
        ApplicationContext appContext = mock(ApplicationContext.class);
        AvaliacaoService avaliacaoService = mock(AvaliacaoService.class);
        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(2L));

        when(avaliacaoService.obterResumoAvaliacoes(2L))
                .thenReturn(new ResumoAvaliacaoResponse(2L, 0.0, 0L, List.of()));

        PerfilEmpresaController controller = new PerfilEmpresaController(userSession, appContext, avaliacaoService);

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PerfilEmpresa.fxml"));
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            Label lblNota = (Label) root.lookup("#lblNotaMedia");
            Label lblEstrelas = (Label) root.lookup("#lblEstrelasNota");
            Label lblTotal = (Label) root.lookup("#lblTotalAvaliacoes");
            VBox vboxComentarios = (VBox) root.lookup("#vboxComentariosAvaliacoes");

            assertEquals("Sem nota", lblNota.getText());
            assertEquals("☆☆☆☆☆", lblEstrelas.getText());
            assertEquals("(0 avaliações)", lblTotal.getText());
            assertEquals(1, vboxComentarios.getChildren().size());

            Label aviso = (Label) vboxComentarios.getChildren().get(0);
            assertEquals("Ainda não há avaliações registradas para este centro de coleta.", aviso.getText());
            return null;
        });
    }

    @Test
    @DisplayName("Deve formatar corretamente singular para 1 avaliação sem comentário")
    void deveFormatarSingularParaUmaAvaliacaoSemComentario() throws Exception {
        ApplicationContext appContext = mock(ApplicationContext.class);
        AvaliacaoService avaliacaoService = mock(AvaliacaoService.class);
        UserSession userSession = new UserSession();
        userSession.setEmpresaLogada(criarEmpresa(3L));

        when(avaliacaoService.obterResumoAvaliacoes(3L))
                .thenReturn(new ResumoAvaliacaoResponse(
                        3L, 5.0, 1L,
                        List.of(new AvaliacaoResponse(9L, 3L, "Pedro", 5, null, Instant.now()))
                ));

        PerfilEmpresaController controller = new PerfilEmpresaController(userSession, appContext, avaliacaoService);

        Parent root = executarNoJavaFx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PerfilEmpresa.fxml"));
            loader.setControllerFactory(clazz -> controller);
            return loader.load();
        });

        executarNoJavaFx(() -> {
            Label lblNota = (Label) root.lookup("#lblNotaMedia");
            Label lblEstrelas = (Label) root.lookup("#lblEstrelasNota");
            Label lblTotal = (Label) root.lookup("#lblTotalAvaliacoes");
            VBox vboxComentarios = (VBox) root.lookup("#vboxComentariosAvaliacoes");

            assertEquals("5.0 / 5.0", lblNota.getText());
            assertEquals("★★★★★", lblEstrelas.getText());
            assertEquals("(1 avaliação)", lblTotal.getText());
            assertTrue(((Label) vboxComentarios.getChildren().get(0)).getText().contains("Avaliação sem comentário."));
            return null;
        });
    }

    private static EmpresaResponse criarEmpresa(Long id) {
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
                new BigDecimal("1000.000"),
                Instant.now(),
                null
        );
    }

    @FunctionalInterface
    private interface JavaFxCallable<T> {
        T call() throws Exception;
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
}
