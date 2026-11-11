package com.residuos.lixoeletronico;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class LixoEletronicoApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        // Inicializa o Spring Boot antes de construir a interface gráfica
        this.springContext = new SpringApplicationBuilder(LixoEletronicoApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));

        // Delega ao Spring a criação das instâncias dos Controllers do FXML
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistema de Gestão de Lixo Eletrônico");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Encerra o contexto do Spring e a Thread do JavaFX ao fechar a janela
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        // Lança o ciclo de vida do JavaFX (que executará o init() e o start())
        Application.launch(LixoEletronicoApplication.class, args);
    }
}