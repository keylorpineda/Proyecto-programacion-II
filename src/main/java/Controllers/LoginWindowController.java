package Controllers;

import Models.User;
import Services.UserManager;
import com.mycompany.proyectoprogramacionii.App;
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

    private graphicUtilities utilities;
    private UserManager userManager;

    @FXML
    private TextField txtUserNameLogin;
    @FXML
    private PasswordField txtUserPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnCreateAccount;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilities = new graphicUtilities();
        userManager = UserManager.getInstance();
    }

    @FXML
    private void LoginAccount(ActionEvent event) {
        String username = txtUserNameLogin.getText().trim();
        String password = txtUserPassword.getText().trim();

        if (userManager.authenticateUser(username, password)) {
            try {
                FlowController flow = FlowController.getInstance();
                User u = userManager.getCurrentUser();
                if (u.isAdmin()) {
                    flow.goView("AdminPrincipalWindow");

                } else {
                    flow.goView("UserViewWindow");
                    UserViewWindowController usrCtrl
                            = flow.getController("UserViewWindow");
                    usrCtrl.setUserName(u.getName());
                }
            } catch (IOException e) {
                utilities.showAlert(Alert.AlertType.ERROR,"Error", "No se pudo cargar la siguiente ventana.");
                e.printStackTrace();
            }
        } else {
            utilities.showAlert(Alert.AlertType.ERROR, "Error al iniciar sesión","Datos de ingreso incorrectos");
        }
    }

    @FXML
    private void CreateAccount(ActionEvent event) throws IOException {
        try {
            FlowController.getInstance().goView("SignUpWindow");
        } catch (IOException e) {
            utilities.showAlert(
                    Alert.AlertType.ERROR, "Error", "No se pudo cargar la ventana de registro.");
        }
    }
}
