package Controllers;

import Utilities.FlowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class InfoWindowController {

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FlowController.getInstance().goView("LoginWindow");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
