package Controllers;

import Models.Administrator;
import Models.User;
import Models.Room;
import Models.Reservation;
import Models.Report;
import Services.UserService;
import Services.RoomService;
import Services.ReservationService;
import Services.ReportService;
import Utilities.FlowController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Alert.AlertType;

public class AdminPrincipalWindowController implements Initializable {

    // ——— FXML —————————————————————————————————————————————————
    @FXML private Label lblUserName;
    @FXML private ToggleButton tgViewUsers, tgViewRooms, tgViewReservations, tgViewReports, tgCreateAdmin;
    @FXML private StackPane spGeneralContent;

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, String> colId, colName, colLastName, colUsername;
    @FXML private TableColumn<User, Void> colEdit, colDelete;

    @FXML private TableView<Room> tblRooms;
    @FXML private TableColumn<Room, String> colRoomId, colRoomName, colRoomType;
    @FXML private TableColumn<Room, Integer> colCapacity;

    @FXML private TableView<Reservation> tblReservations;
    @FXML private TableColumn<Reservation, String> colResId, colResUser, colResRoom, colResDate, colResStart, colResEnd;

    @FXML private TableView<Report> tblReports;
    @FXML private TableColumn<Report, String> colReportId, colReportName, colReportDate;

    @FXML private GridPane formAdmin;
    @FXML private TextField txtAdminId, txtAdminName, txtAdminLastName, txtAdminUser;
    @FXML private PasswordField txtAdminPass;

   
    private final UserService userService             = new UserService();
    private final RoomService roomService             = new RoomService();
    private final ReservationService reservationService = new ReservationService();
    private final ReportService reportService         = new ReportService();

    private final ObservableList<User> usersList           = FXCollections.observableArrayList();
    private final ObservableList<Room> roomsList           = FXCollections.observableArrayList();
    private final ObservableList<Reservation> resList      = FXCollections.observableArrayList();
    private final ObservableList<Report> reportsList       = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
     
        colId.setCellValueFactory(new PropertyValueFactory<>("identification"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
        setupUserEditDelete();
        tblUsers.setItems(usersList);

       
        colRoomId.setCellValueFactory(new PropertyValueFactory<>("idRoom"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("roomCapacity"));
        tblRooms.setItems(roomsList);

       
        colResId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colResUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colResRoom.setCellValueFactory(new PropertyValueFactory<>("spaceName"));
        colResDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colResStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colResEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        tblReservations.setItems(resList);

        colReportId.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        colReportName.setCellValueFactory(new PropertyValueFactory<>("description"));
        colReportDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tblReports.setItems(reportsList);

        hideAll(); 
    }

    @FXML private void tgShowUsersTable(ActionEvent ev) {
        hideAll();
        usersList.setAll(userService.findAll());
        spGeneralContent.getChildren().setAll(tblUsers);
    }
    @FXML private void tgShowRoomTable(ActionEvent ev) {
        hideAll();
        roomsList.setAll(roomService.findAll());
        spGeneralContent.getChildren().setAll(tblRooms);
    }
    @FXML private void tgShowReservationsTable(ActionEvent ev) {
        hideAll();
        resList.setAll(reservationService.findAll());
        spGeneralContent.getChildren().setAll(tblReservations);
    }
    @FXML private void tgShowReport(ActionEvent ev) {
        hideAll();
        reportsList.setAll(reportService.findAll());
        spGeneralContent.getChildren().setAll(tblReports);
    }
    @FXML private void tgCreateAdmin(ActionEvent ev) {
        hideAll();
        spGeneralContent.getChildren().setAll(formAdmin);
    }

    private void hideAll() {
        spGeneralContent.getChildren().clear();
    }


    private void setupUserEditDelete() {
        colEdit.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Editar");
            { btn.setOnAction(e -> {
                User u = getTableView().getItems().get(getIndex());
                try {
                    EditCustomerController ctrl = (EditCustomerController)
                      FlowController.getInstance().openModal("EditCustomer");
                    ctrl.setUser(u);
                    usersList.setAll(userService.findAll());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        colDelete.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            { btn.setOnAction(e -> {
                User u = getTableView().getItems().get(getIndex());
                userService.delete(u.getId());
                usersList.remove(u);
            }); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    
@FXML
private void onCreateAdmin(ActionEvent ev) {
    // 1) Leer y recortar campos
    String id  = txtAdminId.getText().trim();
    String nm  = txtAdminName.getText().trim();
    String ln  = txtAdminLastName.getText().trim();
    String usr = txtAdminUser.getText().trim();
    String pwd = txtAdminPass.getText().trim();

    // 2) Validaciones de UI
    if (id.isEmpty() || nm.isEmpty() || ln.isEmpty() || usr.isEmpty() || pwd.isEmpty()) {
        new Alert(Alert.AlertType.ERROR, "Complete todos los campos").showAndWait();
        return;
    }
    if (pwd.length() < 6) {
        new Alert(Alert.AlertType.WARNING, "La contraseña debe tener al menos 6 caracteres")
            .showAndWait();
        return;
    }

    // 3) Instanciar tu entidad con el constructor requerido
    Administrator admin = new Administrator(Long.valueOf(id), nm, ln, usr, pwd);

    // 4) Intentar persistir con el servicio y manejar excepciones
    UserService userService = new UserService();
    try {
        userService.save(admin);
        new Alert(Alert.AlertType.INFORMATION, "Administrador creado exitosamente")
            .showAndWait();
        clearAdminForm();
        tgShowUsersTable(null);

    } catch (jakarta.persistence.EntityExistsException ex) {
        new Alert(Alert.AlertType.ERROR, "Ya existe un usuario con esa cédula").showAndWait();

    } catch (jakarta.persistence.PersistenceException ex) {
        new Alert(Alert.AlertType.ERROR, "El nombre de usuario ya está en uso").showAndWait();

    } catch (Exception ex) {
        ex.printStackTrace();
        new Alert(Alert.AlertType.ERROR,
                  "Error inesperado: " + ex.getMessage()).showAndWait();
    }
}

/**  
 * Limpia todos los campos del formulario de administrador.  
 */
private void clearAdminForm() {
    txtAdminId.clear();
    txtAdminName.clear();
    txtAdminLastName.clear();
    txtAdminUser.clear();
    txtAdminPass.clear();
}

}

