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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
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

    graphicUtilities utilities = new graphicUtilities();

    private UserService userService = new UserService();

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

    private void mostrarZonasValidas(Space espacio) {
        limpiarHighlights();

        if (espacio == null || currentRoom == null) {
            return;
        }

        for (int row = 0; row <= currentRoom.getRows() - espacio.getHeight(); row++) {
            for (int col = 0; col <= currentRoom.getCols() - espacio.getWidth(); col++) {
                if (esPosicionValida(espacio, row, col)) {
                    Region highlight = new Region();
                    highlight.setStyle(
                            "-fx-background-color: rgba(76, 175, 80, 0.3); "
                            + "-fx-border-color: #4CAF50; "
                            + "-fx-border-width: 2px; "
                            + "-fx-border-style: dashed;"
                    );
                    highlight.setMouseTransparent(true);

                    gridPlano.add(highlight, col, row, espacio.getWidth(), espacio.getHeight());
                    highlightCells.add(highlight);
                }
            }
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
            cargarTablaEspacios();
            cargarTablaUsuariosTop();
            cargarTablaHorarios();
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

        Set<Long> ocupados = new HashSet<>();
        Set<Long> bloqueados = new HashSet<>();

        if (fecha != null && horaInicioStr != null && horaFinStr != null) {
            try {
                LocalTime horaInicio = TimeSlots.parse(horaInicioStr);
                LocalTime horaFin = TimeSlots.parse(horaFinStr);

                ocupados = reservationService.findByRoomAndDateAndTime(
                        currentRoom.getId(), fecha, horaInicio, horaFin
                ).stream().map(r -> r.getSpace().getId()).collect(Collectors.toSet());

                bloqueados = spaceService.findBlockedSpaces(
                        currentRoom.getId(), fecha, horaInicio, horaFin
                ).stream().map(Space::getId).collect(Collectors.toSet());
            } catch (Exception e) {
                System.err.println("Error al cargar estados de espacios: " + e.getMessage());
            }
        }

        for (Space s : currentRoom.getSpaces()) {
            try {
                Node nodo = crearVisualEspacio(s, ocupados.contains(s.getId()), bloqueados.contains(s.getId()));
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

    private Node crearVisualEspacio(Space s, boolean isOcup, boolean isBloq) {
        Button btn = new Button(s.getSpaceName());
        btn.setFont(Font.font("Segoe UI", 12));
        btn.setPrefSize(60 * s.getWidth(), 60 * s.getHeight());

        String color;
        String precio;
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
                precio = "Gratis";
                break;
            default:
                color = "#bdbdbd";
                precio = "$10/hora";
                break;
        }

        if (isOcup) {
            color = "#e57373";
            btn.setDisable(true);
        } else if (isBloq) {
            color = "#616161";
            btn.setDisable(true);
        }

        btn.setStyle(String.format(
                "-fx-background-color: %s; "
                + "-fx-text-fill: #222; "
                + "-fx-border-radius: 8px; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(0,0,0,0.1); "
                + "-fx-border-width: 1px; "
                + "-fx-font-weight: bold; "
                + "-fx-cursor: hand;",
                color
        ));

        Tooltip tooltip = new Tooltip();
        tooltip.setText(String.format(
                "Tipo: %s\nCapacidad: %d personas\nPrecio: %s\nTamaño: %dx%d\nPosición: (%d,%d)%s%s",
                s.getType().toString().replace("_", " "),
                s.getCapacity(),
                precio,
                s.getWidth(), s.getHeight(),
                s.getStartCol(), s.getStartRow(),
                isOcup ? "\n⚠️ OCUPADO" : "",
                isBloq ? "\n🚫 BLOQUEADO" : ""
        ));
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #333; -fx-text-fill: white;");
        tooltip.setShowDelay(Duration.millis(300));
        Tooltip.install(btn, tooltip);

        if (!btn.isDisabled()) {
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
        }

        if (!btn.isDisabled()) {
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
                ContextMenu menu = new ContextMenu();

                MenuItem info = new MenuItem("ℹ️ Información");
                info.setOnAction(e -> mostrarInfoEspacio(s, precio));

                MenuItem eliminar = new MenuItem("🗑️ Eliminar espacio");
                eliminar.setOnAction(e -> confirmarEliminarEspacio(currentRoom, s));

                MenuItem bloquear = new MenuItem("🔒 Bloquear espacio");
                bloquear.setOnAction(e -> bloquearEspacioDialog(s));

                if (btn.isDisabled()) {
                    eliminar.setDisable(true);
                    bloquear.setDisable(true);
                }

                menu.getItems().addAll(info, new SeparatorMenuItem(), bloquear, eliminar);
                menu.show(btn, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        });

        return btn;
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

    private boolean puedeMover(Space espacioAMover, int nuevaFila, int nuevaColumna) {
        if (nuevaFila < 0 || nuevaColumna < 0
                || nuevaFila + espacioAMover.getHeight() > currentRoom.getRows()
                || nuevaColumna + espacioAMover.getWidth() > currentRoom.getCols()) {
            return false;
        }
        for (Space otroEspacio : currentRoom.getSpaces()) {
            if (!otroEspacio.getId().equals(espacioAMover.getId())) {
                boolean superponeHorizontal = nuevaColumna < otroEspacio.getStartCol() + otroEspacio.getWidth()
                        && nuevaColumna + espacioAMover.getWidth() > otroEspacio.getStartCol();
                boolean superponeVertical = nuevaFila < otroEspacio.getStartRow() + otroEspacio.getHeight()
                        && nuevaFila + espacioAMover.getHeight() > otroEspacio.getStartRow();

                if (superponeHorizontal && superponeVertical) {
                    return false;
                }
            }
        }

        return true;
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
                "¿Seguro que deseas eliminar al usuario: " + userSeleccionado.getFullName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                userService.delete(userSeleccionado.getId());
                cargarTablaUsuarios();

                utilities.showAlert(Alert.AlertType.INFORMATION,
                        "Eliminado",
                        "Usuario eliminado correctamente.");
            }
        });
    }

    @FXML
    private void clickEditarUsuario(ActionEvent event) {
    ////
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

    private void cargarTablaEspacios() {
        Map<SpaceType, Long> datos = reservationService.countReservationsBySpaceType();
        ObservableList<Map.Entry<SpaceType, Long>> items = FXCollections.observableArrayList(datos.entrySet());
        colTipoEspacio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey().toString()));
        colCantidadEspacio.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getValue()).asObject());
        tblEspaciosReservados.setItems(items);
    }

    private void cargarTablaUsuariosTop() {
        Map<String, Long> datos = reservationService.getTopUsersByReservations();
        ObservableList<Map.Entry<String, Long>> items = FXCollections.observableArrayList(datos.entrySet());
        colUsuarioNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
        colCantidadUsuario.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getValue()).asObject());
        tblUsuariosReservas.setItems(items);
    }

    private void cargarTablaHorarios() {
        Map<String, Long> datos = reservationService.countReservationsByHourSlot();
        ObservableList<Map.Entry<String, Long>> items = FXCollections.observableArrayList(datos.entrySet());
        colHoraInicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
        colCantidadHora.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getValue()).asObject());
        tblHorariosReservas.setItems(items);
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
        cargarTablaEspacios();
        cargarTablaUsuariosTop();
        cargarTablaHorarios();
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
            if (idText.length() > 9) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Cédula demasiado larga",
                        "La cédula no puede tener más de 9 dígitos.");
                return;
            }
            if (idText.length() < 9) {
                utilities.showAlert(Alert.AlertType.ERROR,
                        "Cédula demasiado corta",
                        "La cédula no puede tener menos de 9 dígitos.");
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

            Administrator admin = new Administrator(id, firstName, lastName, user, password);
            userService.save(admin);

            utilities.showAlert(Alert.AlertType.INFORMATION,
                    "Éxito",
                    "Administrador creado correctamente.");
            clearAdminFields();

        } catch (Exception ex) {
            utilities.showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo crear el administrador.");
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
