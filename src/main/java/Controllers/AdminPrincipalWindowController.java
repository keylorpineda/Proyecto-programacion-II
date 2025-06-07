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

    private Room currentRoom;
    private final RoomService roomService = new RoomService();
    private final SpaceService spaceService = new SpaceService();
    private final ReservationService reservationService = new ReservationService();
    private final ReportService reportService = new ReportService();
    private Node draggedNode;
    private Space draggedSpace;
    private boolean isDragging = false;

    private boolean esPosicionValida(Space espacioAMover, int nuevaFila, int nuevaColumna) {
        if (espacioAMover == null || currentRoom == null) {
            return false;
        }

        // Verificar límites del room
        if (nuevaFila < 0 || nuevaColumna < 0) {
            return false;
        }

        if (nuevaFila + espacioAMover.getHeight() > currentRoom.getRows()
                || nuevaColumna + espacioAMover.getWidth() > currentRoom.getCols()) {
            return false;
        }

        // Verificar colisiones con otros espacios
        for (Space otroEspacio : currentRoom.getSpaces()) {
            // No verificar colisión consigo mismo
            if (otroEspacio.getId().equals(espacioAMover.getId())) {
                continue;
            }

            // Verificar si hay superposición usando lógica de rectángulos
            if (hayColision(
                    nuevaColumna, nuevaFila, espacioAMover.getWidth(), espacioAMover.getHeight(),
                    otroEspacio.getStartCol(), otroEspacio.getStartRow(), otroEspacio.getWidth(), otroEspacio.getHeight()
            )) {
                return false;
            }
        }

        return true;
    }

    private boolean puedeMoverMejorado(Space espacioAMover, int nuevaFila, int nuevaColumna) {
        if (espacioAMover == null || currentRoom == null) {
            return false;
        }

        // Verificar límites del room
        if (nuevaFila < 0 || nuevaColumna < 0) {
            System.out.println("Fuera de límites: posición negativa");
            return false;
        }

        if (nuevaFila + espacioAMover.getHeight() > currentRoom.getRows()
                || nuevaColumna + espacioAMover.getWidth() > currentRoom.getCols()) {
            System.out.println("Fuera de límites: excede dimensiones del room");
            return false;
        }

        // Verificar colisiones con otros espacios
        for (Space otroEspacio : currentRoom.getSpaces()) {
            // No verificar colisión consigo mismo
            if (otroEspacio.getId().equals(espacioAMover.getId())) {
                continue;
            }

            // Calcular los límites del espacio a mover en la nueva posición
            int espacioIzquierda = nuevaColumna;
            int espacioDerecha = nuevaColumna + espacioAMover.getWidth() - 1;
            int espacioArriba = nuevaFila;
            int espacioAbajo = nuevaFila + espacioAMover.getHeight() - 1;

            // Calcular los límites del otro espacio
            int otroIzquierda = otroEspacio.getStartCol();
            int otroDerecha = otroEspacio.getStartCol() + otroEspacio.getWidth() - 1;
            int otroArriba = otroEspacio.getStartRow();
            int otroAbajo = otroEspacio.getStartRow() + otroEspacio.getHeight() - 1;

            // Verificar si hay superposición
            boolean superponeHorizontal = !(espacioDerecha < otroIzquierda || espacioIzquierda > otroDerecha);
            boolean superponeVertical = !(espacioAbajo < otroArriba || espacioArriba > otroAbajo);

            if (superponeHorizontal && superponeVertical) {
                System.out.println("Colisión detectada con espacio: " + otroEspacio.getSpaceName());
                return false;
            }
        }

        return true;
    }

    // Reemplaza el método initialize() en tu AdminPrincipalWindowController
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

        // DRAG AND DROP MEJORADO
        gridPlano.setOnDragOver(event -> {
            if (event.getGestureSource() != gridPlano && event.getDragboard().hasString() && draggedSpace != null) {
                event.acceptTransferModes(TransferMode.MOVE);

                // Calcular posición objetivo más precisa
                Point2D localPoint = gridPlano.sceneToLocal(event.getSceneX(), event.getSceneY());

                // Obtener el tamaño real de cada celda
                double cellWidth = gridPlano.getWidth() / currentRoom.getCols();
                double cellHeight = gridPlano.getHeight() / currentRoom.getRows();

                // Si las celdas no tienen tamaño aún, usar 60 por defecto
                if (cellWidth <= 0) {
                    cellWidth = 60;
                }
                if (cellHeight <= 0) {
                    cellHeight = 60;
                }

                int targetCol = (int) Math.floor(localPoint.getX() / cellWidth);
                int targetRow = (int) Math.floor(localPoint.getY() / cellHeight);

                // Asegurar que el espacio quepa completamente en el grid
                targetCol = Math.max(0, Math.min(targetCol, currentRoom.getCols() - draggedSpace.getWidth()));
                targetRow = Math.max(0, Math.min(targetRow, currentRoom.getRows() - draggedSpace.getHeight()));

                // Verificar si la posición es válida
                if (esPosicionValida(draggedSpace, targetRow, targetCol)) {
                    gridPlano.setStyle("-fx-background-color: rgba(76, 175, 80, 0.3); -fx-border-color: #4CAF50; -fx-border-width: 3px;");
                } else {
                    gridPlano.setStyle("-fx-background-color: rgba(244, 67, 54, 0.3); -fx-border-color: #F44336; -fx-border-width: 3px;");
                }
            }
            event.consume();
        });

        gridPlano.setOnDragExited(event -> {
            gridPlano.setStyle("");
            event.consume();
        });

        gridPlano.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && draggedSpace != null) {
                try {
                    // Calcular posición objetivo
                    Point2D localPoint = gridPlano.sceneToLocal(event.getSceneX(), event.getSceneY());

                    double cellWidth = gridPlano.getWidth() / currentRoom.getCols();
                    double cellHeight = gridPlano.getHeight() / currentRoom.getRows();

                    if (cellWidth <= 0) {
                        cellWidth = 60;
                    }
                    if (cellHeight <= 0) {
                        cellHeight = 60;
                    }

                    int targetCol = (int) Math.floor(localPoint.getX() / cellWidth);
                    int targetRow = (int) Math.floor(localPoint.getY() / cellHeight);

                    // Asegurar límites
                    targetCol = Math.max(0, Math.min(targetCol, currentRoom.getCols() - draggedSpace.getWidth()));
                    targetRow = Math.max(0, Math.min(targetRow, currentRoom.getRows() - draggedSpace.getHeight()));

                    // Verificar si la posición es válida
                    if (esPosicionValida(draggedSpace, targetRow, targetCol)) {
                        // Actualizar posición del espacio
                        draggedSpace.setStartCol(targetCol);
                        draggedSpace.setStartRow(targetRow);
                        spaceService.update(draggedSpace);
                        success = true;

                        mostrarNotificacionExito("Espacio '" + draggedSpace.getSpaceName() + "' movido correctamente");
                    } else {
                        new Alert(Alert.AlertType.WARNING, "No se puede mover el espacio a esa posición").showAndWait();
                    }
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Error al mover el espacio: " + e.getMessage()).showAndWait();
                }
            }

            // Limpiar estado
            draggedNode = null;
            draggedSpace = null;
            isDragging = false;
            gridPlano.setStyle("");

            if (success) {
                cargarPlanoDeRoomActual();
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean hayColision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        if (x1 + w1 <= x2 || x2 + w2 <= x1) {
            return false;
        }

        if (y1 + h1 <= y2 || y2 + h2 <= y1) {
            return false;
        }

        return true;
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

        draggedNode = null;
        draggedSpace = null;
        isDragging = false;
    }

    private static class DragContext {

        double offsetX;
        double offsetY;
    }

    private Node crearVisualEspacio(Space s, boolean isOcup, boolean isBloq) {
        Button btn = new Button(s.getSpaceName());
        btn.setFont(Font.font("Segoe UI", 12));
        btn.setPrefSize(60 * s.getWidth(), 60 * s.getHeight());

        // Colores y precios por tipo
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
                    draggedNode = btn;

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
        LocalDate fecha = dpAdminDate.getValue();
        if (fecha == null) {
            new Alert(Alert.AlertType.WARNING, "Debe seleccionar fecha").showAndWait();
            return;
        }
        List<String> slots = TimeSlots.getDefaultTimeSlots();
        ChoiceDialog<String> hi = new ChoiceDialog<>(slots.get(0), slots);
        hi.setTitle("Bloquear espacio");
        hi.setHeaderText("Hora inicio");
        Optional<String> rhi = hi.showAndWait();
        if (rhi.isEmpty()) {
            return;
        }
        ChoiceDialog<String> hf = new ChoiceDialog<>(slots.get(slots.size() - 1), slots);
        hf.setTitle("Bloquear espacio");
        hf.setHeaderText("Hora fin");
        Optional<String> rhf = hf.showAndWait();
        if (rhf.isEmpty()) {
            return;
        }
        LocalTime start = TimeSlots.parse(rhi.get()), end = TimeSlots.parse(rhf.get());
        if (!end.isAfter(start)) {
            new Alert(Alert.AlertType.ERROR, "La hora fin debe ser mayor").showAndWait();
            return;
        }
        spaceService.blockSpace(s, fecha, start, end);
        new Alert(Alert.AlertType.INFORMATION, "Espacio bloqueado").showAndWait();
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

    private static class Delta {

        double x, y;
    }

    @FXML
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
        switch (tipo) {
            case AREA_COMUN:
                width = height = 2;
                break;
            case SALA_REUNIONES:
                width = 3;
                height = 2;
                break;
            case PASILLO:
                width = height = 1;
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
        String nombre = nameRes.get().trim();

        int capacidad = 1;
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

    @FXML
    private void onCreateAdmin(ActionEvent e) {
        txtAdminId.clear();
        txtAdminName.clear();
        txtAdminLastName.clear();
        txtAdminUser.clear();
        txtAdminPass.clear();
        showUsers();
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

    @FXML
    private void tgCreateAdmin(ActionEvent event) {
        showCreateAdminForm();
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
