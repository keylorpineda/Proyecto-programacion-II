/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Keylo
 */
public class FlowController {

    private static FlowController instance;

    private Stage mainStage;

    private final Map<String, Scene> screenCache = new HashMap<>();

    private final Map<String, FXMLLoader> loaderCache = new HashMap<>();

    private final Stack<String> history = new Stack<>();

    private FlowController() {
    }

    public static FlowController getInstance() {
        if (instance == null) {
            instance = new FlowController();
        }
        return instance;
    }

    public void initFlow(Stage stage) {
        this.mainStage = stage;
    }

    public void goView(String viewName) throws IOException {
        if (mainStage.getScene() != null) {
            String current = getCurrentViewName();
            if (current != null) {
                history.push(current);
            }
        }

        Scene scene = screenCache.get(viewName);
        if (scene == null) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + viewName + ".fxml")
            );
            Parent root = loader.load();
            scene = new Scene(root);
            screenCache.put(viewName, scene);
            loaderCache.put(viewName, loader);
        }

        mainStage.setScene(scene);
        mainStage.sizeToScene();
        mainStage.show();
    }

    public void goBack() {
        if (!history.isEmpty()) {
            String previous = history.pop();
            Scene scene = screenCache.get(previous);
            if (scene != null) {
                mainStage.setScene(scene);
                mainStage.sizeToScene();
                mainStage.show();
            }
        }
    }

    public Object openModal(String viewName) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/" + viewName + ".fxml")
        );
        Parent root = loader.load();
        Stage dialog = new Stage();
        dialog.initOwner(mainStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(root));
        dialog.sizeToScene();
        dialog.showAndWait();
        return loader.getController();
    }

    @SuppressWarnings("unchecked")
    public <T> T getController(String viewName) {
        FXMLLoader loader = loaderCache.get(viewName);
        return (loader != null) ? (T) loader.getController() : null;
    }

    private String getCurrentViewName() {
        Scene actual = mainStage.getScene();
        for (Map.Entry<String, Scene> entry : screenCache.entrySet()) {
            if (entry.getValue() == actual) {
                return entry.getKey();
            }
        }
        return null;
    }
}
