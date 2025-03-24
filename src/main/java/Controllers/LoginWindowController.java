/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import com.mycompany.proyectoprogramacionii.App;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Keylo
 */
public class LoginWindowController implements Initializable {


    @FXML
    private TextField txtuserNameLogin;
    @FXML
    private PasswordField txtuserPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnCreateAccount;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    private void LoginAccount(ActionEvent event) {
    }

    @FXML
    private void CreateAccount(ActionEvent event) throws IOException {
        App.setRoot("SignUpWindow");
    }

}
