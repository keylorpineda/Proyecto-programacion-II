package Controllers;

import Models.User;
import Services.UserManager;
import Utilities.DataBaseManager;
import Utilities.FlowController;
import Utilities.graphicUtilities;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class EditCustomerController {

    @FXML
    private TextField txtIdentificationToEdit;
    @FXML
    private TextField txtNameToEdit;
    @FXML
    private TextField txtLastNameToEdit;
    @FXML
    private TextField txtUserToEdit;
    @FXML
    private TextField txtPasswordToEdit;
    @FXML
    private Button btnSearchUser;
    @FXML
    private Button btnCancelarEditado;
    @FXML
    private Button btnGuardarEditado;

    private UserManager userManager = UserManager.getInstance();
    private graphicUtilities utilities = new graphicUtilities();
    private User currentUser;
    private User originalUserData;

    @FXML
    private void clickBuscarUsuario(ActionEvent event) {
        String cedula = txtIdentificationToEdit.getText().trim();

        if (cedula.isEmpty()) {
            utilities.showAlert(Alert.AlertType.WARNING, "Campo requerido", "Ingrese la cédula.");
            return;
        }

        currentUser = userManager.getUserByIdentification(cedula);
        if (currentUser != null) {
            originalUserData = new User(
                    currentUser.getName(),
                    currentUser.getLastName(),
                    currentUser.getIdentification(),
                    currentUser.getUserName(),
                    currentUser.getPassword()
            );

            // Mostrar datos
            txtNameToEdit.setText(currentUser.getName());
            txtLastNameToEdit.setText(currentUser.getLastName());
            txtUserToEdit.setText(currentUser.getUserName());
            txtPasswordToEdit.setText(currentUser.getPassword());
        } else {
            utilities.showAlert(Alert.AlertType.INFORMATION, "Usuario no encontrado", "No existe ningún usuario con esa cédula.");
        }
    }

    @FXML
    private void clickGuardarEdit(ActionEvent event) {
        if (currentUser == null) {
            utilities.showAlert(Alert.AlertType.ERROR, "Error", "Debe buscar un usuario primero.");
            return;
        }

        String nuevoNombre = txtNameToEdit.getText().trim();
        String nuevoApellido = txtLastNameToEdit.getText().trim();
        String nuevoUsuario = txtUserToEdit.getText().trim();
        String nuevaContrasena = txtPasswordToEdit.getText().trim();
        String identificacion = txtIdentificationToEdit.getText().trim();

        if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevoUsuario.isEmpty() || nuevaContrasena.isEmpty()) {
            utilities.showAlert(Alert.AlertType.WARNING, "Campos incompletos", "Todos los campos deben estar llenos.");
            return;
        }

        if (identificacion.length() != 9 || !identificacion.matches("\\d{9}")) {
            utilities.showAlert(Alert.AlertType.WARNING, "Cédula inválida", "La cédula debe tener exactamente 9 dígitos numéricos.");
            return;
        }

        if (!nuevaContrasena.equals(currentUser.getPassword()) && nuevaContrasena.length() != 6) {
            utilities.showAlert(Alert.AlertType.WARNING, "Contraseña inválida", "La contraseña debe tener exactamente 6 caracteres.");
            return;
        }

        User existente = userManager.getUserByUsername(nuevoUsuario);
        if (existente != null && !existente.getIdentification().equals(currentUser.getIdentification())) {
            utilities.showAlert(Alert.AlertType.ERROR, "Nombre de usuario en uso", "Ese nombre de usuario ya pertenece a otro usuario.");
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

        if (!nuevoUsuario.equals(currentUser.getUserName())) {
            currentUser.setUserName(nuevoUsuario);
            huboCambios = true;
        }

        if (!nuevaContrasena.equals(currentUser.getPassword())) {
            currentUser.setPassword(nuevaContrasena);
            huboCambios = true;
        }

        if (!huboCambios) {
            utilities.showAlert(Alert.AlertType.INFORMATION, "Sin cambios", "No se detectaron cambios para guardar.");
            return;
        }

        try (var em = DataBaseManager.getEntityManager()) {
            em.getTransaction().begin();
            em.merge(currentUser);
            em.getTransaction().commit();
            utilities.showAlert(Alert.AlertType.INFORMATION, "Éxito", "Datos actualizados correctamente.");
        } catch (Exception e) {
            utilities.showAlert(Alert.AlertType.ERROR, "Error", "Ocurrió un error al guardar los cambios.");
            e.printStackTrace();
        }
    }

    @FXML
    private void clickCancelarEdit(ActionEvent event) {
        utilities.showAlert(Alert.AlertType.INFORMATION, "Edición cancelada", "No se realizaron cambios.");

        try {
            FlowController.getInstance().goView("LoginWindow");
        } catch (Exception e) {
            utilities.showAlert(Alert.AlertType.ERROR, "Error", "No se pudo volver al menú principal.");
            e.printStackTrace();
        }
    }
}
