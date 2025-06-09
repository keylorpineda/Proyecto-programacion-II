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
import Services.ReservationService;
import Utilities.FlowController;
import com.mycompany.proyectoprogramacionii.App;
import java.io.IOException;
import javafx.scene.control.Alert;
import Utilities.graphicUtilities;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    private TableColumn<Reservation, Long> idColumn;

    @FXML
    private TableColumn<Reservation, String> spaceColumn;

    @FXML
    private TableColumn<Reservation, LocalDateTime> dateColumn;

    @FXML
    private TableColumn<Reservation, LocalDateTime> startColumn;

    @FXML
    private TableColumn<Reservation, LocalDateTime> endColumn;

    @FXML
    private TableColumn<Reservation, Integer> countColumn;

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

    private ReservationService reservationService;
    private Long currentUserId;

    public void initialize() {
        utilities = new graphicUtilities();
        reservationService = new ReservationService();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        spaceColumn.setCellValueFactory(new PropertyValueFactory<>("spaceName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startTimeAux"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeAux"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("seatCount"));
        reservationList = FXCollections.observableArrayList();
        tbvReservationTable.setItems(reservationList);
        setupContextMenu();
    }

    public void setUserId(Long userId) {
        this.currentUserId = userId;
        System.out.println("Usuario ID establecido: " + userId);
    }

    public void setUserName(String userName) {
        lblUserName.setText(userName);
        loadUserReservations();
    }

    @FXML
    private void clickChange(ActionEvent event) throws IOException {
        FlowController.getInstance().goView("MakeReservationWindow");

        Object controller = FlowController.getInstance().getController("MakeReservationWindow");
        if (controller instanceof MakeReservationWindowController) {
            ((MakeReservationWindowController) controller).refreshRooms();
        }
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem cancelItem = new MenuItem("Cancelar Reservación");
        cancelItem.setOnAction(e -> {
            Reservation selectedReservation = tbvReservationTable.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                cancelReservation(selectedReservation);
            }
        });

        contextMenu.getItems().add(cancelItem);

        tbvReservationTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Reservation selectedItem = tbvReservationTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    contextMenu.show(tbvReservationTable, event.getScreenX(), event.getScreenY());
                }
            } else {
                contextMenu.hide();
            }
        });
    }

    private void cancelReservation(Reservation reservation) {

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Cancelación");
        confirmAlert.setHeaderText("¿Está seguro que desea cancelar esta reservación?");
        confirmAlert.setContentText(
                "Lugar: " + reservation.getPlace() + "\n"
                + "Espacio: " + reservation.getSpaceName() + "\n"
                + "Fecha: " + reservation.getDate() + "\n"
                + "Hora: " + reservation.getStartTimeAux() + " - " + reservation.getEndTimeAux()
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = reservationService.cancelReservation(reservation.getId());

                if (success) {
                    utilities.showAlert(Alert.AlertType.INFORMATION, "Éxito",
                            "La reservación ha sido cancelada exitosamente.");

                    loadUserReservations();
                } else {
                    utilities.showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo cancelar la reservación. Inténtelo nuevamente.");
                }
            } catch (Exception e) {
                utilities.showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al cancelar la reservación: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showReservationTable(ActionEvent event
    ) {
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
        if (currentUserId == null) {
            System.out.println("ERROR: No hay ID de usuario establecido");
            utilities.showAlert(Alert.AlertType.WARNING, "Advertencia", "No se puede cargar las reservaciones");
            return;
        }

        try {
            List<Reservation> userReservations = reservationService.findByUserId(currentUserId);

            reservationList.clear();
            reservationList.addAll(userReservations);
            tbvReservationTable.refresh();

        } catch (Exception e) {
            utilities.showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cargar las reservaciones: " + e.getMessage());
            e.printStackTrace();
        }
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
