package com.residuos.lixoeletronico.controller;

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
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import javafx.application.Platform;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ProgressIndicator piCarregando;

    @FXML
    private TextField txtBuscarEmailUsuario;

    @FXML
    private Button btnBuscarUsuario;

    @FXML
    private Button btnDesvincularUsuario;

    @FXML
    private Label lblUsuarioVinculado;

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
    private Button btnRegistrar;

    @FXML
    private Button btnLimpar;

    @FXML
    private Label lblMassaTotalEmpresa;

    @FXML
    private Label lblTituloMassaEstoque;

    @FXML
    private Label lblMassaEmEstoque;

    @FXML
    private ComboBox<StatusLote> cbFiltroStatus;

    @FXML
    private ComboBox<CategoriaMaterial> cbFiltroCategoria;

    @FXML
    private ComboBox<UrgenciaDescarte> cbFiltroUrgencia;

    @FXML
    private Button btnFiltrarEstoque;

    @FXML
    private Button btnLimparFiltros;

    @FXML
    private Button btnAtualizarEstoque;

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
    private TableColumn<LoteResiduoResponse, String> colStatus;

    @FXML
    private Label lblLoteSelecionado;

    @FXML
    private Button btnAvancarStatus;

    @FXML
    private Button btnVoltarPerfil;

    private final LoteResiduoApiClient apiClient;
    private final UserSession userSession;
    private final ApplicationContext applicationContext;
    private final Executor asyncExecutor;

    private Long usuarioVinculadoId;

    @Autowired
    public RegistroRecepcaoController(
        LoteResiduoApiClient apiClient,
        UserSession userSession,
        ApplicationContext applicationContext
    ) {
        this(
            apiClient,
            userSession,
            applicationContext,
            ForkJoinPool.commonPool()
        );
    }

    RegistroRecepcaoController(
        LoteResiduoApiClient apiClient,
        UserSession userSession,
        ApplicationContext applicationContext,
        Executor asyncExecutor
    ) {
        this.apiClient = apiClient;
        this.userSession = userSession;
        this.applicationContext = applicationContext;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarCamposFormulario();
        configurarFiltrosEstoque();
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

    private void configurarFiltrosEstoque() {
        if (cbFiltroStatus != null) {
            List<StatusLote> opcoesStatus = new ArrayList<>();
            opcoesStatus.add(null);
            opcoesStatus.addAll(Arrays.asList(StatusLote.values()));
            cbFiltroStatus.setItems(
                FXCollections.observableArrayList(opcoesStatus)
            );
            cbFiltroStatus.setCellFactory(listView ->
                criarCelulaStatusFiltro()
            );
            cbFiltroStatus.setButtonCell(criarCelulaStatusFiltro());
            cbFiltroStatus.setValue(null);
        }

        if (cbFiltroCategoria != null) {
            List<CategoriaMaterial> opcoesCategoria = new ArrayList<>();
            opcoesCategoria.add(null);
            opcoesCategoria.addAll(Arrays.asList(CategoriaMaterial.values()));
            cbFiltroCategoria.setItems(
                FXCollections.observableArrayList(opcoesCategoria)
            );
            cbFiltroCategoria.setCellFactory(listView ->
                criarCelulaCategoriaFiltro()
            );
            cbFiltroCategoria.setButtonCell(criarCelulaCategoriaFiltro());
            cbFiltroCategoria.setValue(null);
        }

        if (cbFiltroUrgencia != null) {
            List<UrgenciaDescarte> opcoesUrgencia = new ArrayList<>();
            opcoesUrgencia.add(null);
            opcoesUrgencia.addAll(Arrays.asList(UrgenciaDescarte.values()));
            cbFiltroUrgencia.setItems(
                FXCollections.observableArrayList(opcoesUrgencia)
            );
            cbFiltroUrgencia.setCellFactory(listView ->
                criarCelulaUrgenciaFiltro()
            );
            cbFiltroUrgencia.setButtonCell(criarCelulaUrgenciaFiltro());
            cbFiltroUrgencia.setValue(null);
        }
    }

    private ListCell<CategoriaMaterial> criarCelulaCategoriaFiltro() {
        return new ListCell<>() {
            @Override
            protected void updateItem(CategoriaMaterial item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todas as categorias");
                } else {
                    setText(formatarCategoria(item));
                }
            }
        };
    }

    private ListCell<StatusLote> criarCelulaStatusFiltro() {
        return new ListCell<>() {
            @Override
            protected void updateItem(StatusLote item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todos os status");
                } else {
                    setText(formatarStatus(item));
                }
            }
        };
    }

    private ListCell<UrgenciaDescarte> criarCelulaUrgenciaFiltro() {
        return new ListCell<>() {
            @Override
            protected void updateItem(UrgenciaDescarte item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todas as urgências");
                } else {
                    setText(formatarUrgencia(item));
                }
            }
        };
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
        if (colStatus != null) {
            colStatus.setCellValueFactory(cell ->
                new SimpleStringProperty(
                    formatarStatus(cell.getValue().status())
                )
            );
        }
        if (tblLotesEstoque != null) {
            tblLotesEstoque
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, antigo, selecionado) ->
                    atualizarControlesStatusSelecionado(selecionado)
                );
        }
        atualizarControlesStatusSelecionado(null);
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

        StatusLote filtroStatus = obterFiltroStatus();
        CategoriaMaterial filtroCategoria = obterFiltroCategoria();
        UrgenciaDescarte filtroUrgencia = obterFiltroUrgencia();
        definirCarregando(true);

        CompletableFuture.supplyAsync(
            () -> {
                EmpresaResponse empresaAtualizada = apiClient.buscarPerfilEmpresa(
                    empresa.id()
                );
                List<LoteResiduoResponse> lotes =
                    apiClient.listarLotes(
                        empresa.id(),
                        filtroStatus,
                        filtroCategoria,
                        filtroUrgencia
                    );
                return new DadosEstoqueCarregados(
                    empresaAtualizada,
                    lotes
                );
            },
            asyncExecutor
        ).whenComplete((resultado, erro) ->
            executarNaUiThread(() -> {
                definirCarregando(false);
                if (erro != null) {
                    exibirErro(
                        "Erro ao carregar inventário pela API: " +
                            extrairMensagemErro(erro)
                    );
                    return;
                }
                if (resultado.empresa() != null) {
                    userSession.setEmpresaLogada(resultado.empresa());
                    if (lblNomeEmpresaHeader != null) {
                        lblNomeEmpresaHeader.setText(resultado.empresa().nome());
                    }
                    atualizarResumoMassaTotal(
                        resultado.empresa().totalKgDescartado()
                    );
                }
                atualizarTabelaEEstoque(
                    resultado.lotes(),
                    filtroStatus,
                    filtroCategoria != null || filtroUrgencia != null
                );
            })
        );
    }

    @FXML
    public void handleFiltrarEstoque() {
        carregarLotesEmEstoqueComFiltros(false);
    }

    @FXML
    public void handleAtualizarEstoque() {
        carregarDadosEmpresaEEstoque();
    }

    @FXML
    public void handleLimparFiltros() {
        if (cbFiltroStatus != null) {
            cbFiltroStatus.setValue(null);
        }
        if (cbFiltroCategoria != null) {
            cbFiltroCategoria.setValue(null);
        }
        if (cbFiltroUrgencia != null) {
            cbFiltroUrgencia.setValue(null);
        }
        carregarLotesEmEstoqueComFiltros(false);
    }

    private void carregarLotesEmEstoqueComFiltros(boolean exibirMensagemSucesso) {
        EmpresaResponse empresa = userSession.getEmpresaLogada();
        if (empresa == null) {
            exibirErro("Sessão expirada. Faça login novamente.");
            return;
        }

        StatusLote filtroStatus = obterFiltroStatus();
        CategoriaMaterial filtroCategoria = obterFiltroCategoria();
        UrgenciaDescarte filtroUrgencia = obterFiltroUrgencia();
        definirCarregando(true);

        CompletableFuture.supplyAsync(
            () ->
                apiClient.listarLotes(
                    empresa.id(),
                    filtroStatus,
                    filtroCategoria,
                    filtroUrgencia
                ),
            asyncExecutor
        ).whenComplete((lotes, erro) ->
            executarNaUiThread(() -> {
                definirCarregando(false);
                if (erro != null) {
                    exibirErro(
                        "Erro ao consultar lotes: " +
                            extrairMensagemErro(erro)
                    );
                    return;
                }
                atualizarTabelaEEstoque(
                    lotes,
                    filtroStatus,
                    filtroCategoria != null || filtroUrgencia != null
                );
                if (exibirMensagemSucesso) {
                    exibirInfo("Inventário atualizado com sucesso.");
                }
            })
        );
    }

    @FXML
    public void handleBuscarUsuario() {
        String email =
            txtBuscarEmailUsuario != null && txtBuscarEmailUsuario.getText() != null
                ? txtBuscarEmailUsuario.getText().trim()
                : "";

        if (email.isBlank()) {
            exibirErro("Informe o e-mail do usuário para buscar.");
            return;
        }

        definirCarregando(true);
        exibirInfo("Buscando usuário na API...");

        CompletableFuture.supplyAsync(
            () -> apiClient.buscarUsuarioPorEmail(email),
            asyncExecutor
        ).whenComplete((resultado, erro) ->
            executarNaUiThread(() -> {
                definirCarregando(false);
                if (erro != null) {
                    exibirErro(
                        "Erro ao buscar usuário: " + extrairMensagemErro(erro)
                    );
                    return;
                }

                Optional<UsuarioResponse> usuarioOpt = resultado;
                if (usuarioOpt.isEmpty()) {
                    limparVinculoUsuario(false);
                    exibirErro(
                        "Nenhum usuário cadastrado encontrado com este e-mail. Preencha os dados manualmente."
                    );
                    return;
                }

                UsuarioResponse usuario = usuarioOpt.get();
                usuarioVinculadoId = usuario.id();

                if (txtNomeUsuario != null && usuario.nome() != null) {
                    txtNomeUsuario.setText(usuario.nome());
                }
                if (txtContatoUsuario != null) {
                    String contato =
                        usuario.telefone() != null && !usuario.telefone().isBlank()
                            ? usuario.telefone()
                            : usuario.email();
                    txtContatoUsuario.setText(contato != null ? contato : "");
                }

                atualizarBadgeUsuarioVinculado(usuario);
                exibirSucesso(
                    "Usuário #" +
                        usuario.id() +
                        " (" +
                        usuario.nome() +
                        ") vinculado à recepção."
                );
            })
        );
    }

    @FXML
    public void handleDesvincularUsuario() {
        limparVinculoUsuario(true);
        if (lblMensagem != null) {
            lblMensagem.setText("");
        }
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

        BigDecimal pesoKg = parsePesoKg(txtPesoKg.getText());
        LoteResiduoCreateRequest request = new LoteResiduoCreateRequest(
            txtNomeUsuario.getText().trim(),
            txtContatoUsuario.getText().trim(),
            dpDataRecepcao.getValue(),
            cbCategoriaMaterial.getValue(),
            txtTipoResiduo.getText().trim(),
            pesoKg,
            cbUrgenciaDescarte.getValue(),
            usuarioVinculadoId
        );

        StatusLote filtroStatus = obterFiltroStatus();
        CategoriaMaterial filtroCategoria = obterFiltroCategoria();
        UrgenciaDescarte filtroUrgencia = obterFiltroUrgencia();

        definirCarregando(true);
        exibirInfo("Registrando recepção na API...");

        CompletableFuture.supplyAsync(
            () -> {
                LoteResiduoResponse loteRegistrado = apiClient.registrarRecepcao(
                    empresa.id(),
                    request
                );
                EmpresaResponse empresaAtualizada = apiClient.buscarPerfilEmpresa(
                    empresa.id()
                );
                List<LoteResiduoResponse> lotes =
                    apiClient.listarLotes(
                        empresa.id(),
                        filtroStatus,
                        filtroCategoria,
                        filtroUrgencia
                    );
                return new ResultadoRegistroRecepcao(
                    loteRegistrado,
                    empresaAtualizada,
                    lotes
                );
            },
            asyncExecutor
        ).whenComplete((resultado, erro) ->
            executarNaUiThread(() -> {
                definirCarregando(false);
                if (erro != null) {
                    exibirErro(
                        "Erro ao registrar recepção: " +
                            extrairMensagemErro(erro)
                    );
                    return;
                }

                if (resultado.empresaAtualizada() != null) {
                    userSession.setEmpresaLogada(resultado.empresaAtualizada());
                    atualizarResumoMassaTotal(
                        resultado.empresaAtualizada().totalKgDescartado()
                    );
                }

                atualizarTabelaEEstoque(
                    resultado.lotesEmEstoque(),
                    filtroStatus,
                    filtroCategoria != null || filtroUrgencia != null
                );

                limparCamposFormulario();
                LoteResiduoResponse lote = resultado.loteRegistrado();
                exibirSucesso(
                    "Lote #" +
                        lote.id() +
                        " registrado com sucesso (" +
                        lote.pesoKg().stripTrailingZeros().toPlainString() +
                        " kg em estoque)."
                );
            })
        );
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
        limparVinculoUsuario(true);
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

    private void limparVinculoUsuario(boolean limparCampoBusca) {
        usuarioVinculadoId = null;
        if (limparCampoBusca && txtBuscarEmailUsuario != null) {
            txtBuscarEmailUsuario.clear();
        }
        if (lblUsuarioVinculado != null) {
            lblUsuarioVinculado.setText("");
            lblUsuarioVinculado.setVisible(false);
            lblUsuarioVinculado.setManaged(false);
        }
        if (btnDesvincularUsuario != null) {
            btnDesvincularUsuario.setVisible(false);
            btnDesvincularUsuario.setManaged(false);
        }
    }

    private void atualizarBadgeUsuarioVinculado(UsuarioResponse usuario) {
        if (lblUsuarioVinculado != null) {
            lblUsuarioVinculado.setText(
                "Vinculado ao usuário #" +
                    usuario.id() +
                    " — " +
                    usuario.nome() +
                    " (" +
                    usuario.email() +
                    ")"
            );
            lblUsuarioVinculado.setVisible(true);
            lblUsuarioVinculado.setManaged(true);
        }
        if (btnDesvincularUsuario != null) {
            btnDesvincularUsuario.setVisible(true);
            btnDesvincularUsuario.setManaged(true);
        }
    }

    @FXML
    public void handleAvancarStatus() {
        EmpresaResponse empresa = userSession.getEmpresaLogada();
        if (empresa == null) {
            exibirErro("Sessão expirada. Faça login novamente.");
            return;
        }

        LoteResiduoResponse loteSelecionado = tblLotesEstoque != null
            ? tblLotesEstoque.getSelectionModel().getSelectedItem()
            : null;
        if (loteSelecionado == null) {
            exibirErro("Selecione um lote na tabela para atualizar o status.");
            return;
        }

        StatusLote proximoStatus = obterProximoStatus(loteSelecionado.status());
        if (proximoStatus == null) {
            exibirErro(
                "Lotes com status Destinado já concluíram o ciclo de vida."
            );
            return;
        }

        StatusLote filtroStatus = obterFiltroStatus();
        CategoriaMaterial filtroCategoria = obterFiltroCategoria();
        UrgenciaDescarte filtroUrgencia = obterFiltroUrgencia();

        definirCarregando(true);
        exibirInfo(
            "Atualizando status do lote #" + loteSelecionado.id() + "..."
        );

        CompletableFuture.supplyAsync(
            () -> {
                LoteResiduoResponse loteAtualizado =
                    apiClient.atualizarStatusLote(
                        empresa.id(),
                        loteSelecionado.id(),
                        proximoStatus
                    );
                List<LoteResiduoResponse> lotes = apiClient.listarLotes(
                    empresa.id(),
                    filtroStatus,
                    filtroCategoria,
                    filtroUrgencia
                );
                return new ResultadoAtualizacaoStatus(loteAtualizado, lotes);
            },
            asyncExecutor
        ).whenComplete((resultado, erro) ->
            executarNaUiThread(() -> {
                definirCarregando(false);
                if (erro != null) {
                    exibirErro(
                        "Erro ao atualizar status do lote: " +
                            extrairMensagemErro(erro)
                    );
                    return;
                }

                atualizarTabelaEEstoque(
                    resultado.lotes(),
                    filtroStatus,
                    filtroCategoria != null || filtroUrgencia != null
                );
                selecionarLotePorId(resultado.loteAtualizado().id());
                exibirSucesso(
                    "Status do lote #" +
                        resultado.loteAtualizado().id() +
                        " atualizado para " +
                        formatarStatus(resultado.loteAtualizado().status()) +
                        "."
                );
            })
        );
    }

    private StatusLote obterFiltroStatus() {
        return cbFiltroStatus != null ? cbFiltroStatus.getValue() : null;
    }

    private CategoriaMaterial obterFiltroCategoria() {
        return cbFiltroCategoria != null ? cbFiltroCategoria.getValue() : null;
    }

    private UrgenciaDescarte obterFiltroUrgencia() {
        return cbFiltroUrgencia != null ? cbFiltroUrgencia.getValue() : null;
    }

    private void definirCarregando(boolean carregando) {
        if (piCarregando != null) {
            piCarregando.setVisible(carregando);
            piCarregando.setManaged(carregando);
        }
        if (btnRegistrar != null) btnRegistrar.setDisable(carregando);
        if (btnLimpar != null) btnLimpar.setDisable(carregando);
        if (btnBuscarUsuario != null) btnBuscarUsuario.setDisable(carregando);
        if (btnFiltrarEstoque != null) btnFiltrarEstoque.setDisable(carregando);
        if (btnLimparFiltros != null) btnLimparFiltros.setDisable(carregando);
        if (btnAtualizarEstoque != null) btnAtualizarEstoque.setDisable(carregando);
        if (btnAvancarStatus != null) {
            if (carregando) {
                btnAvancarStatus.setDisable(true);
            } else {
                LoteResiduoResponse selecionado = tblLotesEstoque != null
                    ? tblLotesEstoque.getSelectionModel().getSelectedItem()
                    : null;
                atualizarControlesStatusSelecionado(selecionado);
            }
        }
    }

    private void executarNaUiThread(Runnable acao) {
        if (Platform.isFxApplicationThread()) {
            acao.run();
        } else {
            Platform.runLater(acao);
        }
    }

    private String extrairMensagemErro(Throwable erro) {
        Throwable causa = erro instanceof CompletionException && erro.getCause() != null
            ? erro.getCause()
            : erro;
        if (causa instanceof ApiClientException apiEx) {
            return apiEx.getMessage();
        }
        return causa.getMessage() != null
            ? causa.getMessage()
            : "Erro inesperado ao comunicar com a API.";
    }

    private void atualizarResumoMassaTotal(BigDecimal totalKg) {
        if (lblMassaTotalEmpresa != null) {
            BigDecimal valor = totalKg != null ? totalKg : BigDecimal.ZERO;
            lblMassaTotalEmpresa.setText(
                valor.stripTrailingZeros().toPlainString() + " kg"
            );
        }
    }

    private void atualizarTabelaEEstoque(
        List<LoteResiduoResponse> lotes,
        StatusLote filtroStatus,
        boolean outrosFiltrosAtivos
    ) {
        List<LoteResiduoResponse> listaSegura = lotes != null ? lotes : List.of();
        if (tblLotesEstoque != null) {
            tblLotesEstoque.setItems(
                FXCollections.observableArrayList(listaSegura)
            );
            atualizarControlesStatusSelecionado(
                tblLotesEstoque.getSelectionModel().getSelectedItem()
            );
        }
        if (lblTituloMassaEstoque != null) {
            String tituloBase = switch (filtroStatus != null ? filtroStatus : StatusLote.EM_ESTOQUE) {
                case EM_ESTOQUE -> "EM ESTOQUE ATUAL";
                case EM_PROCESSAMENTO -> "EM PROCESSAMENTO";
                case DESTINADO -> "DESTINADOS";
            };
            if (outrosFiltrosAtivos) {
                tituloBase = switch (filtroStatus != null ? filtroStatus : StatusLote.EM_ESTOQUE) {
                    case EM_ESTOQUE -> "EM ESTOQUE (FILTRADO)";
                    case EM_PROCESSAMENTO -> "EM PROCESSAMENTO (FILTRADO)";
                    case DESTINADO -> "DESTINADOS (FILTRADO)";
                };
            }
            lblTituloMassaEstoque.setText(tituloBase);
        }
        if (lblMassaEmEstoque != null) {
            StatusLote statusAlvo = filtroStatus != null
                ? filtroStatus
                : StatusLote.EM_ESTOQUE;
            BigDecimal soma = listaSegura
                .stream()
                .filter(lote -> lote.status() == statusAlvo)
                .map(LoteResiduoResponse::pesoKg)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblMassaEmEstoque.setText(
                soma.stripTrailingZeros().toPlainString() + " kg"
            );
        }
    }

    private void selecionarLotePorId(Long loteId) {
        if (tblLotesEstoque == null || loteId == null) {
            return;
        }
        for (LoteResiduoResponse item : tblLotesEstoque.getItems()) {
            if (loteId.equals(item.id())) {
                tblLotesEstoque.getSelectionModel().select(item);
                atualizarControlesStatusSelecionado(item);
                return;
            }
        }
        atualizarControlesStatusSelecionado(null);
    }

    private void atualizarControlesStatusSelecionado(
        LoteResiduoResponse loteSelecionado
    ) {
        if (loteSelecionado == null) {
            if (lblLoteSelecionado != null) {
                lblLoteSelecionado.setText(
                    "Selecione um lote na tabela para atualizar o status."
                );
            }
            if (btnAvancarStatus != null) {
                btnAvancarStatus.setText("Avançar Status");
                btnAvancarStatus.setDisable(true);
            }
            return;
        }

        if (lblLoteSelecionado != null) {
            String pesoTexto = loteSelecionado.pesoKg() != null
                ? loteSelecionado.pesoKg().stripTrailingZeros().toPlainString()
                : "0";
            lblLoteSelecionado.setText(
                "Lote #" +
                    loteSelecionado.id() +
                    " — " +
                    loteSelecionado.tipoResiduo() +
                    " (" +
                    pesoTexto +
                    " kg) • Status: " +
                    formatarStatus(loteSelecionado.status())
            );
        }

        if (btnAvancarStatus != null) {
            StatusLote proximo = obterProximoStatus(loteSelecionado.status());
            if (proximo == StatusLote.EM_PROCESSAMENTO) {
                btnAvancarStatus.setText("Iniciar Processamento");
                btnAvancarStatus.setDisable(false);
            } else if (proximo == StatusLote.DESTINADO) {
                btnAvancarStatus.setText("Marcar como Destinado");
                btnAvancarStatus.setDisable(false);
            } else {
                btnAvancarStatus.setText("Lote Destinado");
                btnAvancarStatus.setDisable(true);
            }
        }
    }

    static StatusLote obterProximoStatus(StatusLote statusAtual) {
        if (statusAtual == null) {
            return null;
        }
        return switch (statusAtual) {
            case EM_ESTOQUE -> StatusLote.EM_PROCESSAMENTO;
            case EM_PROCESSAMENTO -> StatusLote.DESTINADO;
            case DESTINADO -> null;
        };
    }

    private void exibirErro(String mensagem) {
        if (lblMensagem != null) {
            lblMensagem.setStyle(
                "-fx-text-fill: #C62828; -fx-font-size: 12px; -fx-font-weight: bold;"
            );
            lblMensagem.setText(mensagem);
        }
    }

    private void exibirInfo(String mensagem) {
        if (lblMensagem != null) {
            lblMensagem.setStyle(
                "-fx-text-fill: #1565C0; -fx-font-size: 12px; -fx-font-weight: bold;"
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

    public static String formatarStatus(StatusLote status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case EM_ESTOQUE -> "Em Estoque";
            case EM_PROCESSAMENTO -> "Em Processamento";
            case DESTINADO -> "Destinado";
        };
    }

    private record DadosEstoqueCarregados(
        EmpresaResponse empresa,
        List<LoteResiduoResponse> lotes
    ) {}

    private record ResultadoRegistroRecepcao(
        LoteResiduoResponse loteRegistrado,
        EmpresaResponse empresaAtualizada,
        List<LoteResiduoResponse> lotesEmEstoque
    ) {}

    private record ResultadoAtualizacaoStatus(
        LoteResiduoResponse loteAtualizado,
        List<LoteResiduoResponse> lotes
    ) {}
}
