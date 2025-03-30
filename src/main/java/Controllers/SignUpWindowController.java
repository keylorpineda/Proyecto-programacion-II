/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import Models.User;
import com.mycompany.proyectoprogramacionii.Utilities;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Keylo
 */
public class SignUpWindowController implements Initializable {
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtId;
    @FXML
    private TextField txtUserName;
    @FXML
    private TextField txtPassword;
    @FXML
    private RadioButton rdbCustomer;
    @FXML
    private RadioButton rdbAdministrator;
    @FXML
    private Button btnCreateAccount;
    @FXML
    private ToggleGroup roles;

    private Utilities utilities;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilities = new Utilities();
        roles = new ToggleGroup();
    }

    @FXML
    private void clickOnCreate(ActionEvent event) {
        rdbCustomer.setToggleGroup(roles);
        rdbAdministrator.setToggleGroup(roles);
        String name = txtName.getText().trim(); 
        String lastName = txtLastName.getText().trim();
        String id = txtId.getText().trim();
        String userName = txtUserName.getText().trim();
        String password = txtPassword.getText().trim();
        String role = "";

        if (name.isEmpty() || lastName.isEmpty() || id.isEmpty() || userName.isEmpty() || password.isEmpty()) {
            utilities.showAlert(AlertType.ERROR, "Campos incompletos", "Por favor, complete todos los campos.");
         return;
        }
        if (rdbCustomer.isSelected()) {
            role = "CUSTOMER";
        } else if (rdbAdministrator.isSelected()) {
            role = "ADMINISTRATOR";
        } else {
            utilities.showAlert(AlertType.ERROR, "Rol no seleccionado", "Por favor, seleccione un rol.");
            return;
        }

        if (password.length() < 6) {
            utilities.showAlert(AlertType.ERROR, "Contraseña debil", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        User newUser = new User(name, lastName, id, userName, password, role);
        utilities.showAlert(AlertType.INFORMATION, "Usuario creado", "Usuario creado exitosamente.");
    }

}
