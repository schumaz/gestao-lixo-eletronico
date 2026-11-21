package com.residuos.lixoeletronico.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.usuario.ItemHistoricoDTO;
import com.residuos.lixoeletronico.model.Endereco;
import com.residuos.lixoeletronico.model.Usuario;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import com.residuos.lixoeletronico.service.UsuarioService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Component
public class PerfilUsuarioController {

    @FXML private ImageView imgFotoPerfil;
    @FXML private Label lblNome;
    @FXML private Label lblEndereco;
    @FXML private Label lblEmail;
    @FXML private Label lblTelefone;
    @FXML private Label lblUltimoDeposito;
    @FXML private Label lblMaterialMaisDescartado;
    @FXML private Label lblTotalKg;
    @FXML private VBox containerHistorico;
    @FXML private Button btnSair;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UsuarioService usuarioService;
    private final LoteResiduoService loteResiduoService;
    private final UserSession userSession;
    private final ApplicationContext applicationContext;
    private static final Logger LOGGER  = LoggerFactory.getLogger(PerfilUsuarioController.class);

    @Autowired
    public PerfilUsuarioController(
            UsuarioService usuarioService,
            LoteResiduoService loteResiduoService,
            UserSession userSession,
            ApplicationContext applicationContext
    ) {
        this.usuarioService = usuarioService;
        this.loteResiduoService = loteResiduoService;
        this.userSession = userSession;
        this.applicationContext = applicationContext;
    }

    public PerfilUsuarioController(
            UsuarioService usuarioService,
            LoteResiduoService loteResiduoService
    ) {
        this(usuarioService, loteResiduoService, null, null);
    }

    @FXML
    public void initialize() {
    }

    @FXML
    public void handleLogout() {
        if (userSession != null) {
            userSession.logout();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            if (applicationContext != null) {
                loader.setControllerFactory(applicationContext::getBean);
            }
            Parent root = loader.load();

            Stage stage = (Stage) (btnSair != null && btnSair.getScene() != null
                    ? btnSair.getScene().getWindow()
                    : lblNome.getScene().getWindow());

            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Login - Gestão de Lixo Eletrônico");
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.error("Erro ao carregar a tela de login ao encerrar sessão", e);
        }
    }

    public void carregarPerfil(Long usuarioId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        List<ItemHistoricoDTO> historico = loteResiduoService.listarHistoricoPorUsuario(usuarioId);

        double totalKg = loteResiduoService.calcularTotalKg(historico);
        String materialMaisDescartado = loteResiduoService.obterMaterialMaisDescartado(historico);

        String ultimoDepositoInfo = null;
        if (!historico.isEmpty()) {
            ItemHistoricoDTO ultimo = historico.get(0);
            String data = ultimo.dataDescarte() != null ? ultimo.dataDescarte().format(DATE_FORMATTER) : "";
            ultimoDepositoInfo = data + " (" + ultimo.nomeEmpresaDestino() + ")";
        }

        String enderecoFormatado = formatarEndereco(usuario.getEndereco());

        preencherDadosPerfil(
                usuario.getNome(),
                enderecoFormatado,
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getUrlFoto(),
                ultimoDepositoInfo,
                materialMaisDescartado,
                totalKg,
                historico
        );
    }

    private String formatarEndereco(Endereco end) {
        if (end == null) {
            return "Não informado";
        }
        return String.format("%s, %s - %s, %s - %s",
                end.getLogradouro(), end.getNumero(), end.getBairro(), end.getCidade(), end.getUf());
    }

    public void preencherDadosPerfil(
            String nome,
            String endereco,
            String email,
            String telefone,
            String urlFoto,
            String ultimoDepositoInfo,
            String materialMaisDescartado,
            double totalKgDescartado,
            List<ItemHistoricoDTO> historico
    ) {
        lblNome.setText(nome != null ? nome : "Usuário");
        lblEndereco.setText("📍 Endereço: " + (endereco != null ? endereco : "Não informado"));
        lblEmail.setText("✉ Email: " + (email != null ? email : "Não informado"));
        lblTelefone.setText("📞 Telefone: " + (telefone != null ? telefone : "Não informado"));

        if (urlFoto != null && !urlFoto.isBlank()) {
            try {
                imgFotoPerfil.setImage(new Image(urlFoto, true));
            } catch (Exception ignored) {
            }
        }

        lblUltimoDeposito.setText(
                ultimoDepositoInfo != null && !ultimoDepositoInfo.isBlank()
                        ? ultimoDepositoInfo
                        : "Nenhum depósito registrado"
        );

        lblMaterialMaisDescartado.setText(
                materialMaisDescartado != null && !materialMaisDescartado.isBlank()
                        ? materialMaisDescartado
                        : "-"
        );

        lblTotalKg.setText(String.format(Locale.US, "%.1f kg", totalKgDescartado));

        renderizarHistorico(historico);
    }

    private void renderizarHistorico(List<ItemHistoricoDTO> historico) {
        containerHistorico.getChildren().clear();

        if (historico == null || historico.isEmpty()) {
            Label lblVazio = new Label("Nenhum descarte registrado no histórico.");
            lblVazio.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
            containerHistorico.getChildren().add(lblVazio);
            return;
        }

        for (ItemHistoricoDTO item : historico) {
            containerHistorico.getChildren().add(criarCardHistorico(item));
        }
    }

    private VBox criarCardHistorico(ItemHistoricoDTO item) {
        VBox card = new VBox(6);
        card.setStyle(
                "-fx-background-color: #f9fafb; " +
                "-fx-padding: 12; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8;"
        );

        HBox topo = new HBox(10);
        topo.setAlignment(Pos.CENTER_LEFT);

        Label lblMaterial = new Label(item.material());
        lblMaterial.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #111827;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblPeso = new Label(String.format(Locale.US, "%.2f kg", item.pesoKg()));
        lblPeso.setStyle(
                "-fx-background-color: #dcfce7; " +
                "-fx-text-fill: #166534; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 11px; " +
                "-fx-padding: 3 8; " +
                "-fx-background-radius: 12;"
        );

        topo.getChildren().addAll(lblMaterial, spacer, lblPeso);

        HBox rodape = new HBox(10);
        rodape.setAlignment(Pos.CENTER_LEFT);

        Label lblDestino = new Label("Destino: " + item.nomeEmpresaDestino());
        lblDestino.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b5563;");

        Region spacerRodape = new Region();
        HBox.setHgrow(spacerRodape, Priority.ALWAYS);

        String dataFormatada = item.dataDescarte() != null ? item.dataDescarte().format(DATE_FORMATTER) : "";
        Label lblData = new Label(dataFormatada);
        lblData.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        rodape.getChildren().addAll(lblDestino, spacerRodape, lblData);

        card.getChildren().addAll(topo, rodape);
        return card;
    }
}