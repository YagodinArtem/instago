package ru;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import ru.yar.controller.Controller;

public class App extends Application {

    public static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("prop.fxml"));
        Parent parent = loader.load();
        controller = loader.getController();
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        Scene main = new Scene(parent);
        primaryStage.setScene(main);
        primaryStage.setTitle("INSTAGO");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
