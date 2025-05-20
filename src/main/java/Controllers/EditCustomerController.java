package Controllers;

import Models.User;
import Services.UserManager;
import Utilities.DataBaseManager;
import Utilities.FlowController;
import Utilities.graphicUtilities;
import jakarta.persistence.EntityManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class EditCustomerController implements Initializable {

    @FXML private TextField txtIdentificationToEdit;
    @FXML private TextField txtNameToEdit;
    @FXML private TextField txtLastNameToEdit;
    @FXML private TextField txtUserToEdit;
    @FXML private TextField txtPasswordToEdit;
    @FXML private Button btnSearchUser;
    @FXML private Button btnCancelarEditado;
    @FXML private Button btnGuardarEditado;

    private final UserManager userManager       = UserManager.getInstance();
    private final graphicUtilities utilities    = new graphicUtilities();

    private User currentUser;
    private User originalUserData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setFieldsDisabled(true);
    }

    public void setUser(User u) {
        this.currentUser = u;
        this.originalUserData = new User(
            u.getName(), u.getLastName(), u.getIdentification(),
            u.getUserName(), u.getPassword()
        );
        txtIdentificationToEdit.setText(u.getIdentification());
        txtNameToEdit.setText(u.getName());
        txtLastNameToEdit.setText(u.getLastName());
        txtUserToEdit.setText(u.getUserName());
        txtPasswordToEdit.setText(u.getPassword());
        setFieldsDisabled(false);
    }

    private void setFieldsDisabled(boolean disabled) {
        // El campo de cédula lo dejamos siempre editable para buscar
        txtNameToEdit     .setDisable(disabled);
        txtLastNameToEdit .setDisable(disabled);
        txtUserToEdit     .setDisable(disabled);
        txtPasswordToEdit .setDisable(disabled);
        btnGuardarEditado .setDisable(disabled);
    }

    @FXML
    private void clickBuscarUsuario(ActionEvent event) {
        String cedula = txtIdentificationToEdit.getText().trim();
        if (cedula.isEmpty()) {
            utilities.showAlert(AlertType.WARNING, "Campo requerido", "Ingrese la cédula.");
            return;
        }
        User found = userManager.getUserByIdentification(cedula);
        if (found != null) {
            setUser(found);
        } else {
            utilities.showAlert(AlertType.INFORMATION, "Usuario no encontrado", "No existe usuario con esa cédula.");
        }
    }

    @FXML
    private void clickGuardarEdit(ActionEvent event) {
        if (currentUser == null) {
            utilities.showAlert(AlertType.ERROR, "Error", "Debe buscar un usuario primero.");
            return;
        }
        String nuevoNombre    = txtNameToEdit.getText().trim();
        String nuevoApellido  = txtLastNameToEdit.getText().trim();
        String nuevoUsuario   = txtUserToEdit.getText().trim();
        String nuevaContrasena= txtPasswordToEdit.getText().trim();
        String identificacion = txtIdentificationToEdit.getText().trim();

        if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() ||
            nuevoUsuario.isEmpty() || nuevaContrasena.isEmpty()) {
            utilities.showAlert(AlertType.WARNING, "Campos incompletos", "Todos los campos deben llenarse.");
            return;
        }
        if (identificacion.length() != 9 || !identificacion.matches("\\d{9}")) {
            utilities.showAlert(AlertType.WARNING, "Cédula inválida", "Debe tener 9 dígitos numéricos.");
            return;
        }
        if (!nuevaContrasena.equals(currentUser.getPassword()) && nuevaContrasena.length() != 6) {
            utilities.showAlert(AlertType.WARNING, "Contraseña inválida", "Debe tener exactamente 6 caracteres.");
            return;
        }
        User existente = userManager.getUserByIdentification(nuevoUsuario);
        if (existente != null && !existente.getIdentification().equals(currentUser.getIdentification())) {
            utilities.showAlert(AlertType.ERROR, "Usuario en uso", "Ese nombre ya está tomado.");
            return;
        }

        boolean huboCambios = false;
        if (!nuevoNombre.equals(currentUser.getName())) {
            currentUser.setName(nuevoNombre); huboCambios = true;
        }
        if (!nuevoApellido.equals(currentUser.getLastName())) {
            currentUser.setLastName(nuevoApellido); huboCambios = true;
        }
        if (!nuevoUsuario.equals(currentUser.getUserName())) {
            currentUser.setUserName(nuevoUsuario); huboCambios = true;
        }
        if (!nuevaContrasena.equals(currentUser.getPassword())) {
            currentUser.setPassword(nuevaContrasena); huboCambios = true;
        }
        if (!huboCambios) {
            utilities.showAlert(AlertType.INFORMATION, "Sin cambios", "No hay nada para guardar.");
            return;
        }

        EntityManager em = DataBaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(currentUser);
            em.getTransaction().commit();
            utilities.showAlert(AlertType.INFORMATION, "Éxito", "Datos actualizados correctamente.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            utilities.showAlert(AlertType.ERROR, "Error", "No se pudieron guardar los cambios.");
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @FXML
    private void clickCancelarEdit(ActionEvent event) {
        utilities.showAlert(AlertType.INFORMATION, "Cancelado", "No se realizaron cambios.");
        try {
            FlowController.getInstance().goBack();
        } catch (Exception e) {
            utilities.showAlert(AlertType.ERROR, "Error", "No se pudo cerrar la ventana.");
            e.printStackTrace();
        }
    }
}