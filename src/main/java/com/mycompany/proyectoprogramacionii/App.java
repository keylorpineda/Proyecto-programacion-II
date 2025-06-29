package com.mycompany.proyectoprogramacionii;

import Utilities.DataBaseManager;
import Utilities.FlowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        DataBaseManager.init();
        Utilities.DataInitializer.initializeIfNeeded();
        FlowController.getInstance().initFlow(stage);
        FlowController.getInstance().goView("LoginWindow");

    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/FXML/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
