package com.residuos.lixoeletronico.controller;

import org.springframework.stereotype.Component;

import com.residuos.lixoeletronico.service.EmpresaService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

@Component
public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Label lblMensagem;

    private final EmpresaService empresaService;

    // Injeção de dependência gerenciada pelo Spring Boot
    public LoginController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @FXML
    public void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String senha = txtSenha.getText().trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            lblMensagem.setText("Preencha todos os campos!");
            return;
        }

        try {
            // Exemplo de integração: Chamar o serviço para validar credenciais
            lblMensagem.setStyle("-fx-text-fill: green;");
            lblMensagem.setText("Autenticando...");

            // Lógica de validação com o banco de dados aqui
            
        } catch (Exception e) {
            lblMensagem.setStyle("-fx-text-fill: red;");
            lblMensagem.setText("Usuário ou senha inválidos.");
        }
    }
}