package Controllers;

import Services.UserManager;
import com.mycompany.proyectoprogramacionii.App;
import com.mycompany.proyectoprogramacionii.Utilities;
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

    private Utilities utilities;
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
        utilities = new Utilities();
        userManager = UserManager.getInstance();
    }

    @FXML
    private void LoginAccount(ActionEvent event) {
        String username = txtUserNameLogin.getText().trim();
        String password = txtUserPassword.getText().trim();

        if (userManager.authenticateUser(username, password)) {
            try {
                App.setRoot("UserViewWindow");
            } catch (IOException e) {
                e.printStackTrace();
                utilities.showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista de usuario.");
            }
        } else {
            utilities.showAlert(Alert.AlertType.ERROR, "Error al iniciar sesión", "Datos de ingreso incorrectos");
        }
    }

    @FXML
    private void CreateAccount(ActionEvent event) throws IOException {
        App.setRoot("SignUpWindow");
    }
}
