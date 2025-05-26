package Controllers;

import Models.Room;
import Models.Space;
import Models.Reservation;
import Services.RoomService;
import Services.SpaceService;
import Services.ReservationService;
import Utilities.FlowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MakeReservationWindowController implements Initializable {

    @FXML private DatePicker dpDateOption;
    @FXML private ComboBox<String> cbStartTime;
    @FXML private ComboBox<String> cbEndTime;
    @FXML private TabPane tpTabChooseSpace;
    @FXML private AnchorPane apFloor1;
    @FXML private AnchorPane apFloor2;
    @FXML private AnchorPane apFloor3;
    @FXML private Button btnBack;

    private final RoomService roomService       = new RoomService();
    private final SpaceService spaceService     = new SpaceService();
    private final ReservationService resService = new ReservationService();

    private List<Room> floor1Rooms;
    private List<Room> floor2Rooms;
    private List<Room> floor3Rooms;

    private static final double CELL_SIZE = 50;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        floor1Rooms = roomService.findByFloor(1);
        floor2Rooms = roomService.findByFloor(2);
        floor3Rooms = roomService.findByFloor(3);
        populateFloor(apFloor1, floor1Rooms);
        populateFloor(apFloor2, floor2Rooms);
        populateFloor(apFloor3, floor3Rooms);
        cbStartTime.getItems().addAll("08:00","08:30","09:00","09:30");
        cbEndTime  .getItems().addAll("10:00","10:30","11:00","11:30");
    }

    private void populateFloor(AnchorPane floorPane, List<Room> rooms) {
        floorPane.getChildren().clear();
        double startX = 50, startY = 50, spacingX = 150;
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            Button b = new Button(room.getRoomName());
            b.setPrefSize(100, 40);
            b.setLayoutX(startX + i * spacingX);
            b.setLayoutY(startY);
            b.setOnAction(e -> handleRoomSelection(room));
            floorPane.getChildren().add(b);
        }
    }

    private void handleRoomSelection(Room room) {
        Tab selected = tpTabChooseSpace.getSelectionModel().getSelectedItem();
        AnchorPane pane = (AnchorPane) selected.getContent();
        pane.getChildren().removeIf(n -> "gpSpaces".equals(n.getId()));

        GridPane gp = new GridPane();
        gp.setId("gpSpaces");
        gp.setHgap(5);
        gp.setVgap(5);
        gp.setLayoutX(50);
        gp.setLayoutY(150);

        LocalDate   date  = dpDateOption.getValue();
        LocalDateTime start = LocalDateTime.of(date, LocalTime.parse(cbStartTime.getValue()));
        LocalDateTime end   = LocalDateTime.of(date, LocalTime.parse(cbEndTime  .getValue()));

        for (Space s : room.getAvailableSpaces()) {
            Button btn = new Button(s.getSpaceName());
            btn.setPrefSize(CELL_SIZE * s.getWidth(), CELL_SIZE * s.getHeight());

            btn.setOnAction(evt -> {
                long already = resService.countReservationsForSpace(
                    s.getId(), date, start, end);

                int cap = s.getCapacity();
                int avail = cap - (int) already;

                TextInputDialog dlg = new TextInputDialog("1");
                dlg.setTitle("Reservar “" + s.getSpaceName() + "”");
                dlg.setHeaderText("Capacidad: " + cap + " — Disponibles: " + avail);
                dlg.setContentText("¿Cuántos desea?");
                Optional<String> opt = dlg.showAndWait();
                if (opt.isEmpty()) return;

                int req;
                try { req = Integer.parseInt(opt.get()); }
                catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Número inválido").showAndWait();
                    return;
                }
                if (req > avail) {
                    new Alert(Alert.AlertType.WARNING,
                              "Sólo quedan " + avail).showAndWait();
                    return;
                }

                for (int i = 0; i < req; i++) {
                    Reservation r = new Reservation();
                    r.setSpace(s);
                    r.setDate(date);
                    r.setStartTime(start);
                    r.setEndTime(end);
                    resService.save(r);
                }
                if (req == avail) {
                    s.setAvailable(false);
                    spaceService.update(s);
                    btn.setDisable(true);
                }
                btn.setText(s.getSpaceName() + "\nQuedan: " + (avail - req));
                new Alert(Alert.AlertType.INFORMATION,
                          "Reservados " + req).showAndWait();
            });

            gp.add(btn,
                   s.getStartCol(),
                   s.getStartRow(),
                   s.getWidth(),
                   s.getHeight());
        }
        pane.getChildren().add(gp);
    }

    @FXML
    private void backUserView(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("UserViewWindow");
    }
}
