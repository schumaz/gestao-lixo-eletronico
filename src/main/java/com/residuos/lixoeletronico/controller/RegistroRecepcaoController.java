package com.residuos.lixoeletronico.controller;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoCreateRequest;
import com.residuos.lixoeletronico.dto.lote.LoteResiduoResponse;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.service.EmpresaService;
import com.residuos.lixoeletronico.service.LoteResiduoService;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RegistroRecepcaoController implements Initializable {

    private static final BigDecimal PESO_MINIMO = new BigDecimal("0.001");
    private static final DateTimeFormatter DATA_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Label lblNomeEmpresaHeader;

    @FXML
    private TextField txtNomeUsuario;

    @FXML
    private TextField txtContatoUsuario;

    @FXML
    private DatePicker dpDataRecepcao;

    @FXML
    private ComboBox<CategoriaMaterial> cbCategoriaMaterial;

    @FXML
    private TextField txtTipoResiduo;

    @FXML
    private TextField txtPesoKg;

    @FXML
    private ComboBox<UrgenciaDescarte> cbUrgenciaDescarte;

    @FXML
    private Label lblMensagem;

    @FXML
    private Label lblMassaTotalEmpresa;

    @FXML
    private Label lblMassaEmEstoque;

    @FXML
    private TableView<LoteResiduoResponse> tblLotesEstoque;

    @FXML
    private TableColumn<LoteResiduoResponse, String> colData;

    @FXML
    private TableColumn<LoteResiduoResponse, String> colMaterial;

    @FXML
    private TableColumn<LoteResiduoResponse, String> colTipo;

    @FXML
    private TableColumn<LoteResiduoResponse, String> colPeso;

    @FXML
    private TableColumn<LoteResiduoResponse, String> colUrgencia;

    @FXML
    private Button btnVoltarPerfil;

    private final LoteResiduoService loteResiduoService;
    private final EmpresaService empresaService;
    private final UserSession userSession;
    private final ApplicationContext applicationContext;

    public RegistroRecepcaoController(
        LoteResiduoService loteResiduoService,
        EmpresaService empresaService,
        UserSession userSession,
        ApplicationContext applicationContext
    ) {
        this.loteResiduoService = loteResiduoService;
        this.empresaService = empresaService;
        this.userSession = userSession;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarCamposFormulario();
        configurarTabelaEstoque();
        carregarDadosEmpresaEEstoque();
    }

    private void configurarCamposFormulario() {
        if (dpDataRecepcao != null && dpDataRecepcao.getValue() == null) {
            dpDataRecepcao.setValue(LocalDate.now());
        }

        if (cbCategoriaMaterial != null) {
            cbCategoriaMaterial.setItems(
                FXCollections.observableArrayList(CategoriaMaterial.values())
            );
            cbCategoriaMaterial.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(CategoriaMaterial categoria) {
                        return formatarCategoria(categoria);
                    }

                    @Override
                    public CategoriaMaterial fromString(String string) {
                        return null;
                    }
                }
            );
        }

        if (cbUrgenciaDescarte != null) {
            cbUrgenciaDescarte.setItems(
                FXCollections.observableArrayList(UrgenciaDescarte.values())
            );
            cbUrgenciaDescarte.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(UrgenciaDescarte urgencia) {
                        return formatarUrgencia(urgencia);
                    }

                    @Override
                    public UrgenciaDescarte fromString(String string) {
                        return null;
                    }
                }
            );
            cbUrgenciaDescarte.setValue(UrgenciaDescarte.MEDIA);
        }
    }

    private void configurarTabelaEstoque() {
        if (colData != null) {
            colData.setCellValueFactory(cell ->
                new SimpleStringProperty(
                    cell.getValue().dataRecepcao() != null
                        ? cell.getValue().dataRecepcao().format(DATA_FORMATTER)
                        : ""
                )
            );
        }
        if (colMaterial != null) {
            colMaterial.setCellValueFactory(cell ->
                new SimpleStringProperty(
                    formatarCategoria(cell.getValue().categoriaMaterial())
                )
            );
        }
        if (colTipo != null) {
            colTipo.setCellValueFactory(cell ->
                new SimpleStringProperty(
                    cell.getValue().tipoResiduo() != null
                        ? cell.getValue().tipoResiduo()
                        : ""
                )
            );
        }
        if (colPeso != null) {
            colPeso.setCellValueFactory(cell ->
                new SimpleStringProperty(
                    cell.getValue().pesoKg() != null
                        ? cell
                              .getValue()
                              .pesoKg()
                              .stripTrailingZeros()
                              .toPlainString()
                        : "0"
                )
            );
        }
        if (colUrgencia != null) {
            colUrgencia.setCellValueFactory(cell ->
                new SimpleStringProperty(
                    formatarUrgencia(cell.getValue().urgenciaDescarte())
                )
            );
        }
    }

    public void carregarDadosEmpresaEEstoque() {
        EmpresaResponse empresa = userSession.getEmpresaLogada();
        if (empresa == null) {
            exibirErro("Nenhuma empresa autenticada na sessão.");
            return;
        }

        if (lblNomeEmpresaHeader != null) {
            lblNomeEmpresaHeader.setText(empresa.nome());
        }

        atualizarResumoMassaTotal(empresa.totalKgDescartado());

        List<LoteResiduoResponse> lotesEmEstoque =
            loteResiduoService.listarLotesEmEstoque(empresa.id(), null, null);
        atualizarTabelaEEstoque(lotesEmEstoque);
    }

    @FXML
    public void handleRegistrarRecepcao() {
        EmpresaResponse empresa = userSession.getEmpresaLogada();
        if (empresa == null) {
            exibirErro("Sessão expirada. Faça login novamente.");
            return;
        }

        String erroValidacao = validarFormulario();
        if (erroValidacao != null) {
            exibirErro(erroValidacao);
            return;
        }

        try {
            BigDecimal pesoKg = parsePesoKg(txtPesoKg.getText());
            LoteResiduoCreateRequest request = new LoteResiduoCreateRequest(
                txtNomeUsuario.getText().trim(),
                txtContatoUsuario.getText().trim(),
                dpDataRecepcao.getValue(),
                cbCategoriaMaterial.getValue(),
                txtTipoResiduo.getText().trim(),
                pesoKg,
                cbUrgenciaDescarte.getValue()
            );

            LoteResiduoResponse loteRegistrado =
                loteResiduoService.registrarRecepcao(empresa.id(), request);

            EmpresaResponse empresaAtualizada =
                empresaService.buscarPerfilPorId(empresa.id());
            userSession.setEmpresaLogada(empresaAtualizada);
            atualizarResumoMassaTotal(empresaAtualizada.totalKgDescartado());

            List<LoteResiduoResponse> lotesEmEstoque =
                loteResiduoService.listarLotesEmEstoque(
                    empresa.id(),
                    null,
                    null
                );
            atualizarTabelaEEstoque(lotesEmEstoque);

            limparCamposFormulario();
            exibirSucesso(
                "Lote #" +
                    loteRegistrado.id() +
                    " registrado com sucesso (" +
                    loteRegistrado
                        .pesoKg()
                        .stripTrailingZeros()
                        .toPlainString() +
                    " kg em estoque)."
            );
        } catch (Exception e) {
            exibirErro("Erro ao registrar recepção: " + e.getMessage());
        }
    }

    @FXML
    public void handleLimpar() {
        limparCamposFormulario();
        if (lblMensagem != null) {
            lblMensagem.setText("");
        }
    }

    @FXML
    public void handleVoltarPerfil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/PerfilEmpresa.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) btnVoltarPerfil.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Perfil da Empresa - Gestão de Lixo Eletrônico");
            stage.centerOnScreen();
        } catch (IOException e) {
            exibirErro("Não foi possível voltar ao perfil: " + e.getMessage());
        }
    }

    String validarFormulario() {
        if (
            txtNomeUsuario == null ||
            txtNomeUsuario.getText() == null ||
            txtNomeUsuario.getText().isBlank()
        ) {
            return "Informe o nome do usuário entregador.";
        }
        if (txtNomeUsuario.getText().trim().length() > 150) {
            return "Nome do usuário deve ter no máximo 150 caracteres.";
        }
        if (
            txtContatoUsuario == null ||
            txtContatoUsuario.getText() == null ||
            txtContatoUsuario.getText().isBlank()
        ) {
            return "Informe o contato do usuário.";
        }
        if (txtContatoUsuario.getText().trim().length() > 100) {
            return "Contato do usuário deve ter no máximo 100 caracteres.";
        }
        if (dpDataRecepcao == null || dpDataRecepcao.getValue() == null) {
            return "Selecione a data de recepção.";
        }
        if (dpDataRecepcao.getValue().isAfter(LocalDate.now())) {
            return "Data de recepção não pode ser futura.";
        }
        if (
            cbCategoriaMaterial == null ||
            cbCategoriaMaterial.getValue() == null
        ) {
            return "Selecione a categoria do material.";
        }
        if (
            cbUrgenciaDescarte == null || cbUrgenciaDescarte.getValue() == null
        ) {
            return "Selecione a urgência de descarte.";
        }
        if (
            txtTipoResiduo == null ||
            txtTipoResiduo.getText() == null ||
            txtTipoResiduo.getText().isBlank()
        ) {
            return "Informe o tipo / descrição do resíduo.";
        }
        if (txtTipoResiduo.getText().trim().length() > 150) {
            return "Tipo do resíduo deve ter no máximo 150 caracteres.";
        }
        if (
            txtPesoKg == null ||
            txtPesoKg.getText() == null ||
            txtPesoKg.getText().isBlank()
        ) {
            return "Informe o peso em kg.";
        }

        BigDecimal peso;
        try {
            peso = parsePesoKg(txtPesoKg.getText());
        } catch (NumberFormatException ex) {
            return "Peso inválido. Use números positivos (ex.: 12.500).";
        }

        if (peso.compareTo(PESO_MINIMO) < 0) {
            return "Peso deve ser maior que zero (mínimo 0.001 kg).";
        }
        if (
            peso.scale() > 3 || peso.precision() - Math.max(peso.scale(), 0) > 9
        ) {
            return "Peso deve ter no máximo 9 dígitos inteiros e 3 casas decimais.";
        }
        return null;
    }

    static BigDecimal parsePesoKg(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new NumberFormatException("Peso vazio");
        }
        String normalizado = texto.trim().replace(",", ".");
        return new BigDecimal(normalizado);
    }

    private void limparCamposFormulario() {
        if (txtNomeUsuario != null) txtNomeUsuario.clear();
        if (txtContatoUsuario != null) txtContatoUsuario.clear();
        if (dpDataRecepcao != null) dpDataRecepcao.setValue(LocalDate.now());
        if (cbCategoriaMaterial != null) cbCategoriaMaterial.setValue(null);
        if (txtTipoResiduo != null) txtTipoResiduo.clear();
        if (txtPesoKg != null) txtPesoKg.clear();
        if (cbUrgenciaDescarte != null) cbUrgenciaDescarte.setValue(
            UrgenciaDescarte.MEDIA
        );
    }

    private void atualizarResumoMassaTotal(BigDecimal totalKg) {
        if (lblMassaTotalEmpresa != null) {
            BigDecimal valor = totalKg != null ? totalKg : BigDecimal.ZERO;
            lblMassaTotalEmpresa.setText(
                valor.stripTrailingZeros().toPlainString() + " kg"
            );
        }
    }

    private void atualizarTabelaEEstoque(List<LoteResiduoResponse> lotes) {
        if (tblLotesEstoque != null) {
            tblLotesEstoque.setItems(FXCollections.observableArrayList(lotes));
        }
        if (lblMassaEmEstoque != null) {
            BigDecimal soma = lotes
                .stream()
                .map(LoteResiduoResponse::pesoKg)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblMassaEmEstoque.setText(
                soma.stripTrailingZeros().toPlainString() + " kg"
            );
        }
    }

    private void exibirErro(String mensagem) {
        if (lblMensagem != null) {
            lblMensagem.setStyle(
                "-fx-text-fill: #C62828; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
            lblMensagem.setText(mensagem);
        }
    }

    private void exibirSucesso(String mensagem) {
        if (lblMensagem != null) {
            lblMensagem.setStyle(
                "-fx-text-fill: #2E7D32; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
            lblMensagem.setText(mensagem);
        }
    }

    public static String formatarCategoria(CategoriaMaterial categoria) {
        if (categoria == null) {
            return "";
        }
        return switch (categoria) {
            case INFORMATICA -> "Informática";
            case CELULARES_TABLETS -> "Celulares e Tablets";
            case PILHAS_BATERIAS -> "Pilhas e Baterias";
            case LAMPADAS -> "Lâmpadas";
            case ELETRODOMESTICOS_PEQUENOS -> "Eletrodomésticos Pequenos";
            case ELETRODOMESTICOS_GRANDES -> "Eletrodomésticos Grandes";
            case TVS_MONITORES -> "TVs e Monitores";
            case CABOS_CARREGADORES -> "Cabos e Carregadores";
            case OUTROS -> "Outros";
        };
    }

    public static String formatarUrgencia(UrgenciaDescarte urgencia) {
        if (urgencia == null) {
            return "";
        }
        return switch (urgencia) {
            case BAIXA -> "Baixa";
            case MEDIA -> "Média";
            case ALTA -> "Alta";
            case CRITICA -> "Crítica";
        };
    }
}
