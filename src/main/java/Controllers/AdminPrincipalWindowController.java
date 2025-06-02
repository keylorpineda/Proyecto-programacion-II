package Controllers;

import java.net.URL;
import java.util.ResourceBundle;

// Asegúrate de que la ruta sea la correcta a tu FlowController:
import Utilities.FlowController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AdminPrincipalWindowController implements Initializable {

    // ===== 1) CABECERA =====

    /** Etiqueta donde se muestra el nombre de usuario */
    @FXML private Label lblUserName;

    /** Botón “Back” (flecha) que vuelve a LoginWindow o retrocede en historial */
    @FXML private Button btnBack;

    // ===== 2) SIDEBAR =====

    /** Botón para colapsar/expandir el sidebar */
    @FXML private Button btnToggleSidebar;

    /** Contenedor del sidebar (VBox) */
    @FXML private VBox sidebar;

    // ===== 3) BREADCRUMB =====

    /** HBox que contiene “Inicio > Usuarios” */
    @FXML private HBox breadcrumb;

    // ===== 4) TOGGLE BUTTONS del menú lateral =====

    @FXML private ToggleButton tgViewUsers;
    @FXML private ToggleButton tgViewRooms;
    @FXML private ToggleButton tgViewReservations;
    @FXML private ToggleButton tgViewReports;
    @FXML private ToggleButton tgCreateAdmin;

    // ===== 5) ÁREA DINÁMICA =====

    @FXML private StackPane spGeneralContent;

    // ===== 6) TABLAS =====

    @FXML private TableView<?> tblUsers;
    @FXML private TableView<?> tblRooms;
    @FXML private TableView<?> tblReservations;
    @FXML private TableView<?> tblReports;

    // ===== 7) FORMULARIO “Crear Administrador” =====

    @FXML private GridPane formAdmin;
    @FXML private TextField txtAdminId;
    @FXML private TextField txtAdminName;
    @FXML private TextField txtAdminLastName;
    @FXML private TextField txtAdminUser;
    @FXML private PasswordField txtAdminPass;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Al cargar la ventana, mostramos por defecto la tabla de usuarios:
        showUsers();

        // Ocultamos el breadcrumb (“Inicio > Usuarios”) al iniciar:
        breadcrumb.setVisible(false);

        // (Opcional) Aquí podrías inyectar datos de prueba en tblUsers para comprobar
        // visualmente que se ve correctamente. Ejemplo:
        //
        // colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        // colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        // colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        // colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
        // tblUsers.getItems().addAll(
        //     new Usuario("123", "Juan", "Pérez", "juanp"),
        //     new Usuario("456", "María", "Gómez", "mariag")
        // );
    }

    // ===============================================
    // 1) HANDLER DEL BOTÓN “Back” (flecha en la cabecera)
    // ===============================================
    @FXML
    private void onBackAction(ActionEvent event) {
        try {
            // Si quieres ir siempre a LoginWindow:
            FlowController.getInstance().goView("LoginWindow");

            // Si en vez de forzar a LoginWindow prefieres usar el historial:
            // FlowController.getInstance().goBack();
        } catch (Exception ex) {
            ex.printStackTrace();
            // Si quieres, aquí puedes mostrar un Alert en pantalla si falla.
        }
    }

    // ===============================================
    // 2) HANDLER PARA COLAPSAR / EXPANDIR EL SIDEBAR
    // ===============================================
    @FXML
    private void onToggleSidebar(ActionEvent event) {
        double currentWidth = sidebar.getPrefWidth();

        if (currentWidth > 80) {
            // --------------- Colapsar --------------- 
            // Reducimos a 80px de ancho, y ocultamos el texto de los toggles
            sidebar.setPrefWidth(80);

            tgViewUsers.setText("");
            tgViewRooms.setText("");
            tgViewReservations.setText("");
            tgViewReports.setText("");
            tgCreateAdmin.setText("");
        } else {
            // -------------- Expandir --------------- 
            // Restauramos 189px de ancho y ponemos de nuevo el texto
            sidebar.setPrefWidth(189);

            tgViewUsers.setText("Usuarios");
            tgViewRooms.setText("Plano");
            tgViewReservations.setText("Reservas");
            tgViewReports.setText("Reportes");
            tgCreateAdmin.setText("Crear Admin");
        }
    }

    // =================================================
    // 3) HANDLERS PARA LOS TOGGLEBUTTONS DEL MENÚ LATERAL
    // =================================================
    @FXML private void tgShowUsersTable(ActionEvent event)         { showUsers(); }
    @FXML private void tgShowRoomTable(ActionEvent event)          { showRooms(); }
    @FXML private void tgShowReservationsTable(ActionEvent event)  { showReservations(); }
    @FXML private void tgShowReport(ActionEvent event)             { showReports(); }
    @FXML private void tgCreateAdmin(ActionEvent event)            { showCreateAdminForm(); }

    // ===================================================
    // 4) HANDLER DEL “Breadcrumb”: volver a la tabla “Usuarios”
    // ===================================================
    @FXML
    private void onBreadcrumbHome() {
        showUsers();
    }

    // ===================================================
    // 5) HANDLER PARA EL BOTÓN “Guardar” DEL FORMULARIO
    //      Crear Administrador
    // ===================================================
    @FXML
    private void onCreateAdmin(ActionEvent event) {
        // Recogemos los datos del formulario:
        String id       = txtAdminId.getText();
        String name     = txtAdminName.getText();
        String lastName = txtAdminLastName.getText();
        String user     = txtAdminUser.getText();
        String pass     = txtAdminPass.getText();

        // TODO: aquí persiste tu nuevo administrador. Por ejemplo:
        //    AdminService.save(new Administrator(id, name, lastName, user, pass));

        // Limpiamos los campos del formulario:
        txtAdminId.clear();
        txtAdminName.clear();
        txtAdminLastName.clear();
        txtAdminUser.clear();
        txtAdminPass.clear();

        // Volvemos a la tabla de usuarios:
        showUsers();
    }

    // ================================
    // 6) MÉTODOS PRIVADOS DE AYUDA (MOSTRAR/OCULTAR VISTAS)
    // ================================
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

        // Mostramos el breadcrumb “Inicio > Usuarios”
        breadcrumb.setVisible(true);
    }

    private void showRooms() {
        resetToggles();
        tgViewRooms.setSelected(true);

        tblUsers.setVisible(false);
        tblRooms.setVisible(true);
        tblReservations.setVisible(false);
        tblReports.setVisible(false);
        formAdmin.setVisible(false);

        breadcrumb.setVisible(false);
    }

    private void showReservations() {
        resetToggles();
        tgViewReservations.setSelected(true);

        tblUsers.setVisible(false);
        tblRooms.setVisible(false);
        tblReservations.setVisible(true);
        tblReports.setVisible(false);
        formAdmin.setVisible(false);

        breadcrumb.setVisible(false);
    }

    private void showReports() {
        resetToggles();
        tgViewReports.setSelected(true);

        tblUsers.setVisible(false);
        tblRooms.setVisible(false);
        tblReservations.setVisible(false);
        tblReports.setVisible(true);
        formAdmin.setVisible(false);

        breadcrumb.setVisible(false);
    }

    private void showCreateAdminForm() {
        resetToggles();
        tgCreateAdmin.setSelected(true);

        tblUsers.setVisible(false);
        tblRooms.setVisible(false);
        tblReservations.setVisible(false);
        tblReports.setVisible(false);
        formAdmin.setVisible(true);

        breadcrumb.setVisible(false);
    }
}
