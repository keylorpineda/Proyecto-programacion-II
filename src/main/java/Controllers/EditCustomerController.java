package Controllers;

import Models.User;
import Services.UserService;
import Utilities.FlowController;
import Utilities.GraphicUtilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class EditCustomerController implements Initializable {

    @FXML private TextField    txtIdentificationToEdit;
    @FXML private TextField    txtNameToEdit;
    @FXML private TextField    txtLastNameToEdit;
    @FXML private TextField    txtUserToEdit;
    @FXML private PasswordField txtPasswordToEdit;
    @FXML private Button       btnCancelarEditado;
    @FXML private Button       btnGuardarEditado;

    private final UserService       userService = new UserService();
    private final GraphicUtilities  utilities   = new GraphicUtilities();
    private User                    currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // El campo de identificación no se edita
        txtIdentificationToEdit.setDisable(true);
        if (currentUser == null) {
            // Si no se cargó el usuario, mostramos error
            utilities.showAlert(AlertType.ERROR,
                                "Error",
                                "No se pudo cargar la información del usuario.");
        }
    }

    /** Será llamado desde el controlador padre para inyectar el User */
    public void setUser(User u) {
        this.currentUser = u;
        fillFields(u);
    }

    /** Rellena los campos con los datos actuales del usuario */
    private void fillFields(User u) {
        txtIdentificationToEdit.setText(String.valueOf(u.getId()));
        txtNameToEdit.setText(u.getName());
        txtLastNameToEdit.setText(u.getLastName());
        txtUserToEdit.setText(u.getName());
        txtPasswordToEdit.setText(u.getPassword());
    }

    /** Maneja el guardado de los cambios */
    @FXML
    private void clickGuardarEdit(ActionEvent event) {
        String nuevoNombre     = txtNameToEdit.getText().trim();
        String nuevoApellido   = txtLastNameToEdit.getText().trim();
        String nuevoUsername   = txtUserToEdit.getText().trim();
        String nuevaContrasena = txtPasswordToEdit.getText().trim();

        // Validaciones básicas
        if (nuevoNombre.isEmpty()
         || nuevoApellido.isEmpty()
         || nuevoUsername.isEmpty()
         || nuevaContrasena.isEmpty()) {
            utilities.showAlert(AlertType.WARNING,
                                "Campos incompletos",
                                "Todos los campos deben llenarse.");
            return;
        }
        // La contraseña debe tener 6 caracteres si cambia
        if (!nuevaContrasena.equals(currentUser.getPassword())
         && nuevaContrasena.length() != 6) {
            utilities.showAlert(AlertType.WARNING,
                                "Contraseña inválida",
                                "Debe tener exactamente 6 caracteres.");
            return;
        }
        // Comprobar unicidad de username
        User existente = userService.findByUserName(nuevoUsername);
        if (existente != null
         && !existente.getId().equals(currentUser.getId())) {
            utilities.showAlert(AlertType.ERROR,
                                "Usuario en uso",
                                "Ese nombre ya está tomado.");
            return;
        }

        // Aplicar cambios
        boolean huboCambios = false;
        if (!nuevoNombre.equals(currentUser.getName())) {
            currentUser.setName(nuevoNombre);
            huboCambios = true;
        }
        if (!nuevoApellido.equals(currentUser.getLastName())) {
            currentUser.setLastName(nuevoApellido);
            huboCambios = true;
        }
        if (!nuevoUsername.equals(currentUser.getName())) {
            currentUser.setName(nuevoUsername);
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

        // Persistir cambios usando el servicio
        try {
            userService.update(currentUser);
            utilities.showAlert(AlertType.INFORMATION,
                                "Éxito",
                                "Datos actualizados correctamente.");
        } catch (Exception e) {
            utilities.showAlert(AlertType.ERROR,
                                "Error",
                                "No se pudieron guardar los cambios.");
            e.printStackTrace();
        }
    }

    /** Cancela la edición y vuelve atrás */
    @FXML
    private void clickCancelarEdit(ActionEvent event) {
        utilities.showAlert(AlertType.INFORMATION,
                            "Cancelado",
                            "No se realizaron cambios.");
        try {
            FlowController.getInstance().goBack();
        } catch (Exception e) {
            utilities.showAlert(AlertType.ERROR,
                                "Error",
                                "No se pudo cerrar la ventana.");
            e.printStackTrace();
        }
    }
}