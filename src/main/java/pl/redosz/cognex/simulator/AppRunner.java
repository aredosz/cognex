package pl.redosz.cognex.simulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppRunner extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/scenes/main.fxml"));
        primaryStage.setTitle("COGNEX simulator");
        primaryStage.setScene(new Scene(root, 640, 550));
        primaryStage.getIcons().add(new Image("/images/cognex_simulator.png"));
        primaryStage.show();
    }
}
