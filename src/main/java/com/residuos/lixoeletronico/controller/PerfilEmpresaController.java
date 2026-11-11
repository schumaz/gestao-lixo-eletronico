package com.residuos.lixoeletronico.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.residuos.lixoeletronico.config.UserSession;
import com.residuos.lixoeletronico.dto.empresa.ContatoDTO;
import com.residuos.lixoeletronico.dto.empresa.EmpresaResponse;
import com.residuos.lixoeletronico.dto.empresa.EnderecoDTO;
import com.residuos.lixoeletronico.dto.empresa.HorarioFuncionamentoDTO;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

@Component
public class PerfilEmpresaController implements Initializable {

    @FXML
    private Label lblNomeEmpresa;

    @FXML
    private Label lblQuemSomos;

    @FXML
    private Label lblPorQueReciclamos;

    @FXML
    private Label lblPasso1Descarte;

    @FXML
    private Label lblPasso2Descarte;

    @FXML
    private Label lblListaEnderecos;

    @FXML
    private Label lblStatToneladas;

    @FXML
    private Label lblStatToneladasDesc;

    @FXML
    private Label lblSloganEmpresa;

    @FXML
    private Label lblWhatsapp;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblEndereco;

    @FXML
    private Label lblHorarioSemana;

    @FXML
    private Label lblHorarioSabado;

    @FXML
    private Label lblHorarioDomingo;

    private final UserSession userSession;
    private final ApplicationContext applicationContext;

    public PerfilEmpresaController(UserSession userSession, ApplicationContext applicationContext) {
        this.userSession = userSession;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EmpresaResponse empresa = userSession.getEmpresaLogada();
        if (empresa != null) {
            preencherDadosPerfil(empresa);
        }
    }

    public void preencherDadosPerfil(EmpresaResponse empresa) {
        if (lblNomeEmpresa != null && empresa.nome() != null) {
            lblNomeEmpresa.setText(empresa.nome());
        }

        if (lblQuemSomos != null && empresa.quemSomos() != null && !empresa.quemSomos().isBlank()) {
            lblQuemSomos.setText(empresa.quemSomos());
        }

        if (lblPorQueReciclamos != null && empresa.porQueReciclamos() != null && !empresa.porQueReciclamos().isBlank()) {
            lblPorQueReciclamos.setText(empresa.porQueReciclamos());
        }

        if (lblPasso1Descarte != null && empresa.instrucoesDescarte() != null && !empresa.instrucoesDescarte().isBlank()) {
            lblPasso1Descarte.setText(empresa.instrucoesDescarte());
        }

        if (lblEmail != null && empresa.email() != null) {
            lblEmail.setText(empresa.email());
        }

        ContatoDTO contato = empresa.contato();
        if (contato != null) {
            if (lblWhatsapp != null && contato.whatsapp() != null && !contato.whatsapp().isBlank()) {
                lblWhatsapp.setText("WhatsApp: " + contato.whatsapp());
            } else if (lblWhatsapp != null && contato.telefone() != null && !contato.telefone().isBlank()) {
                lblWhatsapp.setText("Telefone: " + contato.telefone());
            }
        }

        EnderecoDTO endereco = empresa.endereco();
        if (endereco != null) {
            StringBuilder sb = new StringBuilder();
            if (endereco.logradouro() != null) sb.append(endereco.logradouro());
            if (endereco.numero() != null) sb.append(", ").append(endereco.numero());
            if (endereco.bairro() != null) sb.append(" - ").append(endereco.bairro());
            if (endereco.cidade() != null) sb.append(", ").append(endereco.cidade());
            if (endereco.uf() != null) sb.append(" - ").append(endereco.uf());
            if (endereco.cep() != null) sb.append(", ").append(endereco.cep());

            String endFormatado = sb.toString();
            if (!endFormatado.isBlank()) {
                if (lblEndereco != null) lblEndereco.setText(endFormatado);
                if (lblListaEnderecos != null) lblListaEnderecos.setText("• " + endFormatado);
            }
        }

        formatarEstatisticasDescarte(empresa.totalKgDescartado());
        formatarHorarios(empresa.horarios());
    }

    private void formatarEstatisticasDescarte(BigDecimal totalKg) {
        if (lblStatToneladas == null) return;

        if (totalKg == null || totalKg.compareTo(BigDecimal.ZERO) == 0) {
            lblStatToneladas.setText("0 kg");
            if (lblStatToneladasDesc != null) {
                lblStatToneladasDesc.setText("de resíduo eletrônico destinados corretamente");
            }
            return;
        }

        if (totalKg.compareTo(new BigDecimal("1000")) >= 0) {
            BigDecimal toneladas = totalKg.divide(new BigDecimal("1000"), 1, RoundingMode.HALF_UP);
            lblStatToneladas.setText(toneladas.stripTrailingZeros().toPlainString() + " t");
            if (lblStatToneladasDesc != null) {
                lblStatToneladasDesc.setText("de resíduo eletrônico recebidas e destinadas");
            }
        } else {
            lblStatToneladas.setText(totalKg.stripTrailingZeros().toPlainString() + " kg");
            if (lblStatToneladasDesc != null) {
                lblStatToneladasDesc.setText("de resíduo eletrônico destinados corretamente");
            }
        }
    }

    private void formatarHorarios(List<HorarioFuncionamentoDTO> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

        for (HorarioFuncionamentoDTO h : horarios) {
            String intervalo = h.abertura().format(dtf) + " às " + h.fechamento().format(dtf);

            if (h.diaSemana() == DayOfWeek.SATURDAY && lblHorarioSabado != null) {
                lblHorarioSabado.setText(intervalo);
            } else if (h.diaSemana() == DayOfWeek.SUNDAY && lblHorarioDomingo != null) {
                lblHorarioDomingo.setText(intervalo);
            } else if (lblHorarioSemana != null && (h.diaSemana() == DayOfWeek.MONDAY || h.diaSemana() == DayOfWeek.FRIDAY)) {
                lblHorarioSemana.setText(intervalo);
            }
        }
    }

    @FXML
    public void handleLogout() {
        userSession.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) lblNomeEmpresa.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sistema de Gestão de Lixo Eletrônico");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
