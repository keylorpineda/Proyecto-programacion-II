package Utilities;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.util.Duration;
import java.io.File;
import javafx.scene.Group;

public class PaymentAnimation {

    private StackPane animationContainer;
    private Runnable onAnimationComplete;

    public PaymentAnimation(StackPane container) {
        this.animationContainer = container;
    }

    public void setOnAnimationComplete(Runnable callback) {
        this.onAnimationComplete = callback;
    }

    public void playPaymentAnimation(double totalAmount) {

        animationContainer.getChildren().clear();
        animationContainer.setVisible(true);

        createAnimationBackground();

        Group dataphone = createDataphone(totalAmount);
        Group card = createCreditCard();

        dataphone.setLayoutX(150);
        dataphone.setLayoutY(50);

        card.setLayoutX(-200);
        card.setLayoutY(120);

        animationContainer.getChildren().addAll(dataphone, card);

        executePaymentSequence(card, dataphone, totalAmount);
    }

    private void createAnimationBackground() {
        Rectangle background = new Rectangle(800, 600);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null,
                new Stop(0, Color.web("#1e3c72")),
                new Stop(1, Color.web("#2a5298"))
        );
        background.setFill(gradient);
        background.setOpacity(0.95);

        animationContainer.getChildren().add(background);
    }

    private Group createDataphone(double amount) {
        Group dataphone = new Group();

        Rectangle base = new Rectangle(180, 280);
        base.setFill(Color.web("#2c3e50"));
        base.setArcWidth(20);
        base.setArcHeight(20);
        base.setStroke(Color.web("#34495e"));
        base.setStrokeWidth(2);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setOffsetX(5);
        shadow.setOffsetY(5);
        shadow.setRadius(10);
        base.setEffect(shadow);

        Rectangle screen = new Rectangle(140, 80);
        screen.setX(20);
        screen.setY(30);
        screen.setFill(Color.web("#27ae60"));
        screen.setArcWidth(8);
        screen.setArcHeight(8);
        screen.setStroke(Color.web("#2c3e50"));
        screen.setStrokeWidth(2);

        Label priceLabel = new Label("$" + String.format("%.2f", amount));
        priceLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        priceLabel.setTextFill(Color.BLACK);
        priceLabel.setLayoutX(35);
        priceLabel.setLayoutY(55);

        Rectangle cardSlot = new Rectangle(120, 8);
        cardSlot.setX(30);
        cardSlot.setY(150);
        cardSlot.setFill(Color.web("#1a1a1a"));
        cardSlot.setArcWidth(4);
        cardSlot.setArcHeight(4);

        Rectangle slotLine1 = new Rectangle(100, 1);
        slotLine1.setX(40);
        slotLine1.setY(152);
        slotLine1.setFill(Color.web("#34495e"));

        Rectangle slotLine2 = new Rectangle(100, 1);
        slotLine2.setX(40);
        slotLine2.setY(155);
        slotLine2.setFill(Color.web("#34495e"));

        Circle btn1 = createDataphoneButton(50, 200);
        Circle btn2 = createDataphoneButton(90, 200);
        Circle btn3 = createDataphoneButton(130, 200);

        Circle btn4 = createDataphoneButton(50, 230);
        Circle btn5 = createDataphoneButton(90, 230);
        Circle btn6 = createDataphoneButton(130, 230);

        dataphone.getChildren().addAll(base, screen, priceLabel, cardSlot,
                slotLine1, slotLine2, btn1, btn2, btn3, btn4, btn5, btn6);

        return dataphone;
    }

    private Circle createDataphoneButton(double x, double y) {
        Circle button = new Circle(x, y, 12);
        button.setFill(Color.web("#ecf0f1"));
        button.setStroke(Color.web("#bdc3c7"));
        button.setStrokeWidth(1);

        button.setOnMousePressed(e -> button.setFill(Color.web("#d5dbdb")));
        button.setOnMouseReleased(e -> button.setFill(Color.web("#ecf0f1")));

        return button;
    }

    private Group createCreditCard() {
        Group card = new Group();

        Rectangle cardBody = new Rectangle(240, 150);
        LinearGradient cardGradient = new LinearGradient(0, 0, 1, 1, true, null,
                new Stop(0, Color.web("#667eea")),
                new Stop(1, Color.web("#764ba2"))
        );
        cardBody.setFill(cardGradient);
        cardBody.setArcWidth(15);
        cardBody.setArcHeight(15);
        cardBody.setStroke(Color.web("#5a67d8"));
        cardBody.setStrokeWidth(2);

        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.web("#000000", 0.4));
        cardShadow.setOffsetX(3);
        cardShadow.setOffsetY(3);
        cardShadow.setRadius(8);
        cardBody.setEffect(cardShadow);

        Rectangle chip = new Rectangle(30, 24);
        chip.setX(25);
        chip.setY(40);
        chip.setFill(Color.web("#ffd700"));
        chip.setArcWidth(4);
        chip.setArcHeight(4);
        chip.setStroke(Color.web("#daa520"));
        chip.setStrokeWidth(1);

        Line chipLine1 = new Line(30, 45, 50, 45);
        chipLine1.setStroke(Color.web("#b8860b"));
        chipLine1.setStrokeWidth(0.5);

        Line chipLine2 = new Line(30, 55, 50, 55);
        chipLine2.setStroke(Color.web("#b8860b"));
        chipLine2.setStrokeWidth(0.5);

        Label cardNumber = new Label("•••• •••• •••• ••••");
        cardNumber.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        cardNumber.setTextFill(Color.WHITE);
        cardNumber.setLayoutX(25);
        cardNumber.setLayoutY(80);

        Label cardHolder = new Label("");
        cardHolder.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        cardHolder.setTextFill(Color.WHITE);
        cardHolder.setLayoutX(25);
        cardHolder.setLayoutY(110);

        Label expiry = new Label("");
        expiry.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        expiry.setTextFill(Color.WHITE);
        expiry.setLayoutX(25);
        expiry.setLayoutY(125);

        Label visa = new Label("CARD");
        visa.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        visa.setTextFill(Color.WHITE);
        visa.setLayoutX(180);
        visa.setLayoutY(20);

        card.getChildren().addAll(cardBody, chip, chipLine1, chipLine2,
                cardNumber, cardHolder, expiry, visa);

        return card;
    }

    private void executePaymentSequence(Group card, Group dataphone, double amount) {

        TranslateTransition insertCard = new TranslateTransition(Duration.seconds(2), card);
        insertCard.setToX(380);
        insertCard.setInterpolator(Interpolator.EASE_IN);

        Rectangle screen = (Rectangle) dataphone.getChildren().get(1);

        Timeline processing = new Timeline();
        processing.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        e -> screen.setFill(Color.web("#f39c12"))),
                new KeyFrame(Duration.millis(300),
                        e -> screen.setFill(Color.web("#27ae60"))),
                new KeyFrame(Duration.millis(600),
                        e -> screen.setFill(Color.web("#f39c12"))),
                new KeyFrame(Duration.millis(900),
                        e -> screen.setFill(Color.web("#27ae60")))
        );
        processing.setCycleCount(3);

        Timeline success = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    screen.setFill(Color.web("#2ecc71"));
                    Glow glow = new Glow();
                    glow.setLevel(0.8);
                    screen.setEffect(glow);
                })
        );

        SequentialTransition sequence = new SequentialTransition(
                insertCard,
                new PauseTransition(Duration.millis(500)),
                processing,
                success,
                new PauseTransition(Duration.seconds(1))
        );

        sequence.setOnFinished(e -> showSuccessAnimation());
        sequence.play();
    }

    private void showSuccessAnimation() {

        animationContainer.getChildren().clear();

        Rectangle whiteBackground = new Rectangle(800, 600);
        whiteBackground.setFill(Color.WHITE);
        animationContainer.getChildren().add(whiteBackground);

        Group checkMark = createAnimatedCheckMark();
        checkMark.setLayoutX(350);
        checkMark.setLayoutY(250);

        animationContainer.getChildren().add(checkMark);

        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.8), checkMark);
        scaleIn.setFromX(0);
        scaleIn.setFromY(0);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        Timeline glow = new Timeline();
        for (int i = 0; i <= 10; i++) {
            final double glowLevel = i / 10.0;
            glow.getKeyFrames().add(
                    new KeyFrame(Duration.millis(i * 100), e -> {
                        Glow glowEffect = new Glow();
                        glowEffect.setLevel(glowLevel);
                        checkMark.setEffect(glowEffect);
                    })
            );
        }

        SequentialTransition finalSequence = new SequentialTransition(
                scaleIn,
                glow,
                new PauseTransition(Duration.seconds(2))
        );

        finalSequence.setOnFinished(e -> {
            animationContainer.setVisible(false);
            if (onAnimationComplete != null) {
                onAnimationComplete.run();
            }
        });

        finalSequence.play();
    }

    private Group createAnimatedCheckMark() {
        Group checkGroup = new Group();

        Circle background = new Circle(50);
        background.setFill(Color.web("#2ecc71"));
        background.setStroke(Color.web("#27ae60"));
        background.setStrokeWidth(3);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(8);
        background.setEffect(shadow);

        Path checkPath = new Path();
        MoveTo moveTo = new MoveTo(-15, 0);
        LineTo lineTo1 = new LineTo(-5, 10);
        LineTo lineTo2 = new LineTo(20, -15);

        checkPath.getElements().addAll(moveTo, lineTo1, lineTo2);
        checkPath.setStroke(Color.WHITE);
        checkPath.setStrokeWidth(4);
        checkPath.setStrokeLineCap(StrokeLineCap.ROUND);
        checkPath.setStrokeLineJoin(StrokeLineJoin.ROUND);

        checkGroup.getChildren().addAll(background, checkPath);

        return checkGroup;
    }
}
