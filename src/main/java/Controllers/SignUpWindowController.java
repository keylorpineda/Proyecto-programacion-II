
package Controllers;

import Models.User;
import Services.UserManager;
import com.mycompany.proyectoprogramacionii.App;
import com.mycompany.proyectoprogramacionii.Utilities;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

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

    private Utilities utilities;

    private UserManager userManager;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        utilities = new Utilities();
        userManager=UserManager.getInstance();
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
            utilities.showAlert(AlertType.ERROR, "Contraseña debil", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        User newUser = new User(name, lastName, id, userName, password); 
        userManager.addUser(newUser);
        utilities.showAlert(AlertType.INFORMATION, "Usuario creado", "Usuario creado exitosamente.");
        App.setRoot("LoginWindow");
    }

}
