package com.residuos.lixoeletronico.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.residuos.lixoeletronico.dto.empresa.ContatoDTO;
import com.residuos.lixoeletronico.dto.empresa.EmpresaCreateRequest;
import com.residuos.lixoeletronico.dto.empresa.EnderecoDTO;
import com.residuos.lixoeletronico.dto.usuario.UsuarioCreateRequest;
import com.residuos.lixoeletronico.service.EmpresaService;
import com.residuos.lixoeletronico.service.UsuarioService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Component
public class CadastroController implements Initializable {

    private static final String[] UFS_BRASIL = {
        "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
        "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
        "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    };

    private static final String CEP_REGEX = "\\d{5}-?\\d{3}";

    @FXML private RadioButton rbPessoaFisica;
    @FXML private RadioButton rbPessoaJuridica;
    @FXML private VBox boxPessoaFisica;
    @FXML private VBox boxPessoaJuridica;

    // Campos Pessoa Física
    @FXML private TextField txtNomeUsuario;
    @FXML private TextField txtTelefoneUsuario;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmailUsuario;
    @FXML private PasswordField txtSenhaUsuario;
    @FXML private TextField txtSenhaUsuarioVisivel;

    // Campos Pessoa Jurídica
    @FXML private TextField txtNomeEmpresa;
    @FXML private TextField txtCnpj;
    @FXML private TextField txtEmailEmpresa;
    @FXML private TextField txtTelefoneEmpresa;
    @FXML private TextField txtWhatsappEmpresa;
    @FXML private TextField txtSiteEmpresa;
    @FXML private TextField txtInstagramEmpresa;
    @FXML private PasswordField txtSenhaEmpresa;
    @FXML private TextField txtSenhaEmpresaVisivel;

    // Endereço compartilhado
    @FXML private TextField txtCep;
    @FXML private TextField txtLogradouro;
    @FXML private TextField txtNumero;
    @FXML private TextField txtComplemento;
    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private ComboBox<String> cbUf;

    @FXML private Label lblMensagem;
    @FXML private Button btnVoltarLogin;

    private final UsuarioService usuarioService;
    private final EmpresaService empresaService;
    private final ApplicationContext applicationContext;

    public CadastroController(
            UsuarioService usuarioService,
            EmpresaService empresaService,
            ApplicationContext applicationContext
    ) {
        this.usuarioService = usuarioService;
        this.empresaService = empresaService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbUf != null) {
            cbUf.setItems(FXCollections.observableArrayList(UFS_BRASIL));
            cbUf.setValue("MG");
        }
        vincularCamposSenha(txtSenhaUsuario, txtSenhaUsuarioVisivel);
        vincularCamposSenha(txtSenhaEmpresa, txtSenhaEmpresaVisivel);
    }

    private void vincularCamposSenha(PasswordField passwordField, TextField textField) {
        if (passwordField != null && textField != null) {
            textField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }

    @FXML
    public void handleAlternarTipoCadastro() {
        boolean isPessoaFisica = rbPessoaFisica != null && rbPessoaFisica.isSelected();

        boxPessoaFisica.setVisible(isPessoaFisica);
        boxPessoaFisica.setManaged(isPessoaFisica);

        boxPessoaJuridica.setVisible(!isPessoaFisica);
        boxPessoaJuridica.setManaged(!isPessoaFisica);

        ocultarMensagem();
    }

    @FXML
    public void handleAlternarVisibilidadeSenhaUsuario() {
        alternarVisibilidade(txtSenhaUsuario, txtSenhaUsuarioVisivel);
    }

    @FXML
    public void handleAlternarVisibilidadeSenhaEmpresa() {
        alternarVisibilidade(txtSenhaEmpresa, txtSenhaEmpresaVisivel);
    }

    private void alternarVisibilidade(PasswordField pf, TextField tf) {
        boolean mostrarTexto = !tf.isVisible();
        tf.setVisible(mostrarTexto);
        tf.setManaged(mostrarTexto);
        pf.setVisible(!mostrarTexto);
        pf.setManaged(!mostrarTexto);
    }

    @FXML
    public void handleCadastrar() {
        ocultarMensagem();

        try {
            if (rbPessoaJuridica != null && rbPessoaJuridica.isSelected()) {
                cadastrarPessoaJuridica();
            } else {
                cadastrarPessoaFisica();
            }
        } catch (ResponseStatusException e) {
            exibirErro(e.getReason() != null ? e.getReason() : "Não foi possível concluir o cadastro.");
        } catch (DataIntegrityViolationException e) {
            exibirErro("CPF, CNPJ ou e-mail já cadastrado no sistema.");
        } catch (Exception e) {
            exibirErro("Erro ao salvar cadastro: " + e.getMessage());
        }
    }

    private void cadastrarPessoaFisica() {
        String nome = valor(txtNomeUsuario);
        String telefone = valor(txtTelefoneUsuario);
        String cpf = valor(txtCpf);
        String email = valor(txtEmailUsuario);
        String senha = valor(txtSenhaUsuario);

        if (nome.isEmpty() || telefone.isEmpty() || cpf.isEmpty()
                || email.isEmpty() || senha.isEmpty() || enderecoIncompleto()) {
            exibirErro("Preencha todos os campos obrigatórios.");
            return;
        }

        String cpfNumeros = cpf.replaceAll("\\D", "");
        if (cpfNumeros.length() != 11) {
            exibirErro("CPF inválido. Informe os 11 dígitos.");
            return;
        }

        if (senha.length() < 6) {
            exibirErro("A senha deve ter no mínimo 6 caracteres.");
            return;
        }

        EnderecoDTO endereco = construirEnderecoDTO();
        if (endereco == null) {
            return;
        }

        UsuarioCreateRequest request = new UsuarioCreateRequest(
                nome,
                cpfNumeros,
                email,
                senha,
                telefone,
                null,
                endereco
        );

        usuarioService.cadastrar(request);
        limparCampos();
        exibirSucesso("Cadastro realizado com sucesso! Clique em 'Voltar ao Login' para acessar.");
    }

    private void cadastrarPessoaJuridica() {
        String nome = valor(txtNomeEmpresa);
        String cnpj = valor(txtCnpj);
        String email = valor(txtEmailEmpresa);
        String telefone = valor(txtTelefoneEmpresa);
        String whatsapp = valor(txtWhatsappEmpresa);
        String site = valor(txtSiteEmpresa);
        String instagram = valor(txtInstagramEmpresa);
        String senha = valor(txtSenhaEmpresa);

        if (nome.isEmpty() || cnpj.isEmpty() || email.isEmpty()
                || telefone.isEmpty() || whatsapp.isEmpty() || senha.isEmpty()
                || enderecoIncompleto()) {
            exibirErro("Preencha todos os campos obrigatórios.");
            return;
        }

        String cnpjNumeros = cnpj.replaceAll("\\D", "");
        if (cnpjNumeros.length() != 14) {
            exibirErro("CNPJ inválido. Informe os 14 dígitos.");
            return;
        }

        if (senha.length() < 6) {
            exibirErro("A senha deve ter no mínimo 6 caracteres.");
            return;
        }

        EnderecoDTO endereco = construirEnderecoDTO();
        if (endereco == null) {
            return;
        }

        ContatoDTO contato = new ContatoDTO(
                telefone,
                whatsapp,
                site.isEmpty() ? null : site,
                instagram.isEmpty() ? null : instagram
        );

        EmpresaCreateRequest request = new EmpresaCreateRequest(
                nome,
                cnpjNumeros,
                email,
                senha,
                contato,
                endereco
        );

        empresaService.criarEmpresa(request);
        limparCampos();
        exibirSucesso("Cadastro da empresa realizado com sucesso! Clique em 'Voltar ao Login' para acessar.");
    }

    private boolean enderecoIncompleto() {
        String cep = valor(txtCep);
        String logradouro = valor(txtLogradouro);
        String numero = valor(txtNumero);
        String bairro = valor(txtBairro);
        String cidade = valor(txtCidade);
        String uf = cbUf.getValue() != null ? cbUf.getValue() : "";

        return cep.isEmpty() || logradouro.isEmpty() || numero.isEmpty()
                || bairro.isEmpty() || cidade.isEmpty() || uf.isEmpty();
    }

    private EnderecoDTO construirEnderecoDTO() {
        String cep = valor(txtCep);
        String logradouro = valor(txtLogradouro);
        String numero = valor(txtNumero);
        String complemento = valor(txtComplemento);
        String bairro = valor(txtBairro);
        String cidade = valor(txtCidade);
        String uf = cbUf.getValue() != null ? cbUf.getValue() : "";

        if (!cep.matches(CEP_REGEX)) {
            exibirErro("CEP inválido. Use o formato 00000-000 ou 00000000.");
            return null;
        }

        return new EnderecoDTO(
                cep,
                logradouro,
                numero,
                complemento.isEmpty() ? null : complemento,
                bairro,
                cidade,
                uf,
                null,
                null
        );
    }

    @FXML
    public void handleVoltarLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) btnVoltarLogin.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Login - Gestão de Lixo Eletrônico");
            stage.centerOnScreen();
        } catch (IOException e) {
            exibirErro("Erro ao retornar para a tela de login.");
        }
    }

    private void limparCampos() {
        txtNomeUsuario.clear();
        txtTelefoneUsuario.clear();
        txtCpf.clear();
        txtEmailUsuario.clear();
        txtSenhaUsuario.clear();

        txtNomeEmpresa.clear();
        txtCnpj.clear();
        txtEmailEmpresa.clear();
        txtTelefoneEmpresa.clear();
        txtWhatsappEmpresa.clear();
        txtSiteEmpresa.clear();
        txtInstagramEmpresa.clear();
        txtSenhaEmpresa.clear();

        txtCep.clear();
        txtLogradouro.clear();
        txtNumero.clear();
        txtComplemento.clear();
        txtBairro.clear();
        txtCidade.clear();
        if (cbUf != null) cbUf.setValue("MG");
    }

    private String valor(TextField field) {
        return field != null && field.getText() != null ? field.getText().trim() : "";
    }

    private void exibirErro(String mensagem) {
        lblMensagem.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-alignment: center;");
        lblMensagem.setText(mensagem);
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
    }

    private void exibirSucesso(String mensagem) {
        lblMensagem.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-alignment: center;");
        lblMensagem.setText(mensagem);
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
    }

    private void ocultarMensagem() {
        if (lblMensagem != null) {
            lblMensagem.setText("");
            lblMensagem.setVisible(false);
            lblMensagem.setManaged(false);
        }
    }
}