package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;

import Models.Reservation;
import Services.ReservationManager;
import Utilities.FlowController;
import com.mycompany.proyectoprogramacionii.App;
import java.io.IOException;

public class UserViewWindowController {

    @FXML
    private Label lblUserName;
    @FXML
    private ToggleButton tgbShowReservation;

    @FXML
    private TableView<Reservation> tbvReservationTable;

    @FXML
    private TableColumn<Reservation, String> idColumn;

    @FXML
    private TableColumn<Reservation, String> placeColumn;

    @FXML
    private TableColumn<Reservation, String> spaceColumn;

    @FXML
    private TableColumn<Reservation, String> dateColumn;

    @FXML
    private TableColumn<Reservation, String> startColumn;

    @FXML
    private TableColumn<Reservation, String> endColumn;

    private ReservationManager reservationManager;

    private ObservableList<Reservation> reservationList;
    @FXML
    private Button btnHacerReservacion;

    public void initialize() {
        reservationManager = ReservationManager.getInstance();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("place"));
        spaceColumn.setCellValueFactory(new PropertyValueFactory<>("spaceName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        reservationList = FXCollections.observableArrayList();
        tbvReservationTable.setItems(reservationList);
    }

    public void setUserName(String userName) {
        lblUserName.setText(userName);
    }
    @FXML
    private void clickChange(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("MakeReservationWindow");
    }
    @FXML
    private void showReservationTable(ActionEvent event) {
        boolean isSelected = tgbShowReservation.isSelected();
        tbvReservationTable.setVisible(isSelected);

        if (isSelected) {
            tgbShowReservation.setText("Ocultar Reservaciones");
            loadUserReservations();
        } else {
            tgbShowReservation.setText("Mostrar Reservaciones");
        }
    }

    private void loadUserReservations() {
        String username = lblUserName.getText();

        reservationList.clear();
        reservationList.addAll(reservationManager.getReservationsByUser(username));
        tbvReservationTable.refresh();
    }

   
}
