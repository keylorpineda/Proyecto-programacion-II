package Controllers;

import Models.*;
import Services.*;
import Utilities.*;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
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
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    private TableView<Report> tblReports;
    @FXML
    private TableColumn<User, Long> colId;
    @FXML
    private TableColumn<User, String> colName, colLastName, colUsername, colRole;
    @FXML
    private TableColumn<Reservation, Long> colResId;
    @FXML
    private TableColumn<Reservation, String> colResUser, colResRoom, colResDate, colResStart, colResEnd;
    private TableColumn<Report, Long> colReportId;
    private TableColumn<Report, String> colReportName;
    private TableColumn<Report, String> colReportDate;
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
    @FXML
    private TableView<Map.Entry<SpaceType, Long>> tblEspaciosReservados;
    @FXML
    private TableColumn<Map.Entry<SpaceType, Long>, String> colTipoEspacio;
    @FXML
    private TableColumn<Map.Entry<SpaceType, Long>, Long> colCantidadEspacio;
    @FXML
    private TableView<Map.Entry<String, Long>> tblUsuariosReservas;
    @FXML
    private TableColumn<Map.Entry<String, Long>, String> colUsuarioNombre;
    @FXML
    private TableColumn<Map.Entry<String, Long>, Long> colCantidadUsuario;
    @FXML
    private TableView<Map.Entry<String, Long>> tblHorariosReservas;
    @FXML
    private TableColumn<Map.Entry<String, Long>, String> colHoraInicio;
    @FXML
    private TableColumn<Map.Entry<String, Long>, Long> colCantidadHora;
    @FXML
    private RadioButton radioCliente;
    @FXML
    private RadioButton radioAdmin;
    private ToggleGroup grupoRol = new ToggleGroup();

    graphicUtilities utilities = new graphicUtilities();

    private UserService userService = new UserService();

    @FXML
    private PieChart pieTipoEspacio;

    @FXML
    private BarChart<String, Number> barUsuarios;

    @FXML
    private BarChart<String, Number> barHoras;

    private Room currentRoom;
    private final RoomService roomService = new RoomService();
    private final SpaceService spaceService = new SpaceService();
    private final ReservationService reservationService = new ReservationService();
    private final ReportService reportService = new ReportService();

    private Space draggedSpace;
    private boolean isDragging = false;
    private double cellWidth = 60.0;
    private double cellHeight = 60.0;
    private List<Region> highlightCells = new ArrayList<>();
    @FXML
    private VBox root;
    @FXML
    private HBox header;
    @FXML
    private Button btnToggleSidebar;
    @FXML
    private Button btnGuardarAdmin;
    @FXML
    private Button btnEditarUsuario;
    @FXML
    private Button btnEliminarUsuario;

    private User userSeleccionado;

    private boolean esPosicionValida(Space espacioAMover, int nuevaFila, int nuevaColumna) {
        if (espacioAMover == null || currentRoom == null) {
            return false;
        }
        if (nuevaFila < 0 || nuevaColumna < 0) {
            return false;
        }

        if (nuevaFila + espacioAMover.getHeight() > currentRoom.getRows()
                || nuevaColumna + espacioAMover.getWidth() > currentRoom.getCols()) {
            return false;
        }

        for (Space otroEspacio : currentRoom.getSpaces()) {
            if (otroEspacio.getId().equals(espacioAMover.getId())) {
                continue;
            }

            int espacioRight = nuevaColumna + espacioAMover.getWidth();
            int espacioBottom = nuevaFila + espacioAMover.getHeight();
            int otroRight = otroEspacio.getStartCol() + otroEspacio.getWidth();
            int otroBottom = otroEspacio.getStartRow() + otroEspacio.getHeight();

            boolean superponeHorizontal = !(espacioRight <= otroEspacio.getStartCol() || nuevaColumna >= otroRight);
            boolean superponeVertical = !(espacioBottom <= otroEspacio.getStartRow() || nuevaFila >= otroBottom);

            if (superponeHorizontal && superponeVertical) {
                return false;
            }
        }

        return true;
    }

    private Point2D calcularPosicionCelda(double sceneX, double sceneY, Space espacio) {
        Point2D localPoint = gridPlano.sceneToLocal(sceneX, sceneY);

        actualizarDimensionesCelda();

        int targetCol = (int) Math.floor(localPoint.getX() / cellWidth);
        int targetRow = (int) Math.floor(localPoint.getY() / cellHeight);

        targetCol = Math.max(0, Math.min(targetCol, currentRoom.getCols() - espacio.getWidth()));
        targetRow = Math.max(0, Math.min(targetRow, currentRoom.getRows() - espacio.getHeight()));

        return new Point2D(targetCol, targetRow);
    }

    private Region previewRegion = null;

    private void actualizarDimensionesCelda() {
        if (currentRoom != null && gridPlano.getWidth() > 0 && gridPlano.getHeight() > 0) {
            cellWidth = gridPlano.getWidth() / currentRoom.getCols();
            cellHeight = gridPlano.getHeight() / currentRoom.getRows();
        } else {
            cellWidth = 60.0;
            cellHeight = 60.0;
        }
    }

    private void limpiarHighlights() {
        for (Region highlight : highlightCells) {
            gridPlano.getChildren().remove(highlight);
        }
        highlightCells.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DataInitializer.initializeIfNeeded();
        cbAdminStartTime.getItems().setAll(TimeSlots.getDefaultTimeSlots());
        cbAdminEndTime.getItems().setAll(TimeSlots.getDefaultTimeSlots());
        dpAdminDate.valueProperty().addListener((obs, ov, nv) -> cargarPlanoDeRoomActual());
        cbAdminStartTime.valueProperty().addListener((obs, ov, nv) -> cargarPlanoDeRoomActual());
        cbAdminEndTime.valueProperty().addListener((obs, ov, nv) -> cargarPlanoDeRoomActual());
        cargarComboBoxPisos();
        cargarTablaUsuarios();
        cargarTablaReservas();
        cargarTablaReportes();
        breadcrumb.setVisible(false);
        tgViewUsers.setOnAction(e -> showUsers());
        tgViewRooms.setOnAction(e -> showRooms());
        tgViewReservations.setOnAction(e -> showReservations());
        tgViewReports.setOnAction(e -> {
            showReports();
            cargarGraficoEspacios();
            cargarGraficoUsuariosTop();
            cargarGraficoHorarios();
        });
        tgCreateAdmin.setOnAction(e -> showCreateAdminForm());
        btnAddSpace.setOnAction(this::onAddSpace);
        btnFiltrarPlano.setOnAction(e -> cargarPlanoDeRoomActual());
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("userRole"));
        cargarPlanoDeRoomActual();

        configurarDragAndDrop();
        radioCliente.setToggleGroup(grupoRol);
        radioAdmin.setToggleGroup(grupoRol);
        radioCliente.setSelected(true);
    }

    private void mostrarPreviewPosicion(int col, int row, Space espacio, boolean esValida) {
        limpiarPreview();

        if (espacio == null || currentRoom == null) {
            return;
        }

        previewRegion = new Region();
        previewRegion.setMouseTransparent(true);

        if (esValida) {
            previewRegion.setStyle(
                    "-fx-background-color: rgba(255, 193, 7, 0.4); "
                    + // Amarillo transparente
                    "-fx-border-color: #FFC107; "
                    + "-fx-border-width: 3px; "
                    + "-fx-border-style: solid; "
                    + "-fx-background-radius: 8px; "
                    + "-fx-border-radius: 8px;"
            );
        } else {
            previewRegion.setStyle(
                    "-fx-background-color: rgba(244, 67, 54, 0.4); "
                    + // Rojo transparente
                    "-fx-border-color: #F44336; "
                    + "-fx-border-width: 3px; "
                    + "-fx-border-style: dashed; "
                    + "-fx-background-radius: 8px; "
                    + "-fx-border-radius: 8px;"
            );
        }

        gridPlano.add(previewRegion, col, row, espacio.getWidth(), espacio.getHeight());
    }

    private void limpiarPreview() {
        if (previewRegion != null) {
            gridPlano.getChildren().remove(previewRegion);
            previewRegion = null;
        }
    }

    private void configurarDragAndDrop() {

        gridPlano.setOnDragOver(event -> {
            if (event.getGestureSource() != gridPlano && event.getDragboard().hasString() && draggedSpace != null) {
                event.acceptTransferModes(TransferMode.MOVE);

                Point2D posicion = calcularPosicionCelda(event.getSceneX(), event.getSceneY(), draggedSpace);
                int targetCol = (int) posicion.getX();
                int targetRow = (int) posicion.getY();

                boolean esValida = esPosicionValida(draggedSpace, targetRow, targetCol);

                mostrarPreviewPosicion(targetCol, targetRow, draggedSpace, esValida);

                if (esValida) {
                    gridPlano.setStyle("-fx-cursor: move;");
                } else {
                    gridPlano.setStyle("-fx-cursor: not-allowed;");
                }
            }
            event.consume();
        });

        gridPlano.setOnDragExited(event -> {
            gridPlano.setStyle("");
            limpiarPreview();
            event.consume();
        });

        gridPlano.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && draggedSpace != null) {
                try {
                    Point2D posicion = calcularPosicionCelda(event.getSceneX(), event.getSceneY(), draggedSpace);
                    int targetCol = (int) posicion.getX();
                    int targetRow = (int) posicion.getY();

                    if (esPosicionValida(draggedSpace, targetRow, targetCol)) {

                        draggedSpace.setStartCol(targetCol);
                        draggedSpace.setStartRow(targetRow);
                        spaceService.update(draggedSpace);
                        success = true;

                        mostrarNotificacionExito(
                                "Espacio '" + draggedSpace.getSpaceName()
                                + "' movido a posición (" + targetCol + "," + targetRow + ")"
                        );
                    } else {
                        new Alert(Alert.AlertType.WARNING,
                                "No se puede mover el espacio a esa posición.\n"
                                + "Verifica que no haya colisiones con otros espacios."
                        ).showAndWait();
                    }
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR,
                            "Error al mover el espacio: " + e.getMessage()
                    ).showAndWait();
                }
            }

            draggedSpace = null;
            isDragging = false;
            gridPlano.setStyle("");
            limpiarHighlights();
            limpiarPreview();

            if (success) {
                cargarPlanoDeRoomActual();
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void cargarTablaUsuarios() {
        ObservableList<User> data = FXCollections.observableArrayList(new UserService().findAll());
        tblUsers.setItems(data);
        tblUsers.refresh();
    }

    private void cargarTablaReservas() {
        ObservableList<Reservation> data = FXCollections.observableArrayList(reservationService.findAll());
        tblReservations.setItems(data);
        colResId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colResUser.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().getUser() != null ? r.getValue().getUser().getName() : ""));
        colResRoom.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().getSpace() != null ? r.getValue().getSpace().getName() : ""));
        colResDate.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().getDateCreated() != null ? r.getValue().getDateCreated().toString() : ""));
        colResStart.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().getStartTime() != null ? r.getValue().getStartTime().toLocalTime().toString() : ""));
        colResEnd.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().getEndTime() != null ? r.getValue().getEndTime().toLocalTime().toString() : ""));
    }

    private void cargarTablaReportes() {
        if (tblReports == null) {
            return;
        }

        List<Report> reportes = reportService.findAll();
        ObservableList<Report> obs = FXCollections.observableArrayList(reportes);
        tblReports.setItems(obs);

        if (colReportId != null) {
            colReportId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colReportName != null) {
            colReportName.setCellValueFactory(r
                    -> new SimpleStringProperty(r.getValue().getDescription()));
        }
        if (colReportDate != null) {
            colReportDate.setCellValueFactory(r
                    -> new SimpleStringProperty(
                            r.getValue().getDateCreated() != null
                            ? r.getValue().getDateCreated().toString()
                            : ""));
        }
    }

    private void cargarComboBoxPisos() {
        List<Room> rooms = roomService.findAll();
        comboBoxPiso.getItems().setAll(rooms);
        if (!rooms.isEmpty()) {
            comboBoxPiso.getSelectionModel().selectFirst();
            currentRoom = rooms.get(0);
        }
        comboBoxPiso.setOnAction(e -> {
            currentRoom = comboBoxPiso.getValue();
            cargarPlanoDeRoomActual();
        });
    }

    private void cargarPlanoDeRoomActual() {
        if (currentRoom == null) {
            return;
        }
        limpiarHighlights();
        currentRoom = roomService.findByIdWithSpaces(currentRoom.getId());
        gridPlano.getChildren().clear();
        gridPlano.getColumnConstraints().clear();
        gridPlano.getRowConstraints().clear();

        int filas = currentRoom.getRows();
        int columnas = currentRoom.getCols();

        for (int c = 0; c < columnas; c++) {
            ColumnConstraints colConstraint = new ColumnConstraints(60);
            colConstraint.setHgrow(Priority.NEVER);
            gridPlano.getColumnConstraints().add(colConstraint);
        }
        for (int r = 0; r < filas; r++) {
            RowConstraints rowConstraint = new RowConstraints(60);
            rowConstraint.setVgrow(Priority.NEVER);
            gridPlano.getRowConstraints().add(rowConstraint);
        }

        LocalDate fecha = dpAdminDate.getValue();
        String horaInicioStr = cbAdminStartTime.getValue();
        String horaFinStr = cbAdminEndTime.getValue();

        Map<Long, Integer> espaciosOcupados = new HashMap<>();
        Set<Long> espaciosBloqueados = new HashSet<>();

        if (fecha != null && horaInicioStr != null && horaFinStr != null) {
            try {
                LocalTime horaInicio = TimeSlots.parse(horaInicioStr);
                LocalTime horaFin = TimeSlots.parse(horaFinStr);

                List<Reservation> reservasActivas = reservationService.findByRoomAndDateAndTime(
                        currentRoom.getId(), fecha, horaInicio, horaFin
                );

                for (Reservation reserva : reservasActivas) {
                    Long spaceId = reserva.getSpace().getId();
                    espaciosOcupados.put(spaceId,
                            espaciosOcupados.getOrDefault(spaceId, 0) + reserva.getSeatCount());
                }

                espaciosBloqueados = spaceService.findBlockedSpaces(
                        currentRoom.getId(), fecha, horaInicio, horaFin
                ).stream().map(Space::getId).collect(Collectors.toSet());

            } catch (Exception e) {
                System.err.println("Error al cargar estados de espacios: " + e.getMessage());
            }
        }

        for (Space s : currentRoom.getSpaces()) {
            try {
                int ocupados = 0;
                boolean estaBloqueado = false;
                boolean estaCompleto = false;

                if (s.getType() != SpaceType.PASILLO) {
                    ocupados = espaciosOcupados.getOrDefault(s.getId(), 0);
                    estaBloqueado = espaciosBloqueados.contains(s.getId());
                    estaCompleto = ocupados >= s.getCapacity();
                }

                Node nodo = crearVisualEspacio(s, ocupados, estaBloqueado, estaCompleto);
                gridPlano.add(nodo, s.getStartCol(), s.getStartRow(), s.getWidth(), s.getHeight());

                nodo.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), nodo);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            } catch (Exception e) {
                System.err.println("Error al crear espacio visual: " + e.getMessage());
            }
        }
        actualizarDimensionesCelda();
        draggedSpace = null;
        isDragging = false;
    }

    private Node crearVisualEspacio(Space s, int ocupados, boolean isBloqueado, boolean estaCompleto) {
        Button btn = new Button(s.getSpaceName());
        btn.setFont(Font.font("Segoe UI", 12));
        btn.setPrefSize(60 * s.getWidth(), 60 * s.getHeight());

        String color;
        String precio;
        String estadoTexto = "";

        switch (s.getType()) {
            case ESCRITORIO:
                color = "#90caf9";
                precio = "$15/hora";
                break;
            case SALA_REUNIONES:
                color = "#ffb300";
                precio = "$50/hora";
                break;
            case AREA_COMUN:
                color = "#a5d6a7";
                precio = "$25/hora";
                break;
            case PASILLO:
                color = "#a1887f";
                precio = "";
                break;
            default:
                color = "#bdbdbd";
                precio = "$10/hora";
                break;
        }

        if (isBloqueado) {
            color = "#616161";
            estadoTexto = "🚫 BLOQUEADO";
        } else if (estaCompleto) {
            color = "#e57373";
            estadoTexto = "⚠️ COMPLETO";
        } else if (ocupados > 0) {
            color = "#ffcc80";
            estadoTexto = "⚡ PARCIAL";
        } else {
            estadoTexto = "✅ DISPONIBLE";
        }

        final String estadoFinal = estadoTexto;

        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: #222; -fx-border-radius: 8px; "
                + "-fx-background-radius: 8px; -fx-border-color: rgba(0,0,0,0.1); -fx-border-width: 1px; "
                + "-fx-font-weight: bold; -fx-cursor: hand;",
                color
        ));

        int disponibles = Math.max(0, s.getCapacity() - ocupados);

        Tooltip tooltip = new Tooltip(String.format(
                "Tipo: %s\n"
                + "Capacidad total: %d\n"
                + "Ocupados: %d\n"
                + "Disponibles: %d\n"
                + "Precio: %s\n"
                + "Tamaño: %dx%d\n"
                + "Posición: (%d,%d)\n"
                + "Estado: %s",
                s.getType().toString().replace("_", " "),
                s.getCapacity(),
                ocupados,
                disponibles,
                precio,
                s.getWidth(), s.getHeight(),
                s.getStartCol(), s.getStartRow(),
                estadoFinal
        ));

        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #333; -fx-text-fill: white;");
        tooltip.setShowDelay(Duration.millis(300));
        Tooltip.install(btn, tooltip);

        if (!isBloqueado && !estaCompleto) {
            btn.setOnMouseEntered(e -> {
                if (!isDragging) {
                    btn.setStyle(btn.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
                    DropShadow shadow = new DropShadow();
                    shadow.setColor(Color.rgb(0, 0, 0, 0.3));
                    shadow.setRadius(10);
                    btn.setEffect(shadow);
                }
            });

            btn.setOnMouseExited(e -> {
                if (!isDragging) {
                    btn.setStyle(btn.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", ""));
                    btn.setEffect(null);
                }
            });

            btn.setOnDragDetected(event -> {
                if (event.isPrimaryButtonDown()) {
                    isDragging = true;
                    draggedSpace = s;
                    Dragboard db = btn.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(s.getId()));
                    db.setContent(content);
                    btn.setOpacity(0.7);
                    Glow glow = new Glow();
                    glow.setLevel(0.8);
                    btn.setEffect(glow);
                    event.consume();
                }
            });

            btn.setOnDragDone(event -> {
                btn.setOpacity(1.0);
                btn.setEffect(null);
                isDragging = false;
                limpiarHighlights();
                limpiarPreview();
                if (!event.isDropCompleted()) {
                    animarRebote(btn);
                }
                event.consume();
            });
        }

        btn.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY && !isDragging) {
                mostrarMenuContextualEspacio(event, s, precio, ocupados, disponibles, estadoFinal, isBloqueado, estaCompleto);
            }
            event.consume();
        });

        return btn;
    }

    private void mostrarMenuContextualEspacio(javafx.scene.input.MouseEvent event, Space s, String precio,
            int ocupados, int disponibles, String estadoFinal,
            boolean isBloqueado, boolean estaCompleto) {

        ContextMenu menu = new ContextMenu();

        MenuItem info = new MenuItem("ℹ️ Información");
        info.setOnAction(e -> mostrarInfoEspacioDetallada(s, precio, ocupados, disponibles, estadoFinal));

        MenuItem verReservas = new MenuItem("📋 Ver reservas");
        verReservas.setOnAction(e -> mostrarReservasEspacio(s));
        if (ocupados == 0 && !isBloqueado) {
            verReservas.setDisable(true);
        }

        MenuItem cambiarCapacidad = new MenuItem("👥 Cambiar capacidad");
        cambiarCapacidad.setOnAction(e -> cambiarCapacidadEspacio(s));
        if (s.getType() == SpaceType.PASILLO) {
            cambiarCapacidad.setDisable(true);
        }

        MenuItem bloquear;

        bloquear = new MenuItem("🔒 Bloquear espacio");
        bloquear.setOnAction(e -> bloquearEspacioDialog(s));

        MenuItem eliminar = new MenuItem("🗑️ Eliminar espacio");
        eliminar.setOnAction(e -> confirmarEliminarEspacio(currentRoom, s));
        if (ocupados > 0) {
            eliminar.setDisable(true);
        }

        menu.getItems().addAll(
                info,
                new SeparatorMenuItem(),
                verReservas,
                cambiarCapacidad,
                bloquear,
                eliminar
        );

        menu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
    }

    private void mostrarInfoEspacioDetallada(Space espacio, String precio, int ocupados, int disponibles, String estado) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información del Espacio");
            alert.setHeaderText("Detalles de: " + espacio.getSpaceName());

            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 15px;");

            Label lblTipo = new Label("📋 Tipo: " + espacio.getType().toString().replace("_", " "));
            lblTipo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label lblCapacidad = new Label("👥 Capacidad Total: " + espacio.getCapacity());
            Label lblOcupados = new Label("🔴 Ocupados: " + ocupados);
            Label lblDisponibles = new Label("🟢 Disponibles: " + disponibles);
            Label lblPrecio = new Label("💰 Precio: " + precio);
            Label lblTamaño = new Label("📐 Tamaño: " + espacio.getWidth() + " x " + espacio.getHeight());
            Label lblPosicion = new Label("📍 Posición: (" + espacio.getStartCol() + ", " + espacio.getStartRow() + ")");
            Label lblEstado = new Label("🔄 Estado: " + estado);

            lblEstado.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            if (estado.contains("BLOQUEADO")) {
                lblEstado.setStyle(lblEstado.getStyle() + " -fx-text-fill: #d32f2f;");
            } else if (estado.contains("COMPLETO")) {
                lblEstado.setStyle(lblEstado.getStyle() + " -fx-text-fill: #f57c00;");
            } else if (estado.contains("DISPONIBLE")) {
                lblEstado.setStyle(lblEstado.getStyle() + " -fx-text-fill: #388e3c;");
            }

            content.getChildren().addAll(
                    lblTipo, lblCapacidad, lblOcupados, lblDisponibles,
                    lblPrecio, lblTamaño, lblPosicion, lblEstado
            );

            if (ocupados > 0) {
                Label lblReservasActivas = new Label("\n📅 Reservas Activas:");
                lblReservasActivas.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                content.getChildren().add(lblReservasActivas);

                LocalDate fecha = dpAdminDate.getValue();
                String horaInicioStr = cbAdminStartTime.getValue();
                String horaFinStr = cbAdminEndTime.getValue();

                if (fecha != null && horaInicioStr != null && horaFinStr != null) {
                    try {
                        LocalTime horaInicio = TimeSlots.parse(horaInicioStr);
                        LocalTime horaFin = TimeSlots.parse(horaFinStr);

                        List<Reservation> reservasActivas = reservationService.findBySpaceAndDateAndTime(
                                espacio.getId(), fecha, horaInicio, horaFin
                        );

                        for (Reservation reserva : reservasActivas) {
                            String nombreUsuario = reserva.getUser() != null
                                    ? reserva.getUser().getName() + " " + reserva.getUser().getLastName()
                                    : "Usuario desconocido";
                            String horaInicioRes = reserva.getStartTime() != null
                                    ? reserva.getStartTime().toLocalTime().toString() : "N/A";
                            String horaFinRes = reserva.getEndTime() != null
                                    ? reserva.getEndTime().toLocalTime().toString() : "N/A";

                            Label lblReserva = new Label("   • " + nombreUsuario
                                    + " (" + horaInicioRes + " - " + horaFinRes + ") - "
                                    + reserva.getSeatCount() + " asientos");
                            lblReserva.setStyle("-fx-font-size: 12px;");
                            content.getChildren().add(lblReserva);
                        }
                    } catch (Exception e) {
                        Label lblError = new Label("   Error al cargar reservas: " + e.getMessage());
                        lblError.setStyle("-fx-font-size: 12px; -fx-text-fill: red;");
                        content.getChildren().add(lblError);
                    }
                }
            }

            alert.getDialogPane().setContent(content);
            alert.getDialogPane().setPrefWidth(450);
            alert.showAndWait();

        } catch (Exception e) {
            mostrarError("Error al mostrar información del espacio", e.getMessage());
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void desbloquearEspacioDialog(Space espacio) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Desbloquear Espacio");
        confirmacion.setHeaderText("¿Desbloquear el espacio: " + espacio.getSpaceName() + "?");
        confirmacion.setContentText("El espacio volverá a estar disponible para reservas.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Implementar lógica para desbloquear espacio
                // spaceService.unblockSpace(espacio.getId());
                mostrarNotificacionExito("Espacio desbloqueado correctamente");
                cargarPlanoDeRoomActual();
            } catch (Exception e) {
                mostrarError("Error al desbloquear espacio", e.getMessage());
            }
        }
    }

    private void cambiarCapacidadEspacio(Space espacio) {

        LocalDate fecha = dpAdminDate.getValue();
        String horaInicioStr = cbAdminStartTime.getValue();
        String horaFinStr = cbAdminEndTime.getValue();

        int ocupadosActuales = 0;
        if (fecha != null && horaInicioStr != null && horaFinStr != null) {
            try {
                LocalTime horaInicio = TimeSlots.parse(horaInicioStr);
                LocalTime horaFin = TimeSlots.parse(horaFinStr);

                List<Reservation> reservasActivas = reservationService.findByRoomAndDateAndTime(
                        currentRoom.getId(), fecha, horaInicio, horaFin
                );

                for (Reservation reserva : reservasActivas) {
                    if (reserva.getSpace().getId().equals(espacio.getId())) {
                        ocupadosActuales += reserva.getSeatCount();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al verificar ocupación actual: " + e.getMessage());
            }
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(espacio.getCapacity()));
        dialog.setTitle("Cambiar Capacidad");
        dialog.setHeaderText("Cambiar capacidad del espacio: " + espacio.getSpaceName());

        String mensaje = "Nueva capacidad:";
        if (ocupadosActuales > 0) {
            mensaje += "\n⚠️ Actualmente ocupados: " + ocupadosActuales + " asientos";
        }
        dialog.setContentText(mensaje);

        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            try {
                int nuevaCapacidad = Integer.parseInt(result.get().trim());

                if (nuevaCapacidad < 1) {
                    utilities.showAlert(Alert.AlertType.ERROR,
                            "Capacidad inválida",
                            "La capacidad debe ser mayor a 0.");
                    return;
                }

                if (nuevaCapacidad < ocupadosActuales) {
                    utilities.showAlert(Alert.AlertType.ERROR,
                            "Capacidad insuficiente",
                            String.format("La nueva capacidad (%d) no puede ser menor que los asientos actualmente ocupados (%d).",
                                    nuevaCapacidad, ocupadosActuales));
                    return;
                }

                int capacidadMaxima = calcularCapacidadMaxima(espacio);
                if (nuevaCapacidad > capacidadMaxima) {
                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmacion.setTitle("Capacidad alta");
                    confirmacion.setHeaderText("Capacidad por encima de la recomendada");
                    confirmacion.setContentText(String.format(
                            "La capacidad ingresada (%d) excede la recomendada (%d) para este tipo de espacio.\n¿Desea continuar?",
                            nuevaCapacidad, capacidadMaxima));

                    Optional<ButtonType> respuesta = confirmacion.showAndWait();
                    if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
                        return;
                    }
                }

                espacio.setCapacity(nuevaCapacidad);
                spaceService.update(espacio);

                utilities.showAlert(Alert.AlertType.INFORMATION,
                        "Capacidad actualizada",
                        String.format("La capacidad del espacio '%s' se ha actualizado a %d personas.",
                                espacio.getSpaceName(), nuevaCapacidad));

                cargarPlanoDeRoomActual();

            } catch (NumberFormatException e) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Entrada inválida",
                        "Por favor ingrese un número válido.");
            } catch (Exception e) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo actualizar la capacidad: " + e.getMessage());
            }
        }
    }

    private int calcularCapacidadMaxima(Space espacio) {
        int area = espacio.getWidth() * espacio.getHeight();

        switch (espacio.getType()) {
            case ESCRITORIO:
                return 1;
            case SALA_REUNIONES:
                return area * 4;
            case AREA_COMUN:
                return area * 6;
            case PASILLO:
                return 0;
            default:
                return area * 2;
        }
    }

    private void bloquearEspacioDialog(Space s) {

        Dialog<LocalDate> fechaDialog = new Dialog<>();
        fechaDialog.setTitle("Bloquear espacio - Seleccionar fecha");
        fechaDialog.setHeaderText("Selecciona la fecha para bloquear el espacio '" + s.getSpaceName() + "'");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setPrefWidth(200);

        ButtonType okButtonType = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        fechaDialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Fecha:"), datePicker);
        fechaDialog.getDialogPane().setContent(vbox);

        fechaDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> fechaResult = fechaDialog.showAndWait();
        if (fechaResult.isEmpty() || fechaResult.get() == null) {
            return;
        }
        LocalDate fecha = fechaResult.get();

        List<String> slots = TimeSlots.getDefaultTimeSlots();
        ChoiceDialog<String> hi = new ChoiceDialog<>(slots.get(0), slots);
        hi.setTitle("Bloquear espacio - Hora de inicio");
        hi.setHeaderText("Selecciona la hora de inicio para bloquear");
        hi.setContentText("Hora inicio:");
        Optional<String> rhi = hi.showAndWait();
        if (rhi.isEmpty()) {
            return;
        }

        ChoiceDialog<String> hf = new ChoiceDialog<>(slots.get(slots.size() - 1), slots);
        hf.setTitle("Bloquear espacio - Hora de fin");
        hf.setHeaderText("Selecciona la hora de fin para bloquear");
        hf.setContentText("Hora fin:");
        Optional<String> rhf = hf.showAndWait();
        if (rhf.isEmpty()) {
            return;
        }

        LocalTime start = TimeSlots.parse(rhi.get()), end = TimeSlots.parse(rhf.get());
        if (!end.isAfter(start)) {
            new Alert(Alert.AlertType.ERROR, "La hora fin debe ser mayor que la hora de inicio").showAndWait();
            return;
        }

        spaceService.blockSpace(s, fecha, start, end);
        new Alert(Alert.AlertType.INFORMATION, "Espacio '" + s.getSpaceName() + "' bloqueado correctamente\nFecha: " + fecha + "\nHorario: " + start + " - " + end).showAndWait();
        cargarPlanoDeRoomActual();
    }

    private void confirmarEliminarEspacio(Room r, Space s) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmar eliminación");
        a.setHeaderText("¿Eliminar '" + s.getSpaceName() + "'?");
        a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        a.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(x -> {
            spaceService.delete(s);
            cargarPlanoDeRoomActual();
        });
    }

    @FXML
    private void clickEliminarUsuario(ActionEvent event) {
        userSeleccionado = tblUsers.getSelectionModel().getSelectedItem();

        if (userSeleccionado == null) {
            utilities.showAlert(Alert.AlertType.WARNING,
                    "Ningún usuario seleccionado",
                    "Selecciona un usuario para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que deseas eliminar al usuario: " + userSeleccionado.getFullName() + "?\nTambién se eliminarán sus reservas.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                List<Reservation> reservas = reservationService.findByUserId(userSeleccionado.getId());
                reservas.forEach(reservationService::delete);

                userService.delete(userSeleccionado.getId());
                cargarTablaUsuarios();

                utilities.showAlert(Alert.AlertType.INFORMATION,
                        "Eliminado",
                        "El usuario y sus reservas han sido eliminados correctamente.");
            }
        });
    }

    @FXML
    private void clickEditarUsuario(ActionEvent event) throws IOException {
        User seleccionado = tblUsers.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            utilities.showAlert(Alert.AlertType.WARNING, "Selecciona un usuario", "Debes seleccionar un usuario de la tabla.");
            return;
        }
        UserService.setCurrentUser(seleccionado);
        FlowController.getInstance().goView("EditUserFromAdmin");
    }

    public void refrescarVista() {
        cargarTablaUsuarios();
    }

    private static class Delta {

        double x, y;
    }

    private void onFiltrarPlano(ActionEvent e) {
        cargarPlanoDeRoomActual();
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

        int width = 1, height = 1;
        String nombre;
        int capacidad = 1;

        if (tipo == SpaceType.PASILLO) {
            width = height = 1;
            nombre = "Pasillo";
            capacidad = 0;
        } else {
            switch (tipo) {
                case AREA_COMUN:
                    width = height = 2;
                    break;
                case SALA_REUNIONES:
                    width = 3;
                    height = 2;
                    break;
                default:
                    width = height = 1;
            }

            TextInputDialog nameDlg = new TextInputDialog(tipo.toString());
            nameDlg.setTitle("Nombre");
            nameDlg.setHeaderText("Ponle un nombre al espacio:");
            nameDlg.setContentText("Nombre:");
            Optional<String> nameRes = nameDlg.showAndWait();
            if (!nameRes.isPresent() || nameRes.get().trim().isEmpty()) {
                return;
            }
            nombre = nameRes.get().trim();

            if (tipo != SpaceType.ESCRITORIO) {
                TextInputDialog capDlg = new TextInputDialog("1");
                capDlg.setTitle("Capacidad");
                capDlg.setHeaderText("Introduce la capacidad deseada:");
                capDlg.setContentText("Capacidad:");
                Optional<String> capRes = capDlg.showAndWait();
                if (!capRes.isPresent()) {
                    return;
                }
                try {
                    capacidad = Integer.parseInt(capRes.get().trim());
                    if (capacidad < 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Capacidad inválida.").showAndWait();
                    return;
                }
            }
        }

        int filas = currentRoom.getRows(), cols = currentRoom.getCols();
        boolean[][] ocupadoTemp = new boolean[filas][cols];
        for (Space s : currentRoom.getSpaces()) {
            for (int r = s.getStartRow(); r < s.getStartRow() + s.getHeight(); r++) {
                for (int c = s.getStartCol(); c < s.getStartCol() + s.getWidth(); c++) {
                    if (r >= 0 && r < filas && c >= 0 && c < cols) {
                        ocupadoTemp[r][c] = true;
                    }
                }
            }
        }
        boolean added = false;
        for (int r = 0; r <= filas - height && !added; r++) {
            for (int c = 0; c <= cols - width && !added; c++) {
                boolean libre = true;
                for (int dr = 0; dr < height && libre; dr++) {
                    for (int dc = 0; dc < width && libre; dc++) {
                        if (ocupadoTemp[r + dr][c + dc]) {
                            libre = false;
                        }
                    }
                }
                if (libre) {
                    Space nuevo = new Space(nombre, r, c, width, height, capacidad, tipo, currentRoom);
                    spaceService.update(nuevo);
                    added = true;
                }
            }
        }
        if (!added) {
            new Alert(Alert.AlertType.WARNING, "No hay espacio libre para ese tipo.").showAndWait();
        }
        cargarPlanoDeRoomActual();
    }

    @FXML
    private void onToggleSidebar(ActionEvent e) {
        double w = sidebar.getPrefWidth();
        if (w > 80) {
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

    private void showUsers() {
        toggleViews(true, false, false, false, false);
    }

    private void showRooms() {
        toggleViews(false, true, false, false, false);
    }

    private void showReservations() {
        toggleViews(false, false, true, false, false);
    }

    private void showReports() {
        toggleViews(false, false, false, true, false);
    }

    private void showCreateAdminForm() {
        toggleViews(false, false, false, false, true);
    }

    private void toggleViews(boolean u, boolean p, boolean r, boolean rep, boolean c) {
        resetToggles();
        tgViewUsers.setSelected(u);
        tgViewRooms.setSelected(p);
        tgViewReservations.setSelected(r);
        tgViewReports.setSelected(rep);
        tgCreateAdmin.setSelected(c);
        spUsersScroll.setVisible(u);
        spRoomsScroll.setVisible(p);
        spReservationsScroll.setVisible(r);
        spReportsScroll.setVisible(rep);
        formAdmin.setVisible(c);
        breadcrumb.setVisible(u);
        btnEditarUsuario.setVisible(u);
        btnEliminarUsuario.setVisible(u);
    }

    private void resetToggles() {
        tgViewUsers.setSelected(false);
        tgViewRooms.setSelected(false);
        tgViewReservations.setSelected(false);
        tgViewReports.setSelected(false);
        tgCreateAdmin.setSelected(false);
    }

    @FXML
    private void onBreadcrumbHome() {
        showUsers();
    }

    @FXML
    private void onBackAction(ActionEvent e) throws IOException {
        FlowController.getInstance().goView("LoginWindow");
    }

    private void cargarGraficoEspacios() {
        Map<SpaceType, Long> datos = reservationService.countReservationsBySpaceType();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<SpaceType, Long> entry : datos.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey().getTypeName(), entry.getValue()));
        }

        pieTipoEspacio.setData(pieData);
    }

    private void cargarGraficoUsuariosTop() {
        Map<String, Long> datos = reservationService.getTopUsersByReservations();

        System.out.println("Top usuarios con reservas: " + datos);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservas por usuario");

        for (Map.Entry<String, Long> entry : datos.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barUsuarios.getData().clear();
        barUsuarios.getData().add(series);
    }

    private void cargarGraficoHorarios() {
        Map<String, Long> datos = reservationService.countReservationsByHourSlot();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservas por hora");

        for (Map.Entry<String, Long> entry : datos.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barHoras.getData().clear();
        barHoras.getData().add(series);
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
        System.out.println("CLICK EN REPORTES");
        showReports();
    }

    private void clearAdminFields() {
        txtAdminId.clear();
        txtAdminName.clear();
        txtAdminLastName.clear();
        txtAdminUser.clear();
        txtAdminPass.clear();
    }

    @FXML
    private void tgCreateAdmin(ActionEvent event) {
        showCreateAdminForm();
    }

    @FXML
    private void onCreateAdmin(ActionEvent e) {
        try {
            String firstName = txtAdminName.getText().trim();
            String lastName = txtAdminLastName.getText().trim();
            String user = txtAdminUser.getText().trim();
            String password = txtAdminPass.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || user.isEmpty() || password.isEmpty()) {
                utilities.showAlert(Alert.AlertType.WARNING,
                        "Campos incompletos",
                        "Por favor, complete todos los campos.");
                return;
            }

            String idText = txtAdminId.getText().trim();
            if (!idText.matches("\\d+")) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Cédula inválida",
                        "La cédula debe contener solo números.");
                return;
            }

            if (idText.length() != 9) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Cédula inválida",
                        "La cédula debe tener exactamente 9 dígitos.");
                return;
            }

            Long id = Long.parseLong(idText);

            if (userService.findByIdentification(id) != null) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Cédula ya registrada",
                        "Ya existe un usuario con esta cédula.");
                return;
            }

            if (userService.findByUserName(user) != null) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Nombre de usuario duplicado",
                        "El nombre de usuario ya está en uso.");
                return;
            }

            if (radioAdmin.isSelected()) {
                Administrator admin = new Administrator(id, firstName, lastName, user, password);
                userService.save(admin);

                utilities.showAlert(Alert.AlertType.INFORMATION,
                        "Éxito",
                        "Administrador creado correctamente.");
            } else {
                Customer cliente = new Customer(id, firstName, lastName, user, password);
                userService.save(cliente);

                utilities.showAlert(Alert.AlertType.INFORMATION,
                        "Éxito",
                        "Cliente creado correctamente.");
            }

            clearAdminFields();
            cargarTablaUsuarios();

        } catch (Exception ex) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo crear el usuario.");
            ex.printStackTrace();
        }
    }

    private void mostrarInfoEspacio(Space s, String precio) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Información del Espacio");
        info.setHeaderText(s.getSpaceName());
        info.setContentText(String.format(
                "Tipo: %s\n"
                + "Capacidad: %d personas\n"
                + "Precio: %s\n"
                + "Dimensiones: %d x %d\n"
                + "Posición: Columna %d, Fila %d\n"
                + "ID: %d",
                s.getType().toString().replace("_", " "),
                s.getCapacity(),
                precio,
                s.getWidth(), s.getHeight(),
                s.getStartCol(), s.getStartRow(),
                s.getId()
        ));
        info.showAndWait();
    }

    private void mostrarReservasEspacio(Space espacio) {
        LocalDate fecha = dpAdminDate.getValue();
        String horaInicioStr = cbAdminStartTime.getValue();
        String horaFinStr = cbAdminEndTime.getValue();

        if (fecha == null || horaInicioStr == null || horaFinStr == null) {
            utilities.showAlert(Alert.AlertType.WARNING,
                    "Filtros incompletos",
                    "Selecciona fecha y horarios para ver las reservas.");
            return;
        }

        try {
            LocalTime horaInicio = TimeSlots.parse(horaInicioStr);
            LocalTime horaFin = TimeSlots.parse(horaFinStr);

            List<Reservation> reservas = reservationService.findByRoomAndDateAndTime(
                    currentRoom.getId(), fecha, horaInicio, horaFin
            ).stream()
                    .filter(r -> r.getSpace().getId().equals(espacio.getId()))
                    .collect(Collectors.toList());

            if (reservas.isEmpty()) {
                utilities.showAlert(Alert.AlertType.INFORMATION,
                        "Sin reservas",
                        "No hay reservas activas para este espacio en el período seleccionado.");
                return;
            }

            StringBuilder info = new StringBuilder();
            info.append("Reservas activas para: ").append(espacio.getSpaceName()).append("\n");
            info.append("Fecha: ").append(fecha).append("\n");
            info.append("Período: ").append(horaInicio).append(" - ").append(horaFin).append("\n\n");

            int totalOcupados = 0;
            for (Reservation reserva : reservas) {
                info.append("• Usuario: ").append(reserva.getUser().getFullName()).append("\n");
                info.append("  Asientos: ").append(reserva.getSeatCount()).append("\n");
                info.append("  Horario: ").append(reserva.getStartTime().toLocalTime())
                        .append(" - ").append(reserva.getEndTime().toLocalTime()).append("\n");
                info.append("  ID Reserva: ").append(reserva.getId()).append("\n\n");
                totalOcupados += reserva.getSeatCount();
            }

            info.append("Total ocupados: ").append(totalOcupados)
                    .append("/").append(espacio.getCapacity());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reservas del Espacio");
            alert.setHeaderText("Información de Reservas");
            alert.setContentText(info.toString());

            alert.getDialogPane().setPrefWidth(450);
            alert.showAndWait();

        } catch (Exception e) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudieron cargar las reservas: " + e.getMessage());
        }
    }

    private void animarRebote(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    private void mostrarNotificacionExito(String mensaje) {
        Label notification = new Label("✅ " + mensaje);
        notification.setStyle(
                "-fx-background-color: #4CAF50; "
                + "-fx-text-fill: white; "
                + "-fx-padding: 10px; "
                + "-fx-background-radius: 5px; "
                + "-fx-font-weight: bold;"
        );

        if (spGeneralContent != null) {
            spGeneralContent.getChildren().add(notification);

            FadeTransition fade = new FadeTransition(Duration.millis(2000), notification);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.millis(1000));
            fade.setOnFinished(e -> spGeneralContent.getChildren().remove(notification));
            fade.play();
        }
    }

}
