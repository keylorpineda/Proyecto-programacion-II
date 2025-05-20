package Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Models.Administrator;
import Models.Report;
import Models.Reservation;
import Models.Room;
import Models.User;
import Services.ReportManager;
import Services.ReservationManager;
import Services.UserManager;
import Utilities.FlowController;
import Utilities.RoomManager;

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

public class AdminPrincipalWindowController implements Initializable {

    @FXML
    private Label lblUserName;
    @FXML
    private ToggleButton tgViewUsers, tgViewRooms,
            tgViewReservations, tgViewReports,
            tgCreateAdmin;
    @FXML
    private StackPane spGeneralContent;

    @FXML
    private TableView<User> tblUsers;
    @FXML
    private TableColumn<User, String> colId, colName, colLastName, colUsername;
    @FXML
    private TableColumn<User, Void> colEdit, colDelete;

    @FXML
    private TableView<Room> tblRooms;
    @FXML
    private TableColumn<Room, String> colRoomId, colRoomName, colRoomType;
    @FXML
    private TableColumn<Room, Integer> colCapacity;

    @FXML
    private TableView<Reservation> tblReservations;
    @FXML
    private TableColumn<Reservation, String> colResId,
            colResUser,
            colResRoom,
            colResDate,
            colResStart,
            colResEnd;

    @FXML
    private TableView<Report> tblReports;
    @FXML
    private TableColumn<Report, String> colReportId,
            colReportName,
            colReportDate;

    @FXML
    private GridPane formAdmin;
    @FXML
    private TextField txtAdminId, txtAdminName,
            txtAdminLastName, txtAdminUser;
    @FXML
    private PasswordField txtAdminPass;

    private final UserManager userManager = UserManager.getInstance();
    private final RoomManager roomManager = RoomManager.getInstance();
    private final ReservationManager resManager = ReservationManager.getInstance();
    private final ReportManager reportManager = ReportManager.getInstance();

    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private final ObservableList<Room> roomsList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> resList = FXCollections.observableArrayList();
    private final ObservableList<Report> reportsList = FXCollections.observableArrayList();

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
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("roomCapacity"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
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

    // Toggle handlers
    @FXML
    private void tgShowUsersTable(ActionEvent ev) {
        hideAll();
        usersList.setAll(userManager.getAllUsers());
        tblUsers.setVisible(true);
    }

    @FXML
    private void tgShowRoomTable(ActionEvent ev) {
        hideAll();
        roomsList.setAll(roomManager.getAllRooms());
        tblRooms.setVisible(true);
    }

    @FXML
    private void tgShowReservationsTable(ActionEvent ev) {
        hideAll();
        resList.setAll(resManager.getAllReservations());
        tblReservations.setVisible(true);
    }

    @FXML
    private void tgShowReport(ActionEvent ev) {
        hideAll();
        reportsList.setAll(reportManager.getAllReports());
        tblReports.setVisible(true);
    }

    @FXML
    private void tgCreateAdmin(ActionEvent ev) {
        hideAll();
        formAdmin.setVisible(true);
    }

    private void hideAll() {
        tblUsers.setVisible(false);
        tblRooms.setVisible(false);
        tblReservations.setVisible(false);
        tblReports.setVisible(false);
        formAdmin.setVisible(false);
    }

    private void setupUserEditDelete() {
        colEdit.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button btn = new Button("Editar");

                    {
                        btn.setOnAction(e -> {
                            User u = getTableView().getItems().get(getIndex());
                            try {
                                EditCustomerController ctrl = (EditCustomerController) FlowController.getInstance().openModal("EditCustomer");
                                ctrl.setUser(u);
                                usersList.setAll(userManager.getAllUsers());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });
        colDelete.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button btn = new Button("Eliminar");

                    {
                        btn.setOnAction(e -> {
                            User u = getTableView().getItems().get(getIndex());
                            userManager.deleteUser(u.getIdentification());
                            usersList.remove(u);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });
    }

    @FXML
    private void onCreateAdmin(ActionEvent ev) {
        String id = txtAdminId.getText().trim();
        String nm = txtAdminName.getText().trim();
        String ln = txtAdminLastName.getText().trim();
        String usr = txtAdminUser.getText().trim();
        String pwd = txtAdminPass.getText().trim();

        if (id.isEmpty() || nm.isEmpty() || ln.isEmpty() || usr.isEmpty() || pwd.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Complete todos los campos").showAndWait();
            return;
        }
        if (pwd.length() < 6) {
            new Alert(Alert.AlertType.WARNING, "La contraseña debe tener al menos 6 caracteres")
                    .showAndWait();
            return;
        }

        Administrator admin = new Administrator(nm, ln, id, usr, pwd);
        if (userManager.addUser(admin)) {
            new Alert(Alert.AlertType.INFORMATION, "Administrador creado exitosamente")
                    .showAndWait();
            txtAdminId.clear();
            txtAdminName.clear();
            txtAdminLastName.clear();
            txtAdminUser.clear();
            txtAdminPass.clear();
            tgShowUsersTable(null);
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Ya existe un usuario con esa cédula o nombre de usuario")
                    .showAndWait();
        }
    }
}
