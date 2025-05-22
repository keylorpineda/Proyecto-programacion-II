package Controllers;

import Models.Customer;
import Services.UserManager;
import Utilities.FlowController;
import com.mycompany.proyectoprogramacionii.App;
import Utilities.graphicUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpWindowController implements Initializable {
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtUserId;
    @FXML
    private TextField txtUserName;
    @FXML
    private PasswordField txtUserPassword;
    @FXML
    private Button btnCreateAccount;
    @FXML
    private Button btnBack;

    private graphicUtilities utilities;
    private UserManager userManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilities = new graphicUtilities();
        userManager = UserManager.getInstance();
    }

    @FXML
    private void clickOnCreate(ActionEvent event) throws IOException {
        String name = txtName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String id = txtUserId.getText().trim();
        String userName = txtUserName.getText().trim();
        String password = txtUserPassword.getText().trim();

        if (name.isEmpty() || lastName.isEmpty() || id.isEmpty() || userName.isEmpty() || password.isEmpty()) {
            utilities.showAlert(AlertType.ERROR, "Campos incompletos", "Por favor, complete todos los campos.");
            return;
        }

        if (password.length() < 6) {
            utilities.showAlert(AlertType.ERROR, "Contraseña débil", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (userManager.getUserByIdentification(id) != null) {
            utilities.showAlert(AlertType.ERROR, "Cédula duplicada", "Ya existe un usuario con esta cédula.");
            return;
        }

        if (userManager.getUserByUsername(userName) != null) {
            utilities.showAlert(AlertType.ERROR, "Nombre de usuario duplicado", "El nombre de usuario ya está en uso.");
            return;
        }

        Customer newCustomer = new Customer(name, lastName, id, userName, password);
        userManager.addUser(newCustomer); 

        utilities.showAlert(AlertType.INFORMATION, "Usuario creado", "Usuario creado exitosamente.");
        FlowController.getInstance().goView("LoginWindow");
    }

    @FXML
    private void backWindow(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("LoginWindow");
    }
}
