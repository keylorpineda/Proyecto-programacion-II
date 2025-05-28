package Controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class AdminPrincipalWindowController implements Initializable {

    // ----- CABECERA -----
    @FXML
    private Label lblUserName;

    // ----- TOGGLE BUTTONS MENÚ LATERAL -----
    @FXML
    private ToggleButton tgViewUsers;
    @FXML
    private ToggleButton tgViewRooms;
    @FXML
    private ToggleButton tgViewReservations;
    @FXML
    private ToggleButton tgViewReports;
    @FXML
    private ToggleButton tgCreateAdmin;

    // ----- CONTENEDOR PRINCIPAL DINÁMICO -----
    @FXML
    private StackPane spGeneralContent;

    // ----- TABLAS -----
    @FXML
    private TableView<?> tblUsers;
    @FXML
    private TableView<?> tblRooms;
    @FXML
    private TableView<?> tblReservations;
    @FXML
    private TableView<?> tblReports;

    // ----- FORMULARIO CREAR ADMIN -----
    @FXML
    private GridPane formAdmin;
    @FXML
    private TextField txtAdminId;
    @FXML
    private TextField txtAdminName;
    @FXML
    private TextField txtAdminLastName;
    @FXML
    private TextField txtAdminUser;
    @FXML
    private PasswordField txtAdminPass;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Al cargar, mostrar la tabla de usuarios por defecto
        showUsers();
        // Ejemplo de inicializar el nombre de usuario:
        // lblUserName.setText(Session.getCurrentUser().getFullName());
    }

    // ----- MÉTODOS HANDLER PARA LOS TOGGLEBUTTONS -----
    @FXML
    private void tgShowUsersTable(ActionEvent event) {
        showUsers();
    }

    @FXML
    private void tgShowRoomTable(ActionEvent event) {
        showRooms();
    }

    @FXML
    private void tgShowReservationsTable(ActionEvent event) {
        showReservations();
    }

    @FXML
    private void tgShowReport(ActionEvent event) {
        showReports();
    }

    @FXML
    private void tgCreateAdmin(ActionEvent event) {
        showCreateAdminForm();
    }

    // ----- MÉTODO HANDLER PARA BOTÓN GUARDAR ADMIN -----
    @FXML
    private void onCreateAdmin(ActionEvent event) {
        // Recoger datos del formulario
        String id = txtAdminId.getText();
        String name = txtAdminName.getText();
        String lastName = txtAdminLastName.getText();
        String user = txtAdminUser.getText();
        String pass = txtAdminPass.getText();

        // TODO: Persistir nuevo administrador
        // p.ej. AdminService.save(new Administrator(id, name, lastName, user, pass));
        // Limpiar campos
        txtAdminId.clear();
        txtAdminName.clear();
        txtAdminLastName.clear();
        txtAdminUser.clear();
        txtAdminPass.clear();

        // Volver a la vista de usuarios
        showUsers();
    }

    // ----- MÉTODOS PRIVADOS DE AYUDA -----
    /**
     * Desmarca todos los toggles
     */
    private void resetToggles() {
        tgViewUsers.setSelected(false);
        tgViewRooms.setSelected(false);
        tgViewReservations.setSelected(false);
        tgViewReports.setSelected(false);
        tgCreateAdmin.setSelected(false);
    }

    private void showUsers() {
        resetToggles();
        tgViewUsers.setSelected(true);
        tblUsers.setVisible(true);
        tblRooms.setVisible(false);
        tblReservations.setVisible(false);
        tblReports.setVisible(false);
        formAdmin.setVisible(false);
    }

    private void showRooms() {
        resetToggles();
        tgViewRooms.setSelected(true);
        tblUsers.setVisible(false);
        tblRooms.setVisible(true);
        tblReservations.setVisible(false);
        tblReports.setVisible(false);
        formAdmin.setVisible(false);
    }

    private void showReservations() {
        resetToggles();
        tgViewReservations.setSelected(true);
        tblUsers.setVisible(false);
        tblRooms.setVisible(false);
        tblReservations.setVisible(true);
        tblReports.setVisible(false);
        formAdmin.setVisible(false);
    }

    private void showReports() {
        resetToggles();
        tgViewReports.setSelected(true);
        tblUsers.setVisible(false);
        tblRooms.setVisible(false);
        tblReservations.setVisible(false);
        tblReports.setVisible(true);
        formAdmin.setVisible(false);
    }

    private void showCreateAdminForm() {
        resetToggles();
        tgCreateAdmin.setSelected(true);
        tblUsers.setVisible(false);
        tblRooms.setVisible(false);
        tblReservations.setVisible(false);
        tblReports.setVisible(false);
        formAdmin.setVisible(true);
    }
}
