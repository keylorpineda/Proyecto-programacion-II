package Controllers;

import Models.Room;
import Models.Space;
import Models.User;
import Models.Reservation;
import Models.Report;
import Models.SpaceType;
import Services.RoomService;
import Services.SpaceService;
import Utilities.DataInitializer;
import Utilities.FlowController;
import Utilities.SpaceVisualFactory;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminPrincipalWindowController implements Initializable {

    @FXML
    private Label lblUserName;
    @FXML
    private Button btnBack;
    @FXML
    private VBox sidebar;
    @FXML
    private HBox breadcrumb;
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
    @FXML
    private StackPane spGeneralContent;

    @FXML
    private ScrollPane spUsersScroll;
    @FXML
    private ScrollPane spRoomsScroll;
    @FXML
    private ScrollPane spReservationsScroll;
    @FXML
    private ScrollPane spReportsScroll;

    @FXML
    private TableView<User> tblUsers;
    @FXML
    private TableView<Room> tblRooms;
    @FXML
    private TableView<Reservation> tblReservations;
    @FXML
    private TableView<Report> tblReports;

    @FXML
    private TableColumn<User, Long> colId;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colLastName;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colRole;

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

    @FXML
    private GridPane gridPlano;
    @FXML
    private Button btnAddSpace;
    @FXML
    private ComboBox<Room> comboBoxPiso;

    private Room currentRoom;
    private RoomService roomService = new RoomService();
    private SpaceService spaceService = new SpaceService();
    private boolean[][] ocupado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DataInitializer.initializeIfNeeded();
        cargarComboBoxPisos();
        showUsers();
        breadcrumb.setVisible(false);

        tgViewUsers.setOnAction(this::tgShowUsersTable);
        tgViewRooms.setOnAction(this::tgShowRoomTable);
        tgViewReservations.setOnAction(this::tgShowReservationsTable);
        tgViewReports.setOnAction(this::tgShowReport);
        tgCreateAdmin.setOnAction(this::tgCreateAdmin);
        btnAddSpace.setOnAction(this::onAddSpace);

        if (tblUsers != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
            colRole.setCellValueFactory(new PropertyValueFactory<>("userRole"));
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

    @FXML
    private void onBackAction(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("LoginWindow");
    }

    @FXML
    private void onAddSpace(ActionEvent event) {
        if (currentRoom == null) {
            return;
        }
        // Selección de tipo de espacio
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
        // Selección de nombre
        TextInputDialog nombreDialog = new TextInputDialog(tipo.toString());
        nombreDialog.setTitle("Nombre");
        nombreDialog.setHeaderText("Ponle un nombre al espacio:");
        nombreDialog.setContentText("Nombre:");
        Optional<String> nombreResult = nombreDialog.showAndWait();
        if (!nombreResult.isPresent() || nombreResult.get().trim().isEmpty()) {
            return;
        }
        String nombre = nombreResult.get().trim();

        int filas = currentRoom.getRows();
        int columnas = currentRoom.getCols();
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
        // Buscar la primera posición válida para el nuevo espacio
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
                    // Si hay espacio, crea el espacio con esa posición
                    Space nuevo = new Space(nombre, row, col, width, height, capacidad, tipo, currentRoom);
                    spaceService.update(nuevo); // Aquí los valores nunca serán null
                    added = true;
                }
            }
        }
        if (!added) {
            // Si no se pudo añadir porque no hay espacio
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sin espacio");
            alert.setHeaderText("No hay suficiente espacio libre para ese tipo.");
            alert.showAndWait();
        }
        cargarPlanoDeRoomActual();
    }

    private void cargarPlanoDeRoomActual() {
        if (currentRoom == null) {
            return;
        }
        // Trae el Room con todos sus spaces actualizados
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
        for (Space s : espacios) {
            Node visual = SpaceVisualFactory.createVisual(s, true, () -> confirmarEliminarEspacio(currentRoom, s));
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

    private void agregarNuevoEspacio(Space nuevoEspacio) {
        spaceService.update(nuevoEspacio);
        cargarPlanoDeRoomActual();
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
    private void onBreadcrumbHome() {
        showUsers();
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
}
