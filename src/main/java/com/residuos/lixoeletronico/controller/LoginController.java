package com.residuos.lixoeletronico.controller;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.usuario.UsuarioResponse;
import com.residuos.lixoeletronico.service.EmpresaService;
import com.residuos.lixoeletronico.service.UsuarioService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Label lblMensagem;

    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;
    private final UserSession userSession;
    private final ApplicationContext applicationContext;

    public LoginController(
            EmpresaService empresaService, 
            UsuarioService usuarioService, 
            UserSession userSession, 
            ApplicationContext applicationContext
    ) {
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.userSession = userSession;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void handleLogin() {
        String email = txtUsuario.getText() != null ? txtUsuario.getText().trim() : "";
        String senha = txtSenha.getText() != null ? txtSenha.getText().trim() : "";

        if (email.isEmpty() || senha.isEmpty()) {
            lblMensagem.setStyle("-fx-text-fill: #9c0000;");
            lblMensagem.setText("Preencha todos os campos!");
            return;
        }

        try {
            lblMensagem.setStyle("-fx-text-fill: #006600;");
            lblMensagem.setText("Autenticando...");

            UsuarioResponse usuario = usuarioService.autenticar(email, senha);

            userSession.limparSessao();
            userSession.setUsuarioLogado(usuario);

            navegarParaPerfilUsuario(usuario.id());

        } catch (ResponseStatusException e) {
            exibirErro(e.getReason() != null ? e.getReason() : "Usuário ou senha inválidos.");
            lblMensagem.setStyle("-fx-text-fill: #9c0000;");
            lblMensagem.setText("Usuário ou senha inválidos.");
        } catch (IOException e) {
            lblMensagem.setStyle("-fx-text-fill: #9c0000;");
            lblMensagem.setText("Erro ao carregar a tela de perfil.");
        }
    }

    @FXML
public void handleAbrirCadastro() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Cadastro.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.setScene(new Scene(root, 960, 760));
        stage.setTitle("Cadastro - Gestão de Lixo Eletrônico");
        stage.centerOnScreen();
    } catch (IOException e) {
        exibirErro("Erro ao abrir a tela de cadastro.");
    }
}

    private void navegarParaPerfilUsuario(Long usuarioId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PerfilUsuario.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        PerfilUsuarioController perfilUsuarioController = loader.getController();
        perfilUsuarioController.carregarPerfil(usuarioId);

        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.setScene(new Scene(root, 1020, 680));
        stage.setTitle("Perfil do Usuário - Gestão de Lixo Eletrônico");
        stage.centerOnScreen();
    }

    public void navegarParaPerfilEmpresa(Long empresaId) throws IOException {
        EmpresaResponse empresa = empresaService.buscarPerfilPorId(empresaId);
        userSession.limparSessao();
        userSession.setEmpresaLogada(empresa);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PerfilEmpresa.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Perfil da Empresa - Gestão de Lixo Eletrônico");
        stage.centerOnScreen();
    }

    private void exibirErro(String mensagem) {
        lblMensagem.setStyle("-fx-text-fill: #9c0000;");
        lblMensagem.setText(mensagem);
    }
}
