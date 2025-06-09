package Controllers;

import Controllers.MakeReservationWindowController.MultipleReservationsData;
import Controllers.MakeReservationWindowController.ReservationData;
import Models.Space;
import Models.Reservation;
import Models.User;
import Services.ReservationService;
import Services.UserService;
import Utilities.FlowController;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.util.Duration;
import Utilities.PaymentAnimation;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import javafx.geometry.Pos;

public class PaymentWindowController implements Initializable {

    @FXML
    private Label lblTitle;
    @FXML
    private ScrollPane spReservationDetails;
    @FXML
    private VBox vbReservationDetails;
    @FXML
    private Label lblTotalPrice;

    @FXML
    private Label lblCardType;
    @FXML
    private ImageView ivCardImage;
    @FXML
    private VBox vbCardDetails;
    @FXML
    private TextField tfCardNumber;
    @FXML
    private TextField tfCardName;
    @FXML
    private TextField tfExpiryDate;
    @FXML
    private TextField tfCVV;
    @FXML
    private Button btnCvvHelp;
    @FXML
    private Button btnConfirmPayment;
    @FXML
    private Button btnCancel;

    @FXML
    private StackPane spPaymentOverlay;
    @FXML
    private ImageView ivPaymentAnimation;
    @FXML
    private Label lblPaymentStatus;
    @FXML
    private ProgressIndicator piPaymentProgress;
    private List<ReservationData> multipleReservations;

    private final ReservationService reservationService = new ReservationService();
    private final UserService userService = new UserService();

    private PaymentAnimation paymentAnimation;

    private MakeReservationWindowController.ReservationData reservationData;

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    private enum CardType {
        VISA("Visa", "images/visa_card.png"),
        MASTERCARD("MasterCard", "images/mastercard_card.png"),
        UNKNOWN("Tarjeta", "images/card_default.png");

        private final String displayName;
        private final String imagePath;

        CardType(String displayName, String imagePath) {
            this.displayName = displayName;
            this.imagePath = imagePath;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getImagePath() {
            return imagePath;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeComponents();
            loadReservationData();
            setupEventListeners();
            setupValidation();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización",
                    "Error al inicializar la ventana de pago: " + e.getMessage());
        }
    }

    private boolean createReservations() {
        try {
            User currentUser = UserService.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se encontró el usuario actual.");
                return false;
            }

            for (ReservationData rd : multipleReservations) {
                LocalDate d = rd.getDate();
                LocalDateTime start = rd.getStartTime();
                LocalDateTime end = rd.getEndTime();
                LocalDateTime now = LocalDateTime.now();

                for (Space s : rd.getSelectedSpaces()) {
                    int seats = rd.getSeatsPerSpace().getOrDefault(s, 1);
                    Reservation r = new Reservation(currentUser, s, now, start, end, seats);
                    reservationService.save(r);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initializeComponents() {

        lblCardType.setText("Tipo de Tarjeta: Detectando...");

        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/card_default.png"));
            ivCardImage.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen por defecto de la tarjeta");
        }

        configureCvvHelpButton();

        spPaymentOverlay.setVisible(false);

        btnConfirmPayment.setDisable(true);
    }

    private void configureCvvHelpButton() {
        if (btnCvvHelp != null) {
            Tooltip cvvTooltip = new Tooltip(
                    "CVV (Código de Verificación de la Tarjeta)\n\n"
                    + "• Para Visa/MasterCard: 3 dígitos en la parte trasera\n"
                    + "• Para American Express: 4 dígitos en el frente\n"
                    + "• Es un código de seguridad adicional\n"
                    + "• Ayuda a verificar que tienes la tarjeta física"
            );
            cvvTooltip.setFont(Font.font("Segoe UI", 12));
            cvvTooltip.setShowDelay(Duration.millis(300));
            Tooltip.install(btnCvvHelp, cvvTooltip);
        }
    }

    private void loadReservationData() {
        try {
            MultipleReservationsData multiData
                    = MakeReservationWindowController.getMultipleReservationsData();
            if (multiData == null || multiData.getReservations().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "No se encontraron datos de reservación. Regresando a la selección de espacios.");
                goBackToReservation();
                return;
            }

            // Guardamos la lista de ReservationData
            this.multipleReservations = multiData.getReservations();

            // Despliegas cada bloque de reserva
            displayMultipleReservationDetails(multiData);

            // Y pones el total
            lblTotalPrice.setText(
                    "Total a Pagar: $" + String.format("%.2f", multiData.getGrandTotal())
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cargar los datos de reservación: " + e.getMessage());
        }
    }

    private void displayMultipleReservationDetails(MultipleReservationsData multiData) {
        vbReservationDetails.getChildren().clear();

        for (ReservationData rd : multiData.getReservations()) {
            VBox block = createDetailSection("📅 Reserva: " + rd.getDate()
                    + " " + rd.getStartTime().toLocalTime()
                    + "–" + rd.getEndTime().toLocalTime(), "#1976d2");

            for (Space s : rd.getSelectedSpaces()) {
                int seats = rd.getSeatsPerSpace().getOrDefault(s, 1);
                double unitPrice = getSpacePriceValue(s)
                        * java.time.Duration.between(rd.getStartTime(), rd.getEndTime()).toMinutes() / 60.0;
                double totalForSpace = unitPrice * seats;
                block.getChildren().add(new Label(
                        String.format("• %s (%s) — %d asientos: $%.2f",
                                s.getSpaceName(),
                                s.getType().toString().replace("_", " "),
                                seats,
                                totalForSpace)
                ));
            }

            block.getChildren().add(createDetailLabel(
                    String.format("Subtotal reserva: $%.2f", rd.getTotalPrice())
            ));
            vbReservationDetails.getChildren().add(block);
        }
    }

    private void displayReservationDetails() {
        vbReservationDetails.getChildren().clear();

        VBox dateTimeBox = createDetailSection("📅 Fecha y Horario", "#1976d2");
        dateTimeBox.getChildren().addAll(
                createDetailLabel("Fecha: " + reservationData.getDate().toString()),
                createDetailLabel("Hora inicio: " + reservationData.getStartTime().toLocalTime()),
                createDetailLabel("Hora fin: " + reservationData.getEndTime().toLocalTime())
        );
        vbReservationDetails.getChildren().add(dateTimeBox);

        VBox spacesBox = createDetailSection("🏢 Espacios Seleccionados", "#388e3c");
        for (Space space : reservationData.getSelectedSpaces()) {
            int seats = reservationData.getSeatsPerSpace().getOrDefault(space, 1);
            VBox spaceDetail = new VBox(5);
            spaceDetail.setPadding(new Insets(10));
            spaceDetail.setStyle(
                    "-fx-background-color: #f5f5f5; "
                    + "-fx-border-color: #ddd; "
                    + "-fx-border-radius: 8; "
                    + "-fx-background-radius: 8;"
            );

            Label spaceName = new Label("• " + space.getSpaceName());
            Label spaceType = new Label("   Tipo: " + space.getType().toString().replace("_", " "));
            Label spaceSeats = new Label("   Asientos: " + seats + " / " + space.getCapacity());

            double unitPrice = getSpacePriceValue(space)
                    * java.time.Duration.between(
                            reservationData.getStartTime(), reservationData.getEndTime()
                    ).toMinutes() / 60.0;
            double totalForSpace = unitPrice * seats;
            Label spacePrice = new Label(
                    String.format("   Precio: $%.2f × %d = $%.2f", unitPrice, seats, totalForSpace)
            );

            spaceDetail.getChildren().addAll(spaceName, spaceType, spaceSeats, spacePrice);
            spacesBox.getChildren().add(spaceDetail);
        }
        vbReservationDetails.getChildren().add(spacesBox);

        VBox durationBox = createDetailSection("⏰ Duración", "#f57c00");
        double duration = java.time.Duration.between(
                reservationData.getStartTime(), reservationData.getEndTime()
        ).toMinutes() / 60.0;
        durationBox.getChildren().add(
                createDetailLabel("Duración total: " + String.format("%.1f horas", duration))
        );
        vbReservationDetails.getChildren().add(durationBox);
    }

    private VBox createDetailSection(String title, String color) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
                "-fx-background-color: white; "
                + "-fx-border-color: " + color + "; "
                + "-fx-border-width: 2; "
                + "-fx-border-radius: 10; "
                + "-fx-background-radius: 10;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web(color));

        section.getChildren().add(titleLabel);
        return section;
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web("#444"));
        label.setWrapText(true);
        return label;
    }

    private void displayTotalPrice() {
        lblTotalPrice.setText("Total a Pagar: $" + String.format("%.2f", reservationData.getTotalPrice()));
    }

    private double calculateDuration() {
        return java.time.Duration.between(
                reservationData.getStartTime(),
                reservationData.getEndTime()
        ).toMinutes() / 60.0;
    }

    private String getSpacePrice(Space space) {
        switch (space.getType()) {
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

    private void setupEventListeners() {

        tfCardNumber.textProperty().addListener((obs, oldVal, newVal) -> {
            handleCardNumberChange(newVal);
            validateForm();
        });

        tfCVV.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (newFocused) {
                animateCardFlip();
            } else {
                animateCardFlipBack();
            }
        });

        tfCardName.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        tfExpiryDate.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        tfCVV.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void handleCardNumberChange(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            updateCardDisplay(CardType.UNKNOWN);
            return;
        }

        String cleanNumber = cardNumber.replaceAll("\\D", "");

        if (cleanNumber.length() > 0) {
            char firstDigit = cleanNumber.charAt(0);

            switch (firstDigit) {
                case '4':
                    updateCardDisplay(CardType.VISA);
                    break;
                case '5':
                    updateCardDisplay(CardType.MASTERCARD);
                    break;
                default:
                    updateCardDisplay(CardType.UNKNOWN);
                    break;
            }
        }

        formatCardNumber(cardNumber);
    }

    private void formatCardNumber(String input) {

        String digits = input.replaceAll("\\D", "");

        if (digits.length() > 16) {
            digits = digits.substring(0, 16);
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(digits.charAt(i));
        }

        String currentText = tfCardNumber.getText();
        String newText = formatted.toString();

        if (!currentText.equals(newText)) {
            Platform.runLater(() -> {
                int caretPosition = tfCardNumber.getCaretPosition();
                tfCardNumber.setText(newText);

                int newCaretPos = Math.min(caretPosition + (newText.length() - currentText.length()), newText.length());
                tfCardNumber.positionCaret(newCaretPos);
            });
        }
    }

    private void updateCardDisplay(CardType cardType) {
        lblCardType.setText("Tipo de Tarjeta: " + cardType.getDisplayName());

        try {
            Image cardImage = new Image(getClass().getResourceAsStream("/" + cardType.getImagePath()));
            ivCardImage.setImage(cardImage);
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen de la tarjeta: " + cardType.getImagePath());
        }
    }

    private void animateCardFlip() {
        RotateTransition rotate = new RotateTransition(Duration.millis(300), ivCardImage);
        rotate.setByAngle(180);
        rotate.setAxis(new javafx.geometry.Point3D(0, 1, 0));
        rotate.play();
    }

    private void animateCardFlipBack() {
        RotateTransition rotate = new RotateTransition(Duration.millis(300), ivCardImage);
        rotate.setByAngle(-180);
        rotate.setAxis(new javafx.geometry.Point3D(0, 1, 0));
        rotate.play();
    }

    private void setupValidation() {

        tfExpiryDate.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d{0,2}/?\\d{0,2}")) {
                tfExpiryDate.setText(oldVal);
                return;
            }

            if (newVal.length() == 2 && !newVal.contains("/")) {
                tfExpiryDate.setText(newVal + "/");
                tfExpiryDate.positionCaret(3);
            }

            if (newVal.length() > 5) {
                tfExpiryDate.setText(oldVal);
            }
        });

        tfCVV.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d{0,4}")) {
                tfCVV.setText(oldVal);
            }
        });

        tfCardName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("[a-zA-Z\\s]*")) {
                tfCardName.setText(oldVal);
            }
        });
    }

    private void validateForm() {
        boolean isValid = true;

        String cardNumber = tfCardNumber.getText().replaceAll("\\D", "");
        if (cardNumber.length() != 16) {
            isValid = false;
        }

        if (tfCardName.getText().trim().length() < 2) {
            isValid = false;
        }

        if (!isValidExpiryDate(tfExpiryDate.getText())) {
            isValid = false;
        }

        String cvv = tfCVV.getText();
        if (cvv.length() < 3 || cvv.length() > 4) {
            isValid = false;
        }

        btnConfirmPayment.setDisable(!isValid);
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (!Pattern.matches("\\d{2}/\\d{2}", expiryDate)) {
            return false;
        }

        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);

            if (month < 1 || month > 12) {
                return false;
            }

            LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
            return expiry.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void processPayment(ActionEvent event) {
        if (!validatePaymentData()) {
            return;
        }

        // 1. Muestra el overlay de "procesando"
        showFullScreenProcessing();

        // 2. Después, lanza el guardado y la animación
        Timeline reservationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    boolean success = createReservations();
                    Platform.runLater(() -> {
                        if (success) {
                            if (paymentAnimation == null) {
                                paymentAnimation = new PaymentAnimation(spPaymentOverlay);
                                paymentAnimation.setOnAnimationComplete(() -> {
                                    Platform.runLater(() -> {
                                        try {
                                            goToConfirmation();
                                        } catch (IOException ioEx) {
                                            showAlert(Alert.AlertType.ERROR, "Error",
                                                    "No se pudo ir a la confirmación: " + ioEx.getMessage());
                                        }
                                    });
                                });
                            }
                            // 3. Calcula el gran total en lugar de usar reservationData
                            double grandTotal = multipleReservations.stream()
                                    .mapToDouble(ReservationData::getTotalPrice)
                                    .sum();

                            paymentAnimation.playPaymentAnimation(grandTotal);
                        } else {
                            showPaymentError();
                        }
                    });
                })
        );
        reservationTimeline.play();
    }

    private void showFullScreenProcessing() {

        spPaymentOverlay.setVisible(true);
        spPaymentOverlay.toFront();

        lblPaymentStatus.setText("Procesando pago...");
        lblPaymentStatus.setTextFill(Color.web("#424242"));
        lblPaymentStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        piPaymentProgress.setVisible(true);

        try {
            Image processingImage = new Image(getClass().getResourceAsStream("/images/payment_processing.gif"));
            ivPaymentAnimation.setImage(processingImage);
        } catch (Exception e) {
            System.out.println("No se pudo cargar la animación de procesamiento");
        }
    }

    private void showPaymentSuccess() {
        lblPaymentStatus.setText("¡Pago Exitoso!");
        lblPaymentStatus.setTextFill(Color.web("#4CAF50"));
        lblPaymentStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        piPaymentProgress.setVisible(false);

        try {
            Image successImage = new Image(
                    getClass().getResourceAsStream("/images/payment_success.png")
            );
            ivPaymentAnimation.setImage(successImage);
            ivPaymentAnimation.setFitWidth(120);
            ivPaymentAnimation.setFitHeight(120);
            ivPaymentAnimation.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen de éxito");
        }

        // Calcula aquí el gran total
        double grandTotal = multipleReservations.stream()
                .mapToDouble(ReservationData::getTotalPrice)
                .sum();

        Label totalLabel = new Label("Total pagado: $"
                + String.format("%.2f", grandTotal));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalLabel.setTextFill(Color.WHITE);

        ScaleTransition scale = new ScaleTransition(Duration.millis(500), ivPaymentAnimation);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.play();

        VBox overlayContent = new VBox(20, ivPaymentAnimation, lblPaymentStatus, totalLabel);
        overlayContent.setAlignment(Pos.CENTER);
        spPaymentOverlay.getChildren().setAll(overlayContent);
    }

    private void showPaymentError() {
        lblPaymentStatus.setText("Error en el Pago");
        lblPaymentStatus.setTextFill(Color.web("#F44336"));
        lblPaymentStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        piPaymentProgress.setVisible(false);

        try {
            Image errorImage = new Image(getClass().getResourceAsStream("/images/payment_error.png"));
            ivPaymentAnimation.setImage(errorImage);
            ivPaymentAnimation.setFitWidth(100);
            ivPaymentAnimation.setFitHeight(100);
            ivPaymentAnimation.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen de error");
        }

        TranslateTransition shake = new TranslateTransition(Duration.millis(100), ivPaymentAnimation);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void hideProcessingOverlay() {

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), spPaymentOverlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            spPaymentOverlay.setVisible(false);
            spPaymentOverlay.setOpacity(1.0);
        });
        fadeOut.play();
    }

    private boolean validatePaymentData() {

        String cardNumber = tfCardNumber.getText().replaceAll("\\D", "");

        if (cardNumber.length() != 16) {
            showAlert(Alert.AlertType.WARNING, "Datos Incompletos",
                    "El número de tarjeta debe tener 16 dígitos.");
            return false;
        }

        if (tfCardName.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.WARNING, "Datos Incompletos",
                    "Ingrese el nombre del titular de la tarjeta.");
            return false;
        }

        if (!isValidExpiryDate(tfExpiryDate.getText())) {
            showAlert(Alert.AlertType.WARNING, "Fecha Inválida",
                    "La fecha de vencimiento es inválida o ha expirado.");
            return false;
        }

        String cvv = tfCVV.getText();
        if (cvv.length() < 3 || cvv.length() > 4) {
            showAlert(Alert.AlertType.WARNING, "CVV Inválido",
                    "El CVV debe tener 3 o 4 dígitos.");
            return false;
        }

        return true;
    }

    private void showPaymentProcessing() {
        spPaymentOverlay.setVisible(true);
        lblPaymentStatus.setText("Procesando pago...");
        piPaymentProgress.setVisible(true);

        try {
            Image processingImage = new Image(getClass().getResourceAsStream("/images/payment_processing.gif"));
            ivPaymentAnimation.setImage(processingImage);
        } catch (Exception e) {
            System.out.println("No se pudo cargar la animación de procesamiento");
        }
    }

    private double calculateSpacePrice(Space space) {
        double pricePerHour = getSpacePriceValue(space);
        double duration = calculateDuration();
        return pricePerHour * duration;
    }

    private double getSpacePriceValue(Space space) {
        switch (space.getType()) {
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

    private void goToConfirmation() throws IOException {
        FlowController.getInstance().goView("UserViewWindow");
    }

    @FXML
    private void cancelPayment(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancelar Compra");
        confirmAlert.setHeaderText("¿Está seguro de cancelar la compra?");
        confirmAlert.setContentText("Se perderán todos los datos ingresados y regresará a la selección de espacios.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                goBackToReservation();
            }
        });
    }

    private void goBackToReservation() {
        try {
            FlowController.getInstance().goView("MakeReservationWindow");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo regresar a la ventana de reservación: " + e.getMessage());
        }
    }

}
