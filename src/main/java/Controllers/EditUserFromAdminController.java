package Controllers;

import Models.User;
import Services.UserService;
import Utilities.FlowController;
import Utilities.graphicUtilities;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class EditUserFromAdminController implements Initializable {

    @FXML private TextField txtNombre, txtApellido, txtUsuario;
    @FXML private PasswordField txtContrasena;

    private final UserService userService = new UserService();
    private final graphicUtilities utilities = new graphicUtilities();
    private User user;
    private User copiaOriginal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        user = UserService.getCurrentUser(); // Obtenemos el usuario a editar
            txtNombre.setText(user.getName());
            txtApellido.setText(user.getLastName());
            txtUsuario.setText(user.getUserName());
            txtContrasena.setText(user.getPassword());
        
    }
    private void fillFields(User u) {
        txtNombre.setText(u.getName());
        txtApellido.setText(u.getLastName());
        txtUsuario.setText(u.getUserName());
        txtContrasena.setText(u.getPassword());
    }

    @FXML
    private void clickGuardar(ActionEvent event) throws IOException {
        String nuevoNombre = txtNombre.getText().trim();
        String nuevoApellido = txtApellido.getText().trim();
        String nuevoUsuario = txtUsuario.getText().trim();
        String nuevaClave = txtContrasena.getText().trim();

        if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevoUsuario.isEmpty() || nuevaClave.isEmpty()) {
            utilities.showAlert(Alert.AlertType.WARNING, "Campos incompletos", "Todos los campos son obligatorios.");
            return;
        }

        if (nuevaClave.length() > 6) {
            utilities.showAlert(Alert.AlertType.WARNING, "Contraseña inválida", "Debe tener máximo 6 caracteres.");
            return;
        }

        User existente = userService.findByUserName(nuevoUsuario);
        if (existente != null && !existente.getId().equals(user.getId())) {
            utilities.showAlert(Alert.AlertType.ERROR, "Usuario en uso", "Ese nombre de usuario ya está registrado.");
            return;
        }

        user.setName(nuevoNombre);
        user.setLastName(nuevoApellido);
        user.setUserName(nuevoUsuario);
        user.setPassword(nuevaClave);

        userService.update(user);
        utilities.showAlert(Alert.AlertType.INFORMATION, "Éxito", "Usuario actualizado correctamente.");

      FlowController flow = FlowController.getInstance();
    flow.goView("AdminPrincipalWindow");
 
    AdminPrincipalWindowController ctrl = (AdminPrincipalWindowController) flow.getController("AdminPrincipalWindow");
    ctrl.refrescarVista();   
    }

    @FXML
    private void clickCancelar(ActionEvent event) throws IOException {
        utilities.showAlert(Alert.AlertType.INFORMATION,
                "Cancelado",
                "No se realizaron cambios.");
        User original = userService.findByIdentification(user.getId());

        if (original != null) {
            user = original;       
            fillFields(user);      
        } else {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo recargar la información del usuario.");
        }
        FlowController.getInstance().goView("AdminPrincipalWindow");
    }
    }
    
    
 

