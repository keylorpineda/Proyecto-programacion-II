package Controllers;

import Models.Room;
import Models.Space;
import Utilities.FlowController;
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
   
    private List<Room> floor1Rooms;
    private List<Room> floor2Rooms;
    private List<Room> floor3Rooms;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        populateFloor(apFloor1, floor1Rooms);
        populateFloor(apFloor2, floor2Rooms);
        populateFloor(apFloor3, floor3Rooms);

        cbStartTime.getItems().addAll("08:00", "08:30", "09:00", "09:30");
        cbEndTime.getItems().addAll("10:00", "10:30", "11:00", "11:30");
    }

    

    private void populateFloor(AnchorPane floorPane, List<Room> rooms) {
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
        FlowController.getInstance().goView("UserViewWindow");
    }
}
