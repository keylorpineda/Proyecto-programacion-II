package Controllers;

import Models.Room;
import Models.Space;
import Models.Reservation;
import Services.RoomService;
import Services.SpaceService;
import Services.ReservationService;
import Utilities.FlowController;
import Utilities.TimeSlots; // Clase utilitaria para las horas
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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
    private AnchorPane apFloor1;
    @FXML
    private AnchorPane apFloor2;
    @FXML
    private AnchorPane apFloor3;
    @FXML
    private Button btnBack;
    @FXML
    private Button btnGoToPay;

    private final RoomService roomService = new RoomService();
    private final ReservationService resService = new ReservationService();

    private List<Room> floor1Rooms, floor2Rooms, floor3Rooms;

    private final Set<Space> selectedSpaces = new HashSet<>();

    private static final double CELL_SIZE = 70;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cbStartTime.getItems().setAll(TimeSlots.getDefaultTimeSlots());
        cbEndTime.getItems().setAll(TimeSlots.getDefaultTimeSlots());

        floor1Rooms = roomService.findByFloor(1);
        floor2Rooms = roomService.findByFloor(2);
        floor3Rooms = roomService.findByFloor(3);

        dpDateOption.valueProperty().addListener((obs, oldVal, newVal) -> reloadMatrix());
        cbStartTime.valueProperty().addListener((obs, oldVal, newVal) -> reloadMatrix());
        cbEndTime.valueProperty().addListener((obs, oldVal, newVal) -> reloadMatrix());
        tpTabChooseSpace.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> reloadMatrix());

        btnBack.setOnAction(e -> backUserView());
        btnGoToPay.setOnAction(e -> goToPay());

        reloadMatrix();
    }

    private void reloadMatrix() {
        AnchorPane paneActual = getCurrentAnchorPane();
        if (paneActual == null) {
            return;
        }

        paneActual.getChildren().clear();
        selectedSpaces.clear();

        LocalDate selectedDate = dpDateOption.getValue();
        String startStr = cbStartTime.getValue();
        String endStr = cbEndTime.getValue();

        if (selectedDate == null || startStr == null || endStr == null) {
            Label lbl = new Label("Seleccione fecha y horas para ver disponibilidad.");
            lbl.setFont(Font.font(18));
            lbl.setTextFill(Color.web("#222"));
            paneActual.getChildren().add(lbl);
            return;
        }

        LocalTime startTime = LocalTime.parse(startStr);
        LocalTime endTime = LocalTime.parse(endStr);
        if (!startTime.isBefore(endTime)) {
            Label lbl = new Label("La hora de inicio debe ser antes de la hora final.");
            lbl.setFont(Font.font(16));
            lbl.setTextFill(Color.web("#e57373"));
            paneActual.getChildren().add(lbl);
            return;
        }

        Room currentRoom = getCurrentRoom();
        if (currentRoom == null) {
            return;
        }

        List<Reservation> reservas = resService.findByRoomAndDateAndTime(
                currentRoom.getId(), selectedDate, startStr, endStr
        );
        Set<Long> spacesOcupados = reservas.stream()
                .map(r -> r.getSpace().getId())
                .collect(Collectors.toSet());


        SpaceService spaceService = new SpaceService();
        Set<Long> spacesBloqueados = spaceService.findBlockedSpaces(
                currentRoom.getId(), selectedDate, startTime, endTime
        ).stream().map(Space::getId).collect(Collectors.toSet());

        GridPane matrix = new GridPane();
        matrix.setLayoutX(10);
        matrix.setLayoutY(20);
        matrix.setHgap(5);
        matrix.setVgap(5);

        int filas = currentRoom.getRows();
        int columnas = currentRoom.getCols();


        for (int row = 0; row < filas; row++) {
            for (int col = 0; col < columnas; col++) {
                final int finalRow = row;
                final int finalCol = col;
                Optional<Space> opt = currentRoom.getSpaces().stream()
                        .filter(s -> s.getStartRow() == finalRow && s.getStartCol() == finalCol)
                        .findFirst();
                if (opt.isPresent()) {
                    Space s = opt.get();
                    Button btn = new Button(s.getSpaceName());
                    btn.setPrefSize(CELL_SIZE * s.getWidth(), CELL_SIZE * s.getHeight());
                    btn.setFont(Font.font("Segoe UI", 12));
                    String color = "#bdbdbd";
                    switch (s.getType()) {
                        case ESCRITORIO:
                            color = "#90caf9";
                            break; // celeste
                        case SALA_REUNIONES:
                            color = "#ffb300";
                            break; // amarillo
                        case AREA_COMUN:
                            color = "#a5d6a7";
                            break; // verde
                        case PASILLO:
                            color = "#a1887f";
                            break; // café claro
                        default:
                            break;
                    }
                    final String auxColor = color;
                    if (spacesOcupados.contains(s.getId())) {
                        btn.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; -fx-font-weight:bold;");
                        btn.setDisable(true);
                        btn.setText(btn.getText() + "\n(Ocupado)");
                    } 
                    else if (spacesBloqueados.contains(s.getId())) {
                        btn.setStyle("-fx-background-color: #616161; -fx-text-fill: white; -fx-font-weight:bold;");
                        btn.setDisable(true);
                        btn.setText(btn.getText() + "\n(Bloqueado)");
                    } 
                    else {
                        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #222; -fx-border-radius: 6; -fx-background-radius: 6;");
                        btn.setOnAction(ev -> {
                            if (selectedSpaces.contains(s)) {
                                selectedSpaces.remove(s);
                          
                                btn.setStyle("-fx-background-color: " + auxColor + "; -fx-text-fill: #222; -fx-border-radius: 6; -fx-background-radius: 6;");
                            } else {
                                selectedSpaces.add(s);
                                btn.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6;");
                            }
                        });
                    }
                    matrix.add(btn, col, row, s.getWidth(), s.getHeight());
                } else {
                    Region r = new Region();
                    r.setPrefSize(CELL_SIZE, CELL_SIZE);
                    r.setStyle("-fx-background-color: transparent;");
                    matrix.add(r, col, row);
                }
            }
        }
        paneActual.getChildren().add(matrix);

        btnGoToPay.setLayoutX(10);
        btnGoToPay.setLayoutY(40 + filas * (CELL_SIZE + 5));
        if (!paneActual.getChildren().contains(btnGoToPay)) {
            paneActual.getChildren().add(btnGoToPay);
        }
    }

    private AnchorPane getCurrentAnchorPane() {
        Tab selectedTab = tpTabChooseSpace.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            return null;
        }
        if (selectedTab.getText().equals("Piso 1")) {
            return apFloor1;
        }
        if (selectedTab.getText().equals("Piso 2")) {
            return apFloor2;
        }
        if (selectedTab.getText().equals("Piso 3")) {
            return apFloor3;
        }
        return null;
    }

    private Room getCurrentRoom() {
        Tab selectedTab = tpTabChooseSpace.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            return null;
        }
        if (selectedTab.getText().equals("Piso 1") && !floor1Rooms.isEmpty()) {
            return floor1Rooms.get(0);
        }
        if (selectedTab.getText().equals("Piso 2") && !floor2Rooms.isEmpty()) {
            return floor2Rooms.get(0);
        }
        if (selectedTab.getText().equals("Piso 3") && !floor3Rooms.isEmpty()) {
            return floor3Rooms.get(0);
        }
        return null;
    }

    private void backUserView() {
        try {
            FlowController.getInstance().goView("UserViewWindow");
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo volver a la vista de usuario").showAndWait();
        }
    }

    private void goToPay() {
        if (selectedSpaces.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Seleccione al menos un espacio para reservar.").showAndWait();
            return;
        }
        // Aquí guardarías temporalmente la selección y navegarías a la ventana de pago.
        new Alert(Alert.AlertType.INFORMATION,
                "Implementa aquí la lógica para llevar a la ventana de pago.\nEspacios seleccionados: " + selectedSpaces.size()
        ).showAndWait();
    }

    @FXML
    private void goToPayment(ActionEvent event) {

        new Alert(Alert.AlertType.INFORMATION, "Redirigir a la ventana de pago...").showAndWait();
    }

    @FXML
    private void backUserView(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("UserViewWindow");
    }

}
