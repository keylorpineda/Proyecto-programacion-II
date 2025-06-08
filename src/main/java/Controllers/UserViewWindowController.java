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
import Utilities.FlowController;
import com.mycompany.proyectoprogramacionii.App;
import java.io.IOException;
import javafx.scene.control.Alert;
import Utilities.graphicUtilities;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    private ObservableList<Reservation> reservationList;
    @FXML
    private Button btnCerrarSesion;
    @FXML
    private Button btnMakeReservation;

    private graphicUtilities utilities;
    @FXML
    private Button btnConfiguracion;
    @FXML
    private VBox VBoxMenuUsuario;
    @FXML
    private Button btnPerfilUusuario;
    @FXML
    private Button btnEditarUsuario;

    public void initialize() {
        utilities = new graphicUtilities();
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
        tbvReservationTable.refresh();
    }

    @FXML
    private void toggleMenuOpciones(ActionEvent event) {
        VBoxMenuUsuario.setVisible(!VBoxMenuUsuario.isVisible());
    }
    @FXML
    private void clickCerrarSesionUsuario(ActionEvent event) throws IOException {
        utilities.showAlert(Alert.AlertType.INFORMATION, "Sesión cerrada", "Haz cerrado sesión correctamente!!");
        FlowController.getInstance().goView("LoginWindow");
    }
    @FXML
    private void clickVerPerfilUsuario(ActionEvent event) {
    }

    @FXML
    private void clickEditarUsuario(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("EditCustomer");
    }
  

    @FXML
private void clickOpenGame(ActionEvent event) {
    try {
        FlowController.getInstance().goView("SnakeGame");
    } catch (Exception e) {
        utilities.showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir el minijuego.");
        e.printStackTrace();
    }
}

}
