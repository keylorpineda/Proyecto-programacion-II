package Controllers;

import Models.Room;
import Models.Space;
import Models.Reservation;
import Services.RoomService;
import Services.SpaceService;
import Services.ReservationService;
import Utilities.FlowController;
import Utilities.TimeSlots;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class MakeReservationWindowController implements Initializable {

    @FXML
    private DatePicker dpDateOption;
    @FXML
    private ComboBox<String> cbStartTime;
    @FXML
    private ComboBox<String> cbEndTime;
    @FXML
    private TabPane tpTabChooseSpace;
    @FXML
    private GridPane gpFloor1;
    @FXML
    private GridPane gpFloor2;
    @FXML
    private GridPane gpFloor3;
    @FXML
    private Button btnBack;
    @FXML
    private Button btnGoToPay;

    private Label lblSelectedSpaces;
    private final RoomService roomService = new RoomService();
    private final ReservationService resService = new ReservationService();
    private final SpaceService spaceService = new SpaceService();

    private List<Room> floor1Rooms = new ArrayList<>();
    private List<Room> floor2Rooms = new ArrayList<>();
    private List<Room> floor3Rooms = new ArrayList<>();
    private final Set<Space> selectedSpaces = new HashSet<>();
    private static final double CELL_SIZE = 70;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeComponents();
            loadRooms();
            setupEventListeners();
            Platform.runLater(() -> reloadMatrix());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "Error al inicializar la ventana: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        if (dpDateOption == null || cbStartTime == null || cbEndTime == null
                || tpTabChooseSpace == null || gpFloor1 == null || gpFloor2 == null || gpFloor3 == null) {
            showAlert(Alert.AlertType.ERROR, "Error FXML",
                    "Algunos componentes no se pudieron cargar. Verifique los fx:id en el archivo FXML.");
            return;
        }

        dpDateOption.setValue(LocalDate.now());
        dpDateOption.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        cbStartTime.getItems().setAll(TimeSlots.getDefaultTimeSlots());
        cbEndTime.getItems().setAll(TimeSlots.getDefaultTimeSlots());

        if (!cbStartTime.getItems().isEmpty()) {
            cbStartTime.setValue(cbStartTime.getItems().get(0));
        }
        if (cbEndTime.getItems().size() > 1) {
            cbEndTime.setValue(cbEndTime.getItems().get(1));
        }

        lblSelectedSpaces = new Label("Espacios seleccionados: 0");
        lblSelectedSpaces.setFont(Font.font("Segoe UI", 14));
        lblSelectedSpaces.setTextFill(Color.web("#2196F3"));

        configureGridPanes();
    }

    private void configureGridPanes() {
        if (gpFloor1 != null) {
            gpFloor1.setHgap(5);
            gpFloor1.setVgap(5);
            gpFloor1.setPadding(new Insets(10));
        }
        if (gpFloor2 != null) {
            gpFloor2.setHgap(5);
            gpFloor2.setVgap(5);
            gpFloor2.setPadding(new Insets(10));
        }
        if (gpFloor3 != null) {
            gpFloor3.setHgap(5);
            gpFloor3.setVgap(5);
            gpFloor3.setPadding(new Insets(10));
        }
    }

    private void loadRooms() {
        try {
            floor1Rooms = roomService.findByFloor(1);
            floor2Rooms = roomService.findByFloor(2);
            floor3Rooms = roomService.findByFloor(3);

            if (floor1Rooms.isEmpty() && floor2Rooms.isEmpty() && floor3Rooms.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Advertencia",
                        "No se encontraron salas en ningún piso. Verifique la base de datos.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar las salas: " + e.getMessage());
        }
    }

    private void setupEventListeners() {
        if (dpDateOption != null) {
            dpDateOption.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    reloadMatrix();
                }
            });
        }

        if (cbStartTime != null) {
            cbStartTime.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    reloadMatrix();
                }
            });
        }

        if (cbEndTime != null) {
            cbEndTime.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    reloadMatrix();
                }
            });
        }

        if (tpTabChooseSpace != null) {
            tpTabChooseSpace.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    selectedSpaces.clear();
                    updateSelectedSpacesLabel();
                    reloadMatrix();
                }
            });
        }
    }

    private void reloadMatrix() {
        try {
            GridPane currentPane = getCurrentGridPane();
            if (currentPane == null) {
                showAlert(Alert.AlertType.WARNING, "Advertencia",
                        "No se pudo obtener el panel actual. Verifique que las pestañas estén configuradas correctamente.");
                return;
            }

            currentPane.getChildren().clear();

            LocalDate selectedDate = dpDateOption.getValue();
            String startStr = cbStartTime.getValue();
            String endStr = cbEndTime.getValue();

            if (selectedDate == null || startStr == null || endStr == null) {
                addMessageToPane(currentPane, "Seleccione fecha y horas para ver disponibilidad.", "#666666");
                return;
            }

            LocalTime startTime, endTime;
            try {
                startTime = LocalTime.parse(startStr);
                endTime = LocalTime.parse(endStr);
            } catch (Exception e) {
                addMessageToPane(currentPane, "Formato de hora inválido.", "#e57373");
                return;
            }

            if (!startTime.isBefore(endTime)) {
                addMessageToPane(currentPane, "La hora de inicio debe ser antes de la hora final.", "#e57373");
                return;
            }

            Room currentRoom = getCurrentRoom();
            if (currentRoom == null) {
                addMessageToPane(currentPane, "No hay salas disponibles en este piso.", "#ff9800");
                return;
            }

            // ARREGLADO: Usar el método correcto del servicio
            List<Reservation> reservations = resService.findByRoomAndDateAndTime(
                    currentRoom.getId(), selectedDate, startTime, endTime
            );

            Set<Long> occupiedSpaces = reservations.stream()
                    .map(r -> r.getSpace().getId())
                    .collect(Collectors.toSet());

            // ARREGLADO: Verificar si el método existe antes de llamarlo
            Set<Long> blockedSpaces = new HashSet<>();
            try {
                List<Space> blocked = spaceService.findBlockedSpaces(
                        currentRoom.getId(), selectedDate, startTime, endTime
                );
                blockedSpaces = blocked.stream()
                        .map(Space::getId)
                        .collect(Collectors.toSet());
            } catch (Exception e) {
                System.out.println("Método findBlockedSpaces no disponible o error: " + e.getMessage());
                // Continuar sin espacios bloqueados
            }

            createMatrix(currentPane, currentRoom, occupiedSpaces, blockedSpaces);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar la disponibilidad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMessageToPane(GridPane pane, String message, String color) {
        Label lbl = new Label(message);
        lbl.setFont(Font.font("Segoe UI", 16));
        lbl.setTextFill(Color.web(color));
        lbl.setWrapText(true);
        pane.add(lbl, 0, 0);
        GridPane.setColumnSpan(lbl, Integer.MAX_VALUE);
    }

    private void createMatrix(GridPane pane, Room room, Set<Long> occupiedSpaces, Set<Long> blockedSpaces) {
        pane.getChildren().clear();

        if (lblSelectedSpaces != null) {
            pane.add(lblSelectedSpaces, 0, 0);
            GridPane.setColumnSpan(lblSelectedSpaces, Integer.MAX_VALUE);
        }

        int rows = room.getRows();
        int cols = room.getCols();
        int startRow = 1;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final int finalRow = row;
                final int finalCol = col;

                Optional<Space> spaceOpt = room.getSpaces().stream()
                        .filter(s -> s.getStartRow() == finalRow && s.getStartCol() == finalCol)
                        .findFirst();

                if (spaceOpt.isPresent()) {
                    Space space = spaceOpt.get();
                    Button spaceButton = createSpaceButton(space, occupiedSpaces, blockedSpaces);
                    pane.add(spaceButton, col, startRow + row, space.getWidth(), space.getHeight());
                } else {
                    Region emptyRegion = new Region();
                    emptyRegion.setPrefSize(CELL_SIZE, CELL_SIZE);
                    emptyRegion.setStyle("-fx-background-color: transparent;");
                    pane.add(emptyRegion, col, startRow + row);
                }
            }
        }

        updateSelectedSpacesLabel();
        updatePayButtonState();
    }

    private Button createSpaceButton(Space space, Set<Long> occupiedSpaces, Set<Long> blockedSpaces) {
        Button btn = new Button(space.getSpaceName());
        btn.setPrefSize(CELL_SIZE * space.getWidth(), CELL_SIZE * space.getHeight());
        btn.setFont(Font.font("Segoe UI", 10));
        btn.setWrapText(true);

        String baseColor = getSpaceColor(space);

        if (occupiedSpaces.contains(space.getId())) {
            btn.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 6; -fx-background-radius: 6;");
            btn.setDisable(true);
            btn.setText(btn.getText() + "\n(Ocupado)");
        } else if (blockedSpaces.contains(space.getId())) {
            btn.setStyle("-fx-background-color: #616161; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 6; -fx-background-radius: 6;");
            btn.setDisable(true);
            btn.setText(btn.getText() + "\n(Bloqueado)");
        } else {
            boolean isSelected = selectedSpaces.contains(space);
            String buttonColor = isSelected ? "#388e3c" : baseColor;
            String textColor = isSelected ? "white" : "#222";

            btn.setStyle("-fx-background-color: " + buttonColor + "; -fx-text-fill: " + textColor + "; -fx-border-radius: 6; -fx-background-radius: 6;");

            btn.setOnAction(ev -> toggleSpaceSelection(space, btn, baseColor));
        }

        setupSpaceTooltip(btn, space);
        return btn;
    }

    private void toggleSpaceSelection(Space space, Button button, String baseColor) {
        if (selectedSpaces.contains(space)) {
            selectedSpaces.remove(space);
            button.setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: #222; -fx-border-radius: 6; -fx-background-radius: 6;");
        } else {
            selectedSpaces.add(space);
            button.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6;");
        }
        updateSelectedSpacesLabel();
        updatePayButtonState();
    }

    private String getSpaceColor(Space space) {
        switch (space.getType()) {
            case ESCRITORIO:
                return "#90caf9";
            case SALA_REUNIONES:
                return "#ffb300";
            case AREA_COMUN:
                return "#a5d6a7";
            case PASILLO:
                return "#a1887f";
            default:
                return "#bdbdbd";
        }
    }

    private void setupSpaceTooltip(Button btn, Space space) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(buildSpaceInfo(space));
        tooltip.setFont(Font.font("Segoe UI", 12));
        tooltip.setShowDelay(javafx.util.Duration.millis(500));
        Tooltip.install(btn, tooltip);
    }

    private String buildSpaceInfo(Space space) {
        StringBuilder info = new StringBuilder();
        info.append("Nombre: ").append(space.getSpaceName()).append("\n");
        info.append("Tipo: ").append(space.getType().toString().replace("_", " ")).append("\n");
        info.append("Tamaño: ").append(space.getWidth()).append("x").append(space.getHeight()).append("\n");
        info.append("Posición: (").append(space.getStartRow()).append(",").append(space.getStartCol()).append(")");

        if (space.getCapacity() > 0) {
            info.append("\nCapacidad: ").append(space.getCapacity()).append(" personas");
        }

        return info.toString();
    }

    private void updateSelectedSpacesLabel() {
        if (lblSelectedSpaces != null) {
            lblSelectedSpaces.setText("Espacios seleccionados: " + selectedSpaces.size());
        }
    }

    private void updatePayButtonState() {
        if (btnGoToPay != null) {
            btnGoToPay.setDisable(selectedSpaces.isEmpty());
        }
    }

    private GridPane getCurrentGridPane() {
        if (tpTabChooseSpace == null) {
            return null;
        }

        Tab selectedTab = tpTabChooseSpace.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            return gpFloor1;
        }

        String tabText = selectedTab.getText();
        if ("Piso 1".equals(tabText)) {
            return gpFloor1;
        } else if ("Piso 2".equals(tabText)) {
            return gpFloor2;
        } else if ("Piso 3".equals(tabText)) {
            return gpFloor3;
        }
        return gpFloor1;
    }

    private Room getCurrentRoom() {
        if (tpTabChooseSpace == null) {
            return null;
        }

        Tab selectedTab = tpTabChooseSpace.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            if (!floor1Rooms.isEmpty()) {
                return floor1Rooms.get(0);
            }
            if (!floor2Rooms.isEmpty()) {
                return floor2Rooms.get(0);
            }
            if (!floor3Rooms.isEmpty()) {
                return floor3Rooms.get(0);
            }
            return null;
        }

        String tabText = selectedTab.getText();
        if ("Piso 1".equals(tabText) && !floor1Rooms.isEmpty()) {
            return floor1Rooms.get(0);
        } else if ("Piso 2".equals(tabText) && !floor2Rooms.isEmpty()) {
            return floor2Rooms.get(0);
        } else if ("Piso 3".equals(tabText) && !floor3Rooms.isEmpty()) {
            return floor3Rooms.get(0);
        }
        return null;
    }

    private void backUserView() {
        try {
            FlowController.getInstance().goView("UserViewWindow");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo volver a la vista de usuario: " + e.getMessage());
        }
    }

    private void goToPay() {
        if (selectedSpaces.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Seleccione al menos un espacio para reservar.");
            return;
        }

        LocalDate selectedDate = dpDateOption.getValue();
        String startTime = cbStartTime.getValue();
        String endTime = cbEndTime.getValue();

        StringBuilder reservationDetails = new StringBuilder();
        reservationDetails.append("Fecha: ").append(selectedDate).append("\n");
        reservationDetails.append("Horario: ").append(startTime).append(" - ").append(endTime).append("\n");
        reservationDetails.append("Espacios seleccionados: ").append(selectedSpaces.size()).append("\n\n");

        for (Space space : selectedSpaces) {
            reservationDetails.append("- ").append(space.getSpaceName())
                    .append(" (").append(space.getType().toString().replace("_", " ")).append(")\n");
        }

        showAlert(Alert.AlertType.INFORMATION, "Proceder al Pago",
                "Detalles de la reserva:\n\n" + reservationDetails.toString()
                + "\nImplementar navegación a ventana de pago...");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPayment(ActionEvent event) {
        goToPay();
    }

    @FXML
    private void backUserView(ActionEvent event) {
        backUserView();
    }
}
