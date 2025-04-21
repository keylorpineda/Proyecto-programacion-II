package Controllers;

import Models.Room;
import Models.Space;
import Models.Desk;
import Models.Table;
import Models.Chair;
import com.mycompany.proyectoprogramacionii.App;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

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
    private Tab tabFloor1;
    @FXML
    private AnchorPane apFloor1;
    @FXML
    private Tab tabFloor2;
    @FXML
    private AnchorPane apFloor2;
    @FXML
    private Tab tabFloor3;
    @FXML
    private AnchorPane apFloor3;

    @FXML
    private Button btnBack;
    // Listas de Rooms para cada piso
    private List<Room> floor1Rooms;
    private List<Room> floor2Rooms;
    private List<Room> floor3Rooms;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeData();

        populateFloor(apFloor1, floor1Rooms);
        populateFloor(apFloor2, floor2Rooms);
        populateFloor(apFloor3, floor3Rooms);

        cbStartTime.getItems().addAll("08:00", "08:30", "09:00", "09:30");
        cbEndTime.getItems().addAll("10:00", "10:30", "11:00", "11:30");
    }

    
    private void initializeData() {
        
        Space deskA1 = new Desk("DskA1", "Desk A1", 4, false);
        Space tableA1 = new Table("TblA1", "Table A1", 6, false);
        Space chairA1 = new Chair("ChrA1", "Chair A1", 1, false);
        Room roomA = new Room("Room A", 50,  "A", "Sala", new ArrayList<>(Arrays.asList(deskA1, tableA1, chairA1)));

        // Room B
        Space deskB1 = new Desk("DskB1", "Desk B1", 4, false);
        Space chairB1 = new Chair("ChrB1", "Chair B1", 1, true);
        Room roomB = new Room("Room B", 40, "B", "Sala", new ArrayList<>(Arrays.asList(deskB1, chairB1)));

        // Room C
        Space tableC1 = new Table("TblC1", "Table C1", 6, false);
        Space chairC1 = new Chair("ChrC1", "Chair C1", 1, false);
        Space chairC2 = new Chair("ChrC2", "Chair C2", 1, false);
        Room roomC = new Room("Room C", 60, "C", "Sala", new ArrayList<>(Arrays.asList(tableC1, chairC1, chairC2)));

        floor1Rooms = new ArrayList<>(Arrays.asList(roomA, roomB, roomC));

        Space deskD1 = new Desk("DskD1", "Desk D1", 4, false);
        Space tableD1 = new Table("TblD1", "Table D1", 6, false);
        Room roomD = new Room("Room D", 55, "D", "Sala", new ArrayList<>(Arrays.asList(deskD1, tableD1)));

        Space chairE1 = new Chair("ChrE1", "Chair E1", 1, false);
        Space tableE1 = new Table("TblE1", "Table E1", 6, true);
        Space chairE2 = new Chair("ChrE2", "Chair E2", 1, false);
        Room roomE = new Room("Room E", 45, "E", "Sala", new ArrayList<>(Arrays.asList(chairE1, tableE1, chairE2)));

        floor2Rooms = new ArrayList<>(Arrays.asList(roomD, roomE));

        Space deskF1 = new Desk("DskF1", "Desk F1", 4, false);
        Room roomF = new Room("Room F", 70, "F", "Sala", new ArrayList<>(Arrays.asList(deskF1)));

        // Room G
        Space chairG1 = new Chair("ChrG1", "Chair G1", 1, false);
        Space deskG1 = new Desk("DskG1", "Desk G1", 4, true);
        Room roomG = new Room("Room G", 65, "G", "Sala", new ArrayList<>(Arrays.asList(chairG1, deskG1)));

        floor3Rooms = new ArrayList<>(Arrays.asList(roomF, roomG));
    }
    private void populateFloor(AnchorPane floorPane, List<Room> rooms) {
        // Limpia el contenedor
        floorPane.getChildren().clear();
        double startX = 50;
        double startY = 50;
        double spacingX = 150;

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            Button btnRoom = new Button(room.getRoomName());
            btnRoom.setPrefSize(100, 40);
            btnRoom.setLayoutX(startX + (i * spacingX));
            btnRoom.setLayoutY(startY);
            btnRoom.setUserData(room);
            btnRoom.setOnAction(e -> handleRoomSelection(room));
            floorPane.getChildren().add(btnRoom);
        }
    }

    private void handleRoomSelection(Room room) {
        Tab currentTab = tpTabChooseSpace.getSelectionModel().getSelectedItem();
        AnchorPane floorPane = (AnchorPane) currentTab.getContent();
        
        floorPane.getChildren().removeIf(node -> "gpSpaces".equals(node.getId()));

        GridPane gpSpaces = new GridPane();
        gpSpaces.setId("gpSpaces");
        gpSpaces.setHgap(10);
        gpSpaces.setVgap(10);
        gpSpaces.setLayoutX(50);
        gpSpaces.setLayoutY(150);

        int col = 0;
        int row = 0;
        for (Space s : room.getAvailableSpaces()) {
            Button btnSpace = new Button(s.getSpaceName() + "\n" + s.getReserved()); 
            btnSpace.setPrefSize(100, 50);
            btnSpace.setOnAction(e -> {
                if (!s.getReserved()) {
                    s.reserve();
                    btnSpace.setText(s.getSpaceName() + "\nReservado");
                }
            });
            gpSpaces.add(btnSpace, col, row);
            col++;
            if (col >= 2) { 
                col = 0;
                row++;
            }
        }
        floorPane.getChildren().add(gpSpaces);
    }
    
    @FXML
    private void backUserView(ActionEvent event) throws IOException {
        App.setRoot("UserViewWindow");
    }
}
