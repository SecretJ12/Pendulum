package de.secretj12.Hauptfenster;

import de.secretj12.Pendel.PendelConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Hauptfenster extends Application {
	public static Hauptfenster_controller controller;
	public static Stage stage;
	public static PendelConnection pendel;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("PendelUI.fxml"));
		Parent root = loader.load();
		Hauptfenster.stage = primaryStage;
		primaryStage.setTitle("Pendel");
		primaryStage.setScene(new Scene(root, 900, 450));
		primaryStage.setMinHeight(480);
		primaryStage.setMinWidth(670);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
		
		Hauptfenster.controller = loader.getController();
		stage.setOnCloseRequest(Hauptfenster.controller);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(new String[] {});
	}
}