package com.residuos.lixoeletronico.controller;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.service.EmpresaService;

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
    private final UserSession userSession;
    private final ApplicationContext applicationContext;

    public LoginController(EmpresaService empresaService, UserSession userSession, ApplicationContext applicationContext) {
        this.empresaService = empresaService;
        this.userSession = userSession;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String senha = txtSenha.getText().trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            lblMensagem.setStyle("-fx-text-fill: #9c0000;");
            lblMensagem.setText("Preencha todos os campos!");
            return;
        }

        try {
            lblMensagem.setStyle("-fx-text-fill: #006600;");
            lblMensagem.setText("Autenticando...");

            EmpresaResponse empresa = empresaService.autenticar(usuario, senha);
            userSession.setEmpresaLogada(empresa);

            navegarParaPerfil();

        } catch (Exception e) {
            lblMensagem.setStyle("-fx-text-fill: #9c0000;");
            lblMensagem.setText("Usuário ou senha inválidos.");
        }
    }

    private void navegarParaPerfil() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PerfilEmpresa.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Perfil da Empresa - Gestão de Lixo Eletrônico");
        stage.centerOnScreen();
    }
}
