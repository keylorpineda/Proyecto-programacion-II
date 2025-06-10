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
    private Button btnEditarUsuario;

    private ReservationService reservationService;
    private Long currentUserId;
    @FXML
    private Button btnMinijuegoSnake;

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
            } else {
                utilities.showAlert(Alert.AlertType.WARNING, "Advertencia",
                        "Por favor seleccione una reservación para cancelar.");
            }
        });

        contextMenu.getItems().add(cancelItem);

        tbvReservationTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Reservation selectedItem = tbvReservationTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getId() != null && selectedItem.getId() > 0) {
                    contextMenu.show(tbvReservationTable, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                    if (selectedItem == null) {
                        utilities.showAlert(Alert.AlertType.WARNING, "Advertencia",
                                "Por favor seleccione una reservación válida.");
                    }
                }
            } else {
                contextMenu.hide();
            }
        });
    }

    private void cancelReservation(Reservation reservation) {
        if (reservation == null) {
            utilities.showAlert(Alert.AlertType.ERROR, "Error", "No se ha seleccionado una reservación válida.");
            return;
        }

        if (reservation.getId() == null || reservation.getId() <= 0) {
            utilities.showAlert(Alert.AlertType.ERROR, "Error", "La reservación seleccionada no tiene un ID válido.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Cancelación");
        confirmAlert.setHeaderText("¿Está seguro que desea cancelar esta reservación?");

        StringBuilder contentText = new StringBuilder();
        contentText.append("ID: ").append(reservation.getId()).append("\n");

        try {
            if (reservation.getPlace() != null) {
                contentText.append("Lugar: ").append(reservation.getPlace()).append("\n");
            }
        } catch (Exception e) {
            contentText.append("Lugar: No disponible\n");
        }

        if (reservation.getSpaceName() != null) {
            contentText.append("Espacio: ").append(reservation.getSpaceName()).append("\n");
        }

        if (reservation.getDate() != null) {
            contentText.append("Fecha: ").append(reservation.getDate()).append("\n");
        }

        if (reservation.getStartTimeAux() != null && reservation.getEndTimeAux() != null) {
            contentText.append("Hora: ").append(reservation.getStartTimeAux()).append(" - ").append(reservation.getEndTimeAux());
        }

        confirmAlert.setContentText(contentText.toString());

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (reservationService == null) {
                    utilities.showAlert(Alert.AlertType.ERROR, "Error", "Servicio de reservaciones no disponible.");
                    return;
                }

                boolean success = reservationService.cancelReservation(reservation.getId());

                if (success) {
                    utilities.showAlert(Alert.AlertType.INFORMATION, "Éxito",
                            "La reservación ha sido cancelada exitosamente.");
                    loadUserReservations();
                } else {
                    utilities.showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo cancelar la reservación. Puede que ya haya sido cancelada o no exista.");
                }

            } catch (IllegalArgumentException e) {
                utilities.showAlert(Alert.AlertType.ERROR, "Error de Validación",
                        "Datos de reservación inválidos: " + e.getMessage());

            } catch (RuntimeException e) {
                utilities.showAlert(Alert.AlertType.ERROR, "Error de Sistema",
                        "Error en el sistema: " + e.getMessage());
                e.printStackTrace();

            } catch (Exception e) {
                utilities.showAlert(Alert.AlertType.ERROR, "Error",
                        "Error inesperado al cancelar la reservación: " + e.getMessage());
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
