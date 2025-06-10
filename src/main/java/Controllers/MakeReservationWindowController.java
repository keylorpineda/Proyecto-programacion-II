package Controllers;

import Models.Room;
import Models.Space;
import Models.Reservation;
import Models.SpaceType;
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
import java.time.LocalDateTime;
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
    @FXML
    private Button btnAddReservation;
    @FXML
    private ListView<String> lvReservationList;

    private Label lblSelectedSpaces;
    private final RoomService roomService = new RoomService();
    private final ReservationService resService = new ReservationService();
    private final SpaceService spaceService = new SpaceService();

    private List<Room> floor1Rooms = new ArrayList<>();
    private List<Room> floor2Rooms = new ArrayList<>();
    private List<Room> floor3Rooms = new ArrayList<>();
    private final Set<Space> selectedSpaces = new HashSet<>();
    private static final double CELL_SIZE = 70;
    private static ReservationData reservationData;
    private final Map<Space, Integer> selectedSeatsPerSpace = new HashMap<>();
    private List<ReservationData> multipleReservations = new ArrayList<>();
    private final Set<Space> currentSelectedSpaces = new HashSet<>();
    private final Map<Space, Integer> currentSelectedSeatsPerSpace = new HashMap<>();

    public static class MultipleReservationsData {

        private List<ReservationData> reservations;
        private double grandTotal;

        public MultipleReservationsData(List<ReservationData> reservations) {
            this.reservations = new ArrayList<>(reservations);
            this.grandTotal = reservations.stream().mapToDouble(r -> r.totalPrice).sum();
        }

        public List<ReservationData> getReservations() {
            return reservations;
        }

        public double getGrandTotal() {
            return grandTotal;
        }
    }

    private static MultipleReservationsData multipleReservationsData;

    public static MultipleReservationsData getMultipleReservationsData() {
        return multipleReservationsData;
    }

    public static class ReservationData {

        private String id;
        public Set<Space> selectedSpaces;
        public Map<Space, Integer> seatsPerSpace;
        public LocalDate date;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public double totalPrice;

        public ReservationData(Set<Space> spaces, Map<Space, Integer> seatsMap, LocalDate date,
                LocalDateTime start, LocalDateTime end, double price) {
            this.id = UUID.randomUUID().toString();
            this.selectedSpaces = new HashSet<>(spaces);
            this.seatsPerSpace = new HashMap<>(seatsMap);
            this.date = date;
            this.startTime = start;
            this.endTime = end;
            this.totalPrice = price;
        }

        public Set<Space> getSelectedSpaces() {
            return selectedSpaces;
        }

        public Map<Space, Integer> getSeatsPerSpace() {
            return seatsPerSpace;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getId() {
            return id;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public double getTotalPrice() {
            return totalPrice;
        }
    }

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

    @FXML
    private void addReservationToList() {
        if (currentSelectedSpaces.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Seleccione al menos un espacio para agregar a la lista.");
            return;
        }

        LocalDate selectedDate = dpDateOption.getValue();
        String startStr = cbStartTime.getValue();
        String endStr = cbEndTime.getValue();

        if (selectedDate == null || startStr == null || endStr == null) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Seleccione fecha y horarios válidos.");
            return;
        }

        LocalTime startTime = LocalTime.parse(startStr);
        LocalTime endTime = LocalTime.parse(endStr);

        if (!startTime.isBefore(endTime)) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "La hora de inicio debe ser antes de la hora final.");
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(selectedDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(selectedDate, endTime);

        if (hasConflictWithExistingReservations(currentSelectedSpaces, startDateTime, endDateTime)) {
            showAlert(Alert.AlertType.WARNING, "Conflicto",
                    "Ya tienes una reservación para alguno de estos espacios en el horario seleccionado.");
            return;
        }

        for (Space space : currentSelectedSpaces) {
            int requestedSeats = currentSelectedSeatsPerSpace.getOrDefault(space, 1);
            try {
                int alreadyReserved = resService.getReservedSeats(space.getId(), startDateTime, endDateTime);
                if (alreadyReserved + requestedSeats > space.getCapacity()) {
                    showAlert(Alert.AlertType.ERROR, "Error de Capacidad",
                            String.format("El espacio '%s' no tiene suficiente capacidad disponible.\n"
                                    + "Solicitados: %d, Ya reservados: %d, Capacidad total: %d",
                                    space.getSpaceName(), requestedSeats, alreadyReserved, space.getCapacity()));
                    return;
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al verificar disponibilidad: " + e.getMessage());
                return;
            }
        }

        double totalPrice = calculateCurrentTotalPrice();

        Set<Space> spacesToSave = new HashSet<>(currentSelectedSpaces);
        Map<Space, Integer> seatsToSave = new HashMap<>(currentSelectedSeatsPerSpace);

        ReservationData newReservation = new ReservationData(
                spacesToSave,
                seatsToSave,
                selectedDate,
                startDateTime,
                endDateTime,
                totalPrice
        );

        multipleReservations.add(newReservation);
        updateReservationsList();

        clearCurrentSelection();

        reloadMatrix();

        showAlert(Alert.AlertType.INFORMATION, "Éxito",
                String.format("Reservación agregada a la lista.\nEspacios: %d, Total asientos: %d, Precio: $%.2f",
                        spacesToSave.size(),
                        seatsToSave.values().stream().mapToInt(Integer::intValue).sum(),
                        totalPrice));
    }

    private boolean hasConflictWithExistingReservations(Set<Space> spaces, LocalDateTime start, LocalDateTime end) {
        for (ReservationData existing : multipleReservations) {

            if (timesOverlap(start, end, existing.startTime, existing.endTime)) {

                for (Space space : spaces) {
                    if (existing.selectedSpaces.contains(space)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean timesOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private void updateReservationsList() {
        if (lvReservationList != null) {
            lvReservationList.getItems().clear();
            for (ReservationData reservation : multipleReservations) {

                int totalSpaces = reservation.selectedSpaces.size();

                int totalSeats = reservation.seatsPerSpace.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();

                String description = String.format("%s | %s - %s | %d espacios | %d asientos | $%.2f",
                        reservation.date.toString(),
                        reservation.startTime.toLocalTime().toString(),
                        reservation.endTime.toLocalTime().toString(),
                        totalSpaces,
                        totalSeats,
                        reservation.totalPrice);
                lvReservationList.getItems().add(description);
            }
            updateTotalPrice();
            updatePayButtonState();
        }
    }

    private void clearCurrentSelection() {

        currentSelectedSpaces.clear();
        currentSelectedSeatsPerSpace.clear();

        Set<Space> spacesToRemove = new HashSet<>();
        for (Space space : selectedSpaces) {
            if (!isSpaceInReservationsList(space)) {
                spacesToRemove.add(space);
            }
        }

        selectedSpaces.removeAll(spacesToRemove);
        for (Space space : spacesToRemove) {
            selectedSeatsPerSpace.remove(space);
        }

        updateSelectedSpacesLabel();
        updateAddReservationButtonState();
        updatePayButtonState();
    }

    public static ReservationData getReservationData() {
        return reservationData;
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

                    updateSelectedSpacesLabel();
                    reloadMatrix();
                }
            });
        }
    }

    private void reloadMatrix() {
        try {

            loadRooms();

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

            LocalDateTime startDateTime = LocalDateTime.of(selectedDate, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(selectedDate, endTime);

            Map<Long, Integer> spaceOccupancy = getSpaceOccupancy(currentRoom.getId(), startDateTime, endDateTime);

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
            }

            createMatrix(currentPane, currentRoom, spaceOccupancy, blockedSpaces);

            Platform.runLater(() -> {
                currentPane.autosize();
                if (currentPane.getScene() != null) {
                    currentPane.getScene().getRoot().autosize();
                }
            });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar la disponibilidad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<Long, Integer> getSpaceOccupancy(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<Long, Integer> occupancy = new HashMap<>();

        try {

            List<Reservation> reservations = resService.findByRoomAndDateAndTime(
                    roomId, startTime.toLocalDate(), startTime.toLocalTime(), endTime.toLocalTime()
            );

            for (Reservation reservation : reservations) {

                LocalDateTime resStart = reservation.getStartTime();
                LocalDateTime resEnd = reservation.getEndTime();

                if (timesOverlap(startTime, endTime, resStart, resEnd)) {
                    Long spaceId = reservation.getSpace().getId();
                    int reservedSeats = reservation.getSeatCount();
                    occupancy.put(spaceId, occupancy.getOrDefault(spaceId, 0) + reservedSeats);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return occupancy;
    }

    public void refreshRooms() {
        try {

            floor1Rooms.clear();
            floor2Rooms.clear();
            floor3Rooms.clear();

            loadRooms();

            Platform.runLater(() -> {
                reloadMatrix();

            });

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void forceRefreshAfterPayment() {
        Platform.runLater(() -> {
            try {

                clearReservationsListAfterPayment();

                Platform.runLater(() -> {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    refreshRooms();
                });

            } catch (Exception e) {

                e.printStackTrace();
            }
        });
    }

    private void addMessageToPane(GridPane pane, String message, String color) {
        Label lbl = new Label(message);
        lbl.setFont(Font.font("Segoe UI", 16));
        lbl.setTextFill(Color.web(color));
        lbl.setWrapText(true);
        pane.add(lbl, 0, 0);
        GridPane.setColumnSpan(lbl, Integer.MAX_VALUE);
    }

    private void createMatrix(GridPane pane, Room room, Map<Long, Integer> spaceOccupancy, Set<Long> blockedSpaces) {
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

                    if (space.getType() == SpaceType.PASILLO) {
                        Region pasillo = new Region();
                        pasillo.setPrefSize(CELL_SIZE * space.getWidth(), CELL_SIZE * space.getHeight());
                        pasillo.setStyle("-fx-background-color: #a1887f; -fx-border-color: #8d6e63; -fx-border-width: 1;");
                        pane.add(pasillo, col, startRow + row, space.getWidth(), space.getHeight());
                    } else {
                        Button spaceButton = createSpaceButton(space, spaceOccupancy, blockedSpaces);
                        pane.add(spaceButton, col, startRow + row, space.getWidth(), space.getHeight());
                    }
                } else {
                    Region emptyRegion = new Region();
                    emptyRegion.setPrefSize(CELL_SIZE, CELL_SIZE);
                    emptyRegion.setStyle("-fx-background-color: transparent;");
                    pane.add(emptyRegion, col, startRow + row);
                }
            }
        }

        updateSelectedSpacesLabel();
        updateAddReservationButtonState();
        updatePayButtonState();
    }

    private Button createSpaceButton(Space space, Map<Long, Integer> spaceOccupancy, Set<Long> blockedSpaces) {
        if (space.getType() == SpaceType.PASILLO) {
            return null;
        }

        Button spaceButton = new Button(space.getSpaceName());
        spaceButton.setPrefSize(CELL_SIZE * space.getWidth() + 10, CELL_SIZE * space.getHeight() + 10);
        spaceButton.setFont(Font.font("Segoe UI", Math.max(12, CELL_SIZE / 6)));
        spaceButton.setWrapText(true);

        String baseColor = getSpaceColor(space);

        if (blockedSpaces.contains(space.getId())) {
            spaceButton.setStyle("-fx-background-color: #616161; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
            spaceButton.setDisable(true);
            spaceButton.setText(spaceButton.getText() + "\n(Bloqueado)");
            return spaceButton;
        }

        int occupiedSeats = spaceOccupancy.getOrDefault(space.getId(), 0);
        int availableSeats = space.getCapacity() - occupiedSeats;

        if (availableSeats <= 0) {
            spaceButton.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
            spaceButton.setDisable(true);
            spaceButton.setText(spaceButton.getText() + "\n(Ocupado)");
            return spaceButton;
        } else if (occupiedSeats > 0) {
            spaceButton.setText(space.getSpaceName() + "\n(" + availableSeats + " disponibles)");
        }

        boolean isCurrentlySelected = currentSelectedSpaces.contains(space);
        boolean isInReservations = isSpaceInReservationsList(space);
        boolean isSelected = isCurrentlySelected || isInReservations;

        if (isInReservations) {

            spaceButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: not-allowed; "
                    + "-fx-border-color: #f57c00; -fx-border-width: 3; "
                    + "-fx-effect: dropshadow(gaussian, rgba(255, 152, 0, 0.6), 10, 0, 0, 0);");
            spaceButton.setText(spaceButton.getText() + "\n(En Lista)");
        } else {
            updateSpaceButtonStyle(spaceButton, isSelected, baseColor);
        }

        spaceButton.setOnAction(ev -> {
            ev.consume();
            if (!isInReservations) {
                handleSpaceSelection(space, spaceButton, baseColor);
            } else {
                showAlert(Alert.AlertType.WARNING, "Espacio en uso",
                        "Este espacio ya está incluido en una reservación de la lista. "
                        + "Para modificarlo, elimine primero la reservación de la lista.");
            }
        });

        if (!isInReservations) {
            spaceButton.setOnMouseEntered(e -> {
                if (!spaceButton.isDisabled()) {
                    spaceButton.setScaleX(1.05);
                    spaceButton.setScaleY(1.05);
                }
            });

            spaceButton.setOnMouseExited(e -> {
                spaceButton.setScaleX(1.0);
                spaceButton.setScaleY(1.0);
            });
        }

        setupSpaceTooltip(spaceButton, space, occupiedSeats, availableSeats);
        return spaceButton;
    }

    private void handleSpaceSelection(Space space, Button spaceButton, String baseColor) {

        if (isSpaceInReservationsList(space)) {
            showAlert(Alert.AlertType.WARNING, "Espacio en uso",
                    "Este espacio ya está incluido en una reservación de la lista. "
                    + "Para modificarlo, elimine primero la reservación de la lista.");
            return;
        }

        if (currentSelectedSpaces.contains(space)) {

            currentSelectedSpaces.remove(space);
            currentSelectedSeatsPerSpace.remove(space);

            selectedSpaces.remove(space);
            selectedSeatsPerSpace.remove(space);

            updateSpaceButtonStyle(spaceButton, false, baseColor);

            showAlert(Alert.AlertType.INFORMATION, "Espacio deseleccionado",
                    "Espacio '" + space.getSpaceName() + "' ha sido deseleccionado.");
        } else {
            // Seleccionar espacio
            currentSelectedSpaces.add(space);
            selectedSpaces.add(space);

            currentSelectedSeatsPerSpace.put(space, 1);
            selectedSeatsPerSpace.put(space, 1);

            updateSpaceButtonStyle(spaceButton, true, baseColor);

            if (space.getCapacity() > 1) {
                boolean confirmed = showCapacitySelector(space);
                if (!confirmed) {

                    currentSelectedSpaces.remove(space);
                    currentSelectedSeatsPerSpace.remove(space);
                    selectedSpaces.remove(space);
                    selectedSeatsPerSpace.remove(space);
                    updateSpaceButtonStyle(spaceButton, false, baseColor);
                } else {

                    int selectedSeats = currentSelectedSeatsPerSpace.getOrDefault(space, 1);
                    selectedSeatsPerSpace.put(space, selectedSeats);
                    showAlert(Alert.AlertType.INFORMATION, "Espacio seleccionado",
                            "Espacio '" + space.getSpaceName() + "' seleccionado con " + selectedSeats + " asiento(s).");
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Espacio seleccionado",
                        "Espacio '" + space.getSpaceName() + "' seleccionado.");
            }
        }

        updateSelectedSpacesLabel();
        updateAddReservationButtonState();
    }

    private double calculateCurrentTotalPrice() {
        if (cbStartTime.getValue() == null || cbEndTime.getValue() == null) {
            return 0.0;
        }

        LocalTime startTime = LocalTime.parse(cbStartTime.getValue());
        LocalTime endTime = LocalTime.parse(cbEndTime.getValue());

        double totalHours = java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        double totalPrice = 0.0;

        for (Space space : currentSelectedSpaces) {
            double pricePerHour = getSpacePriceValue(space.getType());
            int seatsRequested = currentSelectedSeatsPerSpace.getOrDefault(space, 1);
            totalPrice += pricePerHour * totalHours * seatsRequested;
        }

        return totalPrice;
    }

    private void updateTotalPrice() {
        double grandTotal = multipleReservations.stream()
                .mapToDouble(r -> r.totalPrice)
                .sum();

        if (lblSelectedSpaces != null) {
            lblSelectedSpaces.setText(String.format("Total de reservaciones: %d | Gran Total: $%.2f",
                    multipleReservations.size(), grandTotal));
        }
    }

    private void updateSpaceButtonStyle(Button button, boolean isSelected, String baseColor) {
        String buttonColor = isSelected ? "#2e7d32" : baseColor;
        String textColor = isSelected ? "white" : "#222";
        String borderColor = isSelected ? "#1b5e20" : "#999";
        String borderWidth = isSelected ? "3" : "1";

        button.setStyle("-fx-background-color: " + buttonColor
                + "; -fx-text-fill: " + textColor
                + "; -fx-border-radius: 8; -fx-background-radius: 8;"
                + "; -fx-border-color: " + borderColor
                + "; -fx-border-width: " + borderWidth
                + "; -fx-font-weight: " + (isSelected ? "bold" : "normal")
                + "; -fx-cursor: hand;"
                + "; -fx-effect: " + (isSelected ? "dropshadow(gaussian, rgba(46, 125, 50, 0.6), 10, 0, 0, 0)" : "none") + ";");
    }

    private HBox createCapacitySelector(Space space, String baseColor) {
        HBox capacityBox = new HBox();
        capacityBox.setAlignment(javafx.geometry.Pos.CENTER);
        capacityBox.setSpacing(3);
        capacityBox.setPrefHeight(Math.max(20, (CELL_SIZE * space.getHeight()) * 0.3));

        double buttonSize = Math.min(18, (CELL_SIZE * Math.min(space.getWidth(), space.getHeight())) / 4);
        double fontSize = Math.max(8, buttonSize * 0.6);

        Button minusBtn = new Button("-");
        minusBtn.setPrefSize(buttonSize, buttonSize);
        minusBtn.setFont(Font.font("Segoe UI", fontSize));
        minusBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3; -fx-padding: 0;");

        Label quantityLabel = new Label("1");
        quantityLabel.setFont(Font.font("Segoe UI", fontSize));
        quantityLabel.setPrefWidth(Math.max(20, buttonSize * 1.2));
        quantityLabel.setAlignment(javafx.geometry.Pos.CENTER);
        quantityLabel.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 2; -fx-padding: 1;");

        Button plusBtn = new Button("+");
        plusBtn.setPrefSize(buttonSize, buttonSize);
        plusBtn.setFont(Font.font("Segoe UI", fontSize));
        plusBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3; -fx-padding: 0;");

        minusBtn.setOnAction(e -> {
            e.consume();
            int currentSeats = selectedSeatsPerSpace.getOrDefault(space, 1);
            if (currentSeats > 1) {
                currentSeats--;
                selectedSeatsPerSpace.put(space, currentSeats);
                quantityLabel.setText(String.valueOf(currentSeats));
                updateSelectedSpacesLabel();
            }
        });

        plusBtn.setOnAction(e -> {
            e.consume();
            int currentSeats = selectedSeatsPerSpace.getOrDefault(space, 1);

            if (canIncreaseSeats(space, currentSeats)) {
                currentSeats++;
                selectedSeatsPerSpace.put(space, currentSeats);
                quantityLabel.setText(String.valueOf(currentSeats));
                updateSelectedSpacesLabel();
            } else {
                showAlert(Alert.AlertType.WARNING, "Capacidad máxima",
                        "No hay suficientes asientos disponibles en este espacio para el horario seleccionado.");
            }
        });

        capacityBox.getChildren().addAll(minusBtn, quantityLabel, plusBtn);

        capacityBox.setVisible(false);
        capacityBox.setManaged(false);

        return capacityBox;
    }

    private boolean canIncreaseSeats(Space space, int currentRequestedSeats) {
        try {
            LocalDate selectedDate = dpDateOption.getValue();
            String startStr = cbStartTime.getValue();
            String endStr = cbEndTime.getValue();

            if (selectedDate == null || startStr == null || endStr == null) {
                return false;
            }

            LocalDateTime startTime = LocalDateTime.of(selectedDate, LocalTime.parse(startStr));
            LocalDateTime endTime = LocalDateTime.of(selectedDate, LocalTime.parse(endStr));

            int alreadyReserved = resService.getReservedSeats(space.getId(), startTime, endTime);

            return (alreadyReserved + currentRequestedSeats + 1) <= space.getCapacity();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void toggleSpaceSelection(Space space, Button spaceButton, String baseColor, VBox container) {

        handleSpaceSelection(space, spaceButton, baseColor);
    }

    private int getMaxAvailableSeats(Space space) {
        try {
            LocalDate selectedDate = dpDateOption.getValue();
            String startStr = cbStartTime.getValue();
            String endStr = cbEndTime.getValue();

            if (selectedDate == null || startStr == null || endStr == null) {
                return space.getCapacity();
            }

            LocalDateTime startTime = LocalDateTime.of(selectedDate, LocalTime.parse(startStr));
            LocalDateTime endTime = LocalDateTime.of(selectedDate, LocalTime.parse(endStr));

            int alreadyReserved = resService.getReservedSeats(space.getId(), startTime, endTime);
            return space.getCapacity() - alreadyReserved;

        } catch (Exception e) {
            e.printStackTrace();
            return space.getCapacity();
        }
    }

    private boolean showCapacitySelector(Space space) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Cantidad de Asientos");
        dialog.setHeaderText("Espacio: " + space.getSpaceName()
                + "\nCapacidad máxima: " + space.getCapacity() + " personas"
                + "\nSeleccione cuántos asientos desea reservar:");

        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new Insets(20));

        Label label = new Label("Cantidad de asientos a reservar:");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox selectorBox = new HBox(15);
        selectorBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.setPrefSize(40, 40);
        minusBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 16px; "
                + "-fx-font-weight: bold; -fx-cursor: hand;");

        Label quantityLabel = new Label("1");
        quantityLabel.setPrefWidth(60);
        quantityLabel.setAlignment(javafx.geometry.Pos.CENTER);
        quantityLabel.setStyle("-fx-background-color: white; -fx-border-color: #ccc; "
                + "-fx-border-width: 2; -fx-padding: 10; -fx-font-size: 16px; "
                + "-fx-font-weight: bold; -fx-border-radius: 5;");

        Button plusBtn = new Button("+");
        plusBtn.setPrefSize(40, 40);
        plusBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 16px; "
                + "-fx-font-weight: bold; -fx-cursor: hand;");

        int currentValue = currentSelectedSeatsPerSpace.getOrDefault(space, 1);
        quantityLabel.setText(String.valueOf(currentValue));

        minusBtn.setOnAction(e -> {
            int current = Integer.parseInt(quantityLabel.getText());
            if (current > 1) {
                current--;
                quantityLabel.setText(String.valueOf(current));
            }
        });

        plusBtn.setOnAction(e -> {
            int current = Integer.parseInt(quantityLabel.getText());
            if (canIncreaseSeats(space, current - 1)) {
                current++;
                quantityLabel.setText(String.valueOf(current));
            } else {
                showAlert(Alert.AlertType.WARNING, "Capacidad máxima",
                        "No hay suficientes asientos disponibles en este espacio para el horario seleccionado.\n"
                        + "Máximo disponible: " + getMaxAvailableSeats(space));
            }
        });

        selectorBox.getChildren().addAll(minusBtn, quantityLabel, plusBtn);
        content.getChildren().addAll(label, selectorBox);

        dialog.getDialogPane().setContent(content);

        ButtonType okButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return Integer.parseInt(quantityLabel.getText());
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();

        if (result.isPresent()) {
            int selectedQuantity = result.get();

            currentSelectedSeatsPerSpace.put(space, selectedQuantity);
            selectedSeatsPerSpace.put(space, selectedQuantity);
            updateSelectedSpacesLabel();
            return true;
        } else {
            return false;
        }
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

    private String getSpacePrice(SpaceType type) {
        switch (type) {
            case ESCRITORIO:
                return "$15/hora";
            case SALA_REUNIONES:
                return "$50/hora";
            case AREA_COMUN:
                return "$25/hora";
            case PASILLO:
                return "Gratis";
            default:
                return "$10/hora";
        }
    }

    private double getSpacePriceValue(SpaceType type) {
        switch (type) {
            case ESCRITORIO:
                return 15.0;
            case SALA_REUNIONES:
                return 50.0;
            case AREA_COMUN:
                return 25.0;
            case PASILLO:
                return 0.0;
            default:
                return 10.0;
        }
    }

    private void setupSpaceTooltip(Button btn, Space space, int occupiedSeats, int availableSeats) {
        if (space.getType() == SpaceType.PASILLO) {
            return;
        }

        Tooltip tooltip = new Tooltip();
        tooltip.setText(buildSpaceInfo(space, occupiedSeats, availableSeats));
        tooltip.setFont(Font.font("Segoe UI", 12));
        tooltip.setShowDelay(javafx.util.Duration.millis(500));
        Tooltip.install(btn, tooltip);
    }

    private String buildSpaceInfo(Space space, int occupiedSeats, int availableSeats) {
        StringBuilder info = new StringBuilder();
        info.append("Nombre: ").append(space.getSpaceName()).append("\n");
        info.append("Tipo: ").append(space.getType().toString().replace("_", " ")).append("\n");
        info.append("Tamaño: ").append(space.getWidth()).append("x").append(space.getHeight()).append("\n");
        info.append("Posición: (").append(space.getStartRow()).append(",").append(space.getStartCol()).append(")").append("\n");

        String precio = getSpacePrice(space.getType());
        info.append("Precio: ").append(precio);

        if (space.getCapacity() > 0) {
            info.append("\nCapacidad total: ").append(space.getCapacity()).append(" personas");
            info.append("\nOcupados: ").append(occupiedSeats).append(" asientos");
            info.append("\nDisponibles: ").append(availableSeats).append(" asientos");
        }

        return info.toString();
    }

    private double calculateTotalPrice() {
        LocalTime startTime = LocalTime.parse(cbStartTime.getValue());
        LocalTime endTime = LocalTime.parse(cbEndTime.getValue());

        double totalHours = java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        double totalPrice = 0.0;

        for (Space space : selectedSpaces) {
            double pricePerHour = getSpacePriceValue(space.getType());
            int seatsRequested = selectedSeatsPerSpace.getOrDefault(space, 1);

            totalPrice += pricePerHour * totalHours * seatsRequested;
        }

        return totalPrice;
    }

    private void updateSelectedSpacesLabel() {
        if (lblSelectedSpaces != null) {
            double currentPrice = calculateCurrentTotalPrice();
            int currentSeats = currentSelectedSeatsPerSpace.values().stream().mapToInt(Integer::intValue).sum();
            double grandTotal = multipleReservations.stream().mapToDouble(r -> r.totalPrice).sum();

            String currentInfo = "";
            if (!currentSelectedSpaces.isEmpty()) {
                currentInfo = String.format("Selección actual: %d espacios, %d asientos ($%.2f)",
                        currentSelectedSpaces.size(), currentSeats, currentPrice);
            }

            String reservationInfo = String.format("Reservaciones en lista: %d ($%.2f)",
                    multipleReservations.size(), grandTotal);

            String fullText = currentInfo.isEmpty() ? reservationInfo : currentInfo + " | " + reservationInfo;
            lblSelectedSpaces.setText(fullText);
        }
    }

    private void updateAddReservationButtonState() {
        if (btnAddReservation != null) {
            btnAddReservation.setDisable(currentSelectedSpaces.isEmpty());
        }
    }

    private void updatePayButtonState() {
        if (btnGoToPay != null) {
            btnGoToPay.setDisable(multipleReservations.isEmpty());
        }
    }

    @FXML
    private void removeSelectedReservation() {
        int selectedIndex = lvReservationList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < multipleReservations.size()) {

            ReservationData removedReservation = multipleReservations.get(selectedIndex);

            multipleReservations.remove(selectedIndex);

            updateReservationsList();

            reloadMatrix();

            showAlert(Alert.AlertType.INFORMATION, "Eliminado",
                    "Reservación eliminada de la lista. Los espacios están ahora disponibles para nueva selección.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Selección", "Seleccione una reservación para eliminar.");
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

    @FXML
    private void goToPay() {
        if (multipleReservations.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Agregue al menos una reservación a la lista.");
            return;
        }

        MultipleReservationsData allReservations = new MultipleReservationsData(multipleReservations);

        reservationData = null;
        multipleReservationsData = allReservations;

        try {
            FlowController.getInstance().goView("PaymentWindow");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de pago: " + e.getMessage());
        }
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

    public void clearReservationsListAfterPayment() {
        multipleReservations.clear();
        currentSelectedSpaces.clear();
        currentSelectedSeatsPerSpace.clear();
        selectedSpaces.clear();
        selectedSeatsPerSpace.clear();

        if (lvReservationList != null) {
            Platform.runLater(() -> {
                lvReservationList.getItems().clear();
                updateSelectedSpacesLabel();
                updateAddReservationButtonState();
                updatePayButtonState();

                loadRooms();
                reloadMatrix();
            });
        }

        multipleReservationsData = null;
        reservationData = null;
    }

    private boolean isSpaceInReservationsList(Space space) {
        return multipleReservations.stream()
                .anyMatch(reservation -> reservation.selectedSpaces.contains(space));
    }

    public static void clearReservationsAfterPayment() {
        multipleReservationsData = null;
        reservationData = null;
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
