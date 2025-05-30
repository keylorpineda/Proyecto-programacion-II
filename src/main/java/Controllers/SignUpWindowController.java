package Controllers;

import Models.Customer;
import Models.User;
import Services.UserService;
import Utilities.graphicUtilities;
import Utilities.FlowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilities = new graphicUtilities();
        userService = new UserService();
    }

    @FXML
    private void clickOnCreate(ActionEvent event) {
        String name = txtName.getText().trim();
        String lastName = txtLastName.getText().trim();
        Long id = Long.parseLong(txtUserId.getText().trim());
        String userName = txtUserName.getText().trim();
        String password = txtUserPassword.getText().trim();
        String idText = txtUserId.getText().trim();

        // 1) Validaciones
        if (name.isEmpty() || lastName.isEmpty() || idText.isEmpty()
                || userName.isEmpty() || password.isEmpty()) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Campos incompletos",
                    "Por favor complete todos los campos.");
            return;
        }
        if (password.length() < 6) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Contraseña débil",
                    "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (userService.findByIdentification(id) != null) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Cédula duplicada",
                    "Ya existe un usuario con esta cédula.");
            return;
        }
        if (userService.findByUserName(userName) != null) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Nombre de usuario duplicado",
                    "El nombre de usuario ya está en uso.");
            return;
        }

        User newUser = new Customer(Long.valueOf(id), name, lastName, userName, password);
        try {
            userService.save(newUser);
            UserService.setCurrentUser(newUser);
            utilities.showAlert(Alert.AlertType.INFORMATION,
                    "Usuario creado",
                    "Usuario creado exitosamente.");
            FlowController.getInstance().goView("LoginWindow");

        } catch (EntityExistsException ex) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Cédula duplicada",
                    "Ya existe un usuario con esta cédula.");
        } catch (PersistenceException ex) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Nombre de usuario duplicado",
                    "El nombre de usuario ya está en uso.");
        } catch (IOException ex) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo cargar la ventana de login.");
            ex.printStackTrace();
        }
    }

    @FXML
    private void backWindow(ActionEvent event) {
        try {
            FlowController.getInstance().goView("LoginWindow");
        } catch (IOException ex) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo regresar a la ventana de login.");
            ex.printStackTrace();
        }
    }
}
