package Controllers;

import Models.User;
import Services.UserService;
import Utilities.graphicUtilities;
import Utilities.FlowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginWindowController implements Initializable {

    @FXML
    private TextField txtUserNameLogin;
    @FXML
    private PasswordField txtUserPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnCreateAccount;

    private graphicUtilities utilities;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilities = new graphicUtilities();
        userService = new UserService();
    }

    @FXML
    private void LoginAccount(ActionEvent event) {
        String username = txtUserNameLogin.getText().trim();
        String password = txtUserPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            utilities.showAlert(Alert.AlertType.WARNING,
                    "Campos incompletos",
                    "Debe ingresar usuario y contraseña.");
            return;
        }

        User u = userService.authenticate(username, password);

        if (u != null) {
            UserService.setCurrentUser(u);
            try {
                FlowController flow = FlowController.getInstance();
                if (u.isAdmin()) {
                    flow.goView("AdminPrincipalWindow");
                } else {
                    flow.goView("UserViewWindow");
                    UserViewWindowController usrCtrl
                            = flow.getController("UserViewWindow");
                    usrCtrl.setUserName(u.getName());
                }
            } catch (IOException e) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo cargar la siguiente ventana.");
                e.printStackTrace();
            }
        } else {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error al iniciar sesión",
                    "Usuario o contraseña incorrectos.");
        }
    }

    @FXML
    private void clickConfiguracionUsuario(ActionEvent event) {
        try {
            FlowController.getInstance().goView("UserConfigWindow");
        } catch (IOException e) {
            new graphicUtilities().showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo abrir configuración de usuario."
            );
            e.printStackTrace();
        }
    }

    @FXML
    private void CreateAccount(ActionEvent event) {
        try {
            FlowController.getInstance().goView("SignUpWindow");
        } catch (IOException e) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo cargar la ventana de registro.");
        }
    }
}
