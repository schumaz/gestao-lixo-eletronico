package com.residuos.lixoeletronico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class LixoEletronicoApplication extends Application {

	private ConfigurableApplicationContext springContext;

	@Override
	public void init() throws Exception {
		springContext = SpringApplication.run(LixoEletronicoApplication.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
		fxmlLoader.setControllerFactory(springContext::getBean);
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Login");
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception {
		springContext.close();
	}

	public static void main(String[] args) {
		SpringApplication.run(LixoEletronicoApplication.class, args);
	}

}
