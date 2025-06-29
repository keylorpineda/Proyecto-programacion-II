package Controllers;

import Models.User;
import Services.UserService;
import Utilities.FlowController;
import Utilities.graphicUtilities;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class EditCustomerController implements Initializable {

    @FXML
    private Button btnCancelarEditado;

    @FXML
    private Button btnGuardarEditado;

    @FXML
    private TextField txtIdentificationToEdit;

    @FXML
    private TextField txtLastNameToEdit;

    @FXML
    private TextField txtNameToEdit;

    @FXML
    private TextField txtPasswordToEdit;

    @FXML
    private TextField txtUserToEdit;

    private final UserService userService = new UserService();
    private final graphicUtilities utilities = new graphicUtilities();
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdentificationToEdit.setDisable(true);

        currentUser = UserService.getCurrentUser();
        if (currentUser != null) {
            fillFields(currentUser);
        } else {
            utilities.showAlert(AlertType.ERROR,
                    "Error",
                    "No se pudo cargar la información del usuario.");
        }
    }

    private void fillFields(User u) {
        txtIdentificationToEdit.setText(String.valueOf(u.getId()));
        txtNameToEdit.setText(u.getName());
        txtLastNameToEdit.setText(u.getLastName());
        txtUserToEdit.setText(u.getUserName());
        txtPasswordToEdit.setText(u.getPassword());
    }

    @FXML
    private void clickGuardarEdit(ActionEvent event) throws IOException {
        String nuevoNombre = txtNameToEdit.getText().trim();
        String nuevoApellido = txtLastNameToEdit.getText().trim();
        String nuevoUsername = txtUserToEdit.getText().trim();
        String nuevaContrasena = txtPasswordToEdit.getText().trim();

        if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty()
                || nuevoUsername.isEmpty() || nuevaContrasena.isEmpty()) {
            utilities.showAlert(AlertType.WARNING,
                    "Campos incompletos",
                    "Todos los campos deben llenarse.");
            return;
        }
        if (nuevaContrasena.length() > 6) {
            utilities.showAlert(AlertType.WARNING,
                    "Contraseña inválida",
                    "La contraseña no debe tener más de 6 caracteres.");
            return;
        }
        User existente = userService.findByUserName(nuevoUsername);
        if (existente != null && !existente.getId().equals(currentUser.getId())) {
            utilities.showAlert(AlertType.ERROR,
                    "Usuario en uso",
                    "Ese nombre de usuario ya está registrado.");
            return;
        }
        boolean huboCambios = false;
        if (!nuevoNombre.equals(currentUser.getName())) {
            currentUser.setName(nuevoNombre);
            huboCambios = true;
        }
        if (!nuevoApellido.equals(currentUser.getLastName())) {
            currentUser.setLastName(nuevoApellido);
            huboCambios = true;
        }
        if (!nuevoUsername.equals(currentUser.getUserName())) {
            currentUser.setUserName(nuevoUsername);
            huboCambios = true;
        }
        if (!nuevaContrasena.equals(currentUser.getPassword())) {
            currentUser.setPassword(nuevaContrasena);
            huboCambios = true;
        }
        if (!huboCambios) {
            utilities.showAlert(AlertType.INFORMATION,
                    "Sin cambios",
                    "No hay nada para guardar.");
            return;
        }
        try {
            userService.update(currentUser);
            utilities.showAlert(AlertType.INFORMATION,
                    "Éxito",
                    "Datos actualizados correctamente.");
            FlowController.getInstance().goView("UserViewWindow");
        } catch (Exception e) {
            utilities.showAlert(AlertType.ERROR,
                    "Error",
                    "No se pudieron guardar los cambios.");
            e.printStackTrace();
        }
    }
    @FXML
    private void clickCancelarEdit(ActionEvent event) throws IOException {
        utilities.showAlert(AlertType.INFORMATION,
                "Cancelado",
                "No se realizaron cambios.");
        User original = userService.findByIdentification(currentUser.getId());

        if (original != null) {
            currentUser = original;       
            fillFields(currentUser);      
        } else {
            utilities.showAlert(AlertType.ERROR,
                    "Error",
                    "No se pudo recargar la información del usuario.");
        }
        FlowController.getInstance().goView("UserViewWindow");
    }
}
