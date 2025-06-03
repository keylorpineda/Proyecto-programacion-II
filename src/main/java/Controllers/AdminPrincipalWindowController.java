package Controllers;

import Models.*;
import Services.*;
import Utilities.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminPrincipalWindowController implements Initializable {

    @FXML
    private Label lblUserName;
    @FXML
    private Button btnBack, btnAddSpace, btnFiltrarPlano;
    @FXML
    private VBox sidebar;
    @FXML
    private HBox breadcrumb;
    @FXML
    private ToggleButton tgViewUsers, tgViewRooms, tgViewReservations, tgViewReports, tgCreateAdmin;
    @FXML
    private StackPane spGeneralContent;
    @FXML
    private ScrollPane spUsersScroll, spRoomsScroll, spReservationsScroll, spReportsScroll;
    @FXML
    private TableView<User> tblUsers;
    @FXML
    private TableView<Reservation> tblReservations;
    @FXML
    private TableView<Report> tblReports;
    @FXML
    private TableColumn<User, Long> colId;
    @FXML
    private TableColumn<User, String> colName, colLastName, colUsername, colRole;
    @FXML
    private TableColumn<Reservation, Long> colResId;
    @FXML
    private TableColumn<Reservation, String> colResUser, colResRoom, colResDate, colResStart, colResEnd;
    @FXML
    private TableColumn<Report, Long> colReportId;
    @FXML
    private TableColumn<Report, String> colReportName, colReportDate;
    @FXML
    private GridPane formAdmin;
    @FXML
    private TextField txtAdminId, txtAdminName, txtAdminLastName, txtAdminUser;
    @FXML
    private PasswordField txtAdminPass;
    @FXML
    private GridPane gridPlano;
    @FXML
    private ComboBox<Room> comboBoxPiso;
    @FXML
    private DatePicker dpAdminDate;
    @FXML
    private ComboBox<String> cbAdminStartTime, cbAdminEndTime;

    // Servicios
    private Room currentRoom;
    private final RoomService roomService = new RoomService();
    private final SpaceService spaceService = new SpaceService();
    private final ReservationService reservationService = new ReservationService();
    private final ReportService reportService = new ReportService();
    private boolean[][] ocupado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DataInitializer.initializeIfNeeded();

        cbAdminStartTime.getItems().addAll(TimeSlots.getDefaultTimeSlots());
        cbAdminEndTime.getItems().addAll(TimeSlots.getDefaultTimeSlots());

        cargarComboBoxPisos();
        cargarTablaUsuarios();
        cargarTablaReservas();
        cargarTablaReportes();
        breadcrumb.setVisible(false);

        tgViewUsers.setOnAction(this::tgShowUsersTable);
        tgViewRooms.setOnAction(this::tgShowRoomTable);
        tgViewReservations.setOnAction(this::tgShowReservationsTable);
        tgViewReports.setOnAction(this::tgShowReport);
        tgCreateAdmin.setOnAction(this::tgCreateAdmin);
        btnAddSpace.setOnAction(this::onAddSpace);
        btnFiltrarPlano.setOnAction(this::onFiltrarPlano);

        if (tblUsers != null) {
            colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            colLastName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("lastName"));
            colUsername.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userName"));
            colRole.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userRole"));
        }
        cargarPlanoDeRoomActual();
    }

    private void cargarTablaUsuarios() {
        ObservableList<User> usuarios = FXCollections.observableArrayList(new UserService().findAll());
        tblUsers.setItems(usuarios);
    }

    private void cargarTablaReservas() {
        List<Reservation> reservas = reservationService.findAll();
        ObservableList<Reservation> reservasObs = FXCollections.observableArrayList(reservas);
        tblReservations.setItems(reservasObs);

        if (colResId != null) {
            colResId.setCellValueFactory(new PropertyValueFactory<>("id"));

        }
        if (colResUser != null) {
            colResUser.setCellValueFactory(r -> new SimpleStringProperty(
                    r.getValue().getUser() != null ? r.getValue().getUser().getName() : ""));
        }
        if (colResRoom != null) {
            colResRoom.setCellValueFactory(r -> new SimpleStringProperty(
                    r.getValue().getSpace() != null ? r.getValue().getSpace().getName() : ""));
        }
        if (colResDate != null) {
            colResDate.setCellValueFactory(r -> new SimpleStringProperty(
                    r.getValue().getDateCreated() != null ? r.getValue().getDateCreated().toString() : ""));
        }
        if (colResStart != null) {
            colResStart.setCellValueFactory(r -> new SimpleStringProperty(
                    r.getValue().getStartTime() != null ? r.getValue().getStartTime().toLocalTime().toString() : ""));
        }
        if (colResEnd != null) {
            colResEnd.setCellValueFactory(r -> new SimpleStringProperty(
                    r.getValue().getEndTime() != null ? r.getValue().getEndTime().toLocalTime().toString() : ""));
        }
    }

    private void cargarTablaReportes() {
        if (reportService == null || tblReports == null) {
            return;
        }
        List<Report> reportes = reportService.findAll();
        ObservableList<Report> obs = FXCollections.observableArrayList(reportes);
        tblReports.setItems(obs);

        if (colReportId != null) {
            colReportId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colReportName != null) {
            colReportName.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getDescription()));
        }
        if (colReportDate != null) {
            colReportDate.setCellValueFactory(r -> new SimpleStringProperty(
                    r.getValue().getDateCreated() != null ? r.getValue().getDateCreated().toString() : ""));
        }
    }

    private void cargarComboBoxPisos() {
        List<Room> rooms = roomService.findAll();
        comboBoxPiso.getItems().setAll(rooms);
        if (!rooms.isEmpty()) {
            comboBoxPiso.getSelectionModel().select(0);
            currentRoom = rooms.get(0);
            cargarPlanoDeRoomActual();
        }
        comboBoxPiso.setOnAction(event -> {
            currentRoom = comboBoxPiso.getValue();
            cargarPlanoDeRoomActual();
        });
    }

    private void cargarPlanoDeRoomActual() {
        if (currentRoom == null) {
            return;
        }
        if (currentRoom.getId() != null) {
            currentRoom = roomService.findByIdWithSpaces(currentRoom.getId());
        }

        gridPlano.getChildren().clear();
        gridPlano.getColumnConstraints().clear();
        gridPlano.getRowConstraints().clear();

        int filas = currentRoom.getRows();
        int columnas = currentRoom.getCols();

        for (int i = 0; i < columnas; i++) {
            gridPlano.getColumnConstraints().add(new ColumnConstraints(60));
        }
        for (int i = 0; i < filas; i++) {
            gridPlano.getRowConstraints().add(new RowConstraints(60));
        }

        ocupado = new boolean[filas][columnas];
        List<Space> espacios = currentRoom.getSpaces();

        LocalDate fecha = dpAdminDate.getValue();
        String horaInicio = cbAdminStartTime.getValue();
        String horaFin = cbAdminEndTime.getValue();

        Set<Long> ocupados = new HashSet<>();
        Set<Long> bloqueados = new HashSet<>();
        if (fecha != null && horaInicio != null && horaFin != null) {
            ocupados = reservationService.findByRoomAndDateAndTime(
                    currentRoom.getId(), fecha,
                    TimeSlots.parse(horaInicio),
                    TimeSlots.parse(horaFin)
            ).stream().map(r -> r.getSpace().getId()).collect(Collectors.toSet());

            bloqueados = spaceService.findBlockedSpaces(
                    currentRoom.getId(), fecha,
                    TimeSlots.parse(horaInicio),
                    TimeSlots.parse(horaFin)
            ).stream().map(Space::getId).collect(Collectors.toSet());
        }

        for (Space s : espacios) {
            Node visual = crearVisualEspacio(s, ocupados.contains(s.getId()), bloqueados.contains(s.getId()));
            gridPlano.add(visual, s.getStartCol(), s.getStartRow(), s.getWidth(), s.getHeight());

            for (int f = s.getStartRow(); f < s.getStartRow() + s.getHeight(); f++) {
                for (int c = s.getStartCol(); c < s.getStartCol() + s.getWidth(); c++) {
                    if (f >= 0 && f < filas && c >= 0 && c < columnas) {
                        ocupado[f][c] = true;
                    }
                }
            }
        }
    }

    private Node crearVisualEspacio(Space s, boolean ocupado, boolean bloqueado) {
        Button btn = new Button(s.getSpaceName());
        btn.setFont(Font.font("Segoe UI", 12));
        btn.setPrefSize(60 * s.getWidth(), 60 * s.getHeight());

        String color = "#bdbdbd";
        switch (s.getType()) {
            case ESCRITORIO:
                color = "#90caf9";
                break;
            case SALA_REUNIONES:
                color = "#ffb300";
                break;
            case AREA_COMUN:
                color = "#a5d6a7";
                break;
            case PASILLO:
                color = "#a1887f";
                break;
            default:
                break;
        }
        if (ocupado) {
            color = "#e57373"; 
            btn.setDisable(true);
        } else if (bloqueado) {
            color = "#616161"; 
            btn.setDisable(true);
        }
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #222; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Menú contextual
        btn.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) {
                ContextMenu menu = new ContextMenu();

                MenuItem eliminar = new MenuItem("Eliminar espacio");
                eliminar.setOnAction(e -> confirmarEliminarEspacio(currentRoom, s));

                MenuItem bloquear = new MenuItem("Bloquear espacio (horario)");
                bloquear.setOnAction(e -> bloquearEspacioDialog(s));

                menu.getItems().addAll(eliminar, bloquear);
                menu.show(btn, ev.getScreenX(), ev.getScreenY());
            }
        });
        return btn;
    }

    private void bloquearEspacioDialog(Space space) {
        LocalDate fecha = dpAdminDate.getValue();
        if (fecha == null) {
            new Alert(Alert.AlertType.WARNING, "Debe seleccionar una fecha para bloquear el espacio.").showAndWait();
            return;
        }

        List<String> slots = TimeSlots.getDefaultTimeSlots();
        ChoiceDialog<String> horaInicio = new ChoiceDialog<>(slots.get(0), slots);
        horaInicio.setTitle("Bloquear espacio");
        horaInicio.setHeaderText("Seleccione la hora de inicio");
        Optional<String> resStart = horaInicio.showAndWait();
        if (!resStart.isPresent()) {
            return;
        }

        ChoiceDialog<String> horaFin = new ChoiceDialog<>(slots.get(slots.size() - 1), slots);
        horaFin.setTitle("Bloquear espacio");
        horaFin.setHeaderText("Seleccione la hora de fin");
        Optional<String> resEnd = horaFin.showAndWait();
        if (!resEnd.isPresent()) {
            return;
        }

        LocalTime hStart = TimeSlots.parse(resStart.get());
        LocalTime hEnd = TimeSlots.parse(resEnd.get());
        if (hEnd.compareTo(hStart) <= 0) {
            new Alert(Alert.AlertType.ERROR, "La hora de fin debe ser mayor que la de inicio.").showAndWait();
            return;
        }
        spaceService.blockSpace(space, fecha, hStart, hEnd);
        cargarPlanoDeRoomActual();
        new Alert(Alert.AlertType.INFORMATION, "Espacio bloqueado correctamente.").showAndWait();
    }

    private void confirmarEliminarEspacio(Room room, Space space) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro que desea eliminar este espacio?");
        alert.setContentText("Nombre: " + space.getName());
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                eliminarEspacio(room, space);
            }
        });
    }

    private void eliminarEspacio(Room room, Space space) {
        spaceService.delete(space);
        cargarPlanoDeRoomActual();
    }

    @FXML
    private void onFiltrarPlano(ActionEvent event) {
        cargarPlanoDeRoomActual();
    }

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
        spUsersScroll.setVisible(true);
        spRoomsScroll.setVisible(false);
        spReservationsScroll.setVisible(false);
        spReportsScroll.setVisible(false);
        formAdmin.setVisible(false);
        breadcrumb.setVisible(true);
    }

    private void showRooms() {
        resetToggles();
        tgViewRooms.setSelected(true);
        spUsersScroll.setVisible(false);
        spRoomsScroll.setVisible(true);
        spReservationsScroll.setVisible(false);
        spReportsScroll.setVisible(false);
        formAdmin.setVisible(false);
        breadcrumb.setVisible(false);
        cargarPlanoDeRoomActual();
    }

    private void showReservations() {
        resetToggles();
        tgViewReservations.setSelected(true);
        spUsersScroll.setVisible(false);
        spRoomsScroll.setVisible(false);
        spReservationsScroll.setVisible(true);
        spReportsScroll.setVisible(false);
        formAdmin.setVisible(false);
        breadcrumb.setVisible(false);
    }

    private void showReports() {
        resetToggles();
        tgViewReports.setSelected(true);
        spUsersScroll.setVisible(false);
        spRoomsScroll.setVisible(false);
        spReservationsScroll.setVisible(false);
        spReportsScroll.setVisible(true);
        formAdmin.setVisible(false);
        breadcrumb.setVisible(false);
    }

    private void showCreateAdminForm() {
        resetToggles();
        tgCreateAdmin.setSelected(true);
        spUsersScroll.setVisible(false);
        spRoomsScroll.setVisible(false);
        spReservationsScroll.setVisible(false);
        spReportsScroll.setVisible(false);
        formAdmin.setVisible(true);
        breadcrumb.setVisible(false);
    }

    @FXML
    private void onBreadcrumbHome() {
        showUsers();
    }

    @FXML
    private void onBackAction(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("LoginWindow");
    }

    @FXML
    private void onAddSpace(ActionEvent event) {
        if (currentRoom == null) {
            return;
        }
        ChoiceDialog<SpaceType> tipoDialog = new ChoiceDialog<>(SpaceType.ESCRITORIO, SpaceType.values());
        tipoDialog.setTitle("Agregar Espacio");
        tipoDialog.setHeaderText("Selecciona el tipo de espacio a agregar");
        tipoDialog.setContentText("Tipo:");
        Optional<SpaceType> tipoResult = tipoDialog.showAndWait();
        if (!tipoResult.isPresent()) {
            return;
        }
        SpaceType tipo = tipoResult.get();
        int width = 1, height = 1, capacidad = 1;
        switch (tipo) {
            case ESCRITORIO:
                width = 1;
                height = 1;
                capacidad = 1;
                break;
            case AREA_COMUN:
                width = 2;
                height = 2;
                capacidad = 6;
                break;
            case SALA_REUNIONES:
                width = 3;
                height = 2;
                capacidad = 10;
                break;
            case PASILLO:
                width = 1;
                height = 1;
                capacidad = 1;
                break;
        }
        TextInputDialog nombreDialog = new TextInputDialog(tipo.toString());
        nombreDialog.setTitle("Nombre");
        nombreDialog.setHeaderText("Ponle un nombre al espacio:");
        nombreDialog.setContentText("Nombre:");
        Optional<String> nombreResult = nombreDialog.showAndWait();
        if (!nombreResult.isPresent() || nombreResult.get().trim().isEmpty()) {
            return;
        }
        String nombre = nombreResult.get().trim();
        int filas = currentRoom.getRows(), columnas = currentRoom.getCols();
        boolean[][] ocupadoTemp = new boolean[filas][columnas];
        for (Space s : currentRoom.getSpaces()) {
            for (int f = s.getStartRow(); f < s.getStartRow() + s.getHeight(); f++) {
                for (int c = s.getStartCol(); c < s.getStartCol() + s.getWidth(); c++) {
                    if (f >= 0 && f < filas && c >= 0 && c < columnas) {
                        ocupadoTemp[f][c] = true;
                    }
                }
            }
        }
        boolean added = false;
        for (int row = 0; row <= filas - height && !added; row++) {
            for (int col = 0; col <= columnas - width && !added; col++) {
                boolean espacioLibre = true;
                for (int dr = 0; dr < height; dr++) {
                    for (int dc = 0; dc < width; dc++) {
                        if (ocupadoTemp[row + dr][col + dc]) {
                            espacioLibre = false;
                        }
                    }
                }
                if (espacioLibre) {
                    Space nuevo = new Space(nombre, row, col, width, height, capacidad, tipo, currentRoom);
                    spaceService.update(nuevo);
                    added = true;
                }
            }
        }
        if (!added) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sin espacio");
            alert.setHeaderText("No hay suficiente espacio libre para ese tipo.");
            alert.showAndWait();
        }
        cargarPlanoDeRoomActual();
    }

    @FXML
    private void onToggleSidebar(ActionEvent event) {
        double currentWidth = sidebar.getPrefWidth();
        if (currentWidth > 80) {
            sidebar.setPrefWidth(80);
            tgViewUsers.setText("");
            tgViewRooms.setText("");
            tgViewReservations.setText("");
            tgViewReports.setText("");
            tgCreateAdmin.setText("");
        } else {
            sidebar.setPrefWidth(189);
            tgViewUsers.setText("Usuarios");
            tgViewRooms.setText("Plano");
            tgViewReservations.setText("Reservas");
            tgViewReports.setText("Reportes");
            tgCreateAdmin.setText("Crear Admin");
        }
    }
    @FXML
    private void onCreateAdmin(ActionEvent event) {
        txtAdminId.clear();
        txtAdminName.clear();
        txtAdminLastName.clear();
        txtAdminUser.clear();
        txtAdminPass.clear();
        showUsers();
    }
}
