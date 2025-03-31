/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import Models.UserManager;
import com.mycompany.proyectoprogramacionii.App;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.mycompany.proyectoprogramacionii.Utilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Keylo
 */
public class LoginWindowController implements Initializable {

    Utilities utilities;
    private UserManager userManager;
    @FXML
    private TextField txtUserNameLogin;
    @FXML
    private PasswordField txtUserPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnCreateAccount;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilities = new Utilities();
        userManager = UserManager.getInstance();
    }    
    
    @FXML
    private void LoginAccount(ActionEvent event) {
        String username = txtUserNameLogin.getText();
        String password = txtUserPassword.getText();
        if (userManager.authenticateUser(username,password)) {

        }
        else {
            utilities.showAlert(Alert.AlertType.ERROR, "Error al iniciar sesion", "Datos de ingreso incorrectos");
        }
    }

    @FXML
    private void CreateAccount(ActionEvent event) throws IOException {
        App.setRoot("SignUpWindow");
    }

}
