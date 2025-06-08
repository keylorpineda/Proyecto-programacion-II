package Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import Utilities.FlowController;

public class SnakeGameController implements Initializable {

    @FXML
    private Pane gamePane;

    @FXML
    private Label lblScore;

    @FXML
    private StackPane overlayPane;

    @FXML
    private Button btnStartGame;

    private final int tileSize = 20;
    private final int width = 30;
    private final int height = 20;

    private List<Rectangle> snake;
    private String direction = "RIGHT";
    private boolean running = true;
    private Timeline timeline;
    private Rectangle food;
    private int score = 0;
    private double currentSpeed = 0.2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnStartGame.setOnAction(e -> {
            overlayPane.setVisible(false);
            startGame();
            Platform.runLater(() -> gamePane.requestFocus());
        });

        Platform.runLater(() -> gamePane.requestFocus());

        gamePane.setOnKeyPressed(e -> {
            if (!running && e.getCode() == KeyCode.R) {
                restartGame();
                return;
            }

            if (e.getCode() == KeyCode.ESCAPE) {
                try {
                    FlowController.getInstance().goView("UserViewWindow");
                } catch (Exception ex) {
                    System.out.println("Error al volver a la pantalla de usuario:");
                    ex.printStackTrace();
                }
                return;
            }

            if (e.getCode() == KeyCode.W && !direction.equals("DOWN")) direction = "UP";
            if (e.getCode() == KeyCode.S && !direction.equals("UP")) direction = "DOWN";
            if (e.getCode() == KeyCode.A && !direction.equals("RIGHT")) direction = "LEFT";
            if (e.getCode() == KeyCode.D && !direction.equals("LEFT")) direction = "RIGHT";
        });

        gamePane.setFocusTraversable(true);
    }

    private void startGame() {
        gamePane.getChildren().clear();
        score = 0;
        updateScoreLabel();

        snake = new ArrayList<>();
        Rectangle head = createRectangle(tileSize * 5, tileSize * 5, Color.LIME);
        snake.add(head);
        gamePane.getChildren().add(head);

        direction = "RIGHT";
        running = true;
        spawnFood();

        startTimeline(currentSpeed);
    }

    private void restartGame() {
        if (timeline != null) timeline.stop();
        currentSpeed = 0.2;
        startGame();
        gamePane.requestFocus();
    }

    private void startTimeline(double speed) {
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(speed), e -> {
            if (running) moveSnake();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void moveSnake() {
        Rectangle head = snake.get(0);
        double x = head.getX();
        double y = head.getY();

        switch (direction) {
            case "UP": y -= tileSize; break;
            case "DOWN": y += tileSize; break;
            case "LEFT": x -= tileSize; break;
            case "RIGHT": x += tileSize; break;
        }

        if (x < 0 || x + tileSize > gamePane.getWidth() || y < 0 || y + tileSize > gamePane.getHeight() || hitsItself(x, y)) {
            gameOver();
            return;
        }

        Rectangle newHead = createRectangle(x, y, Color.LIMEGREEN);
        snake.add(0, newHead);
        gamePane.getChildren().add(newHead);

        if (x == food.getX() && y == food.getY()) {
            score++;
            updateScoreLabel();

            if (currentSpeed > 0.05) {
                currentSpeed -= 0.01;
                startTimeline(currentSpeed);
            }

            spawnFood();
        } else {
            Rectangle tail = snake.remove(snake.size() - 1);
            gamePane.getChildren().remove(tail);
        }
    }
private boolean hitsItself(double x, double y) {
    for (int i = 1; i < snake.size(); i++) { 
        Rectangle part = snake.get(i);
        if (part.getX() == x && part.getY() == y) return true;
    }
    return false;
}

    private void spawnFood() {
        Random random = new Random();
        double x, y;
        do {
            x = random.nextInt(width) * tileSize;
            y = random.nextInt(height) * tileSize;
        } while (isOnSnake(x, y));

        if (food != null) gamePane.getChildren().remove(food);
        food = createRectangle(x, y, Color.RED);
        gamePane.getChildren().add(food);
    }

    private boolean isOnSnake(double x, double y) {
        for (Rectangle part : snake) {
            if (part.getX() == x && part.getY() == y) return true;
        }
        return false;
    }

    private Rectangle createRectangle(double x, double y, Color color) {
        Rectangle r = new Rectangle(tileSize, tileSize);
        r.setX(x);
        r.setY(y);
        r.setFill(color);
        return r;
    }

    private void updateScoreLabel() {
        if (lblScore != null)
            lblScore.setText("Puntos: " + score);
    }

    private void showGameOverAnimation() {
        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setStyle("-fx-text-fill: red; -fx-font-size: 60px; -fx-font-weight: bold;");
        gameOverLabel.setOpacity(0);
        StackPane.setAlignment(gameOverLabel, Pos.CENTER);
        gamePane.getChildren().add(gameOverLabel);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), gameOverLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), gameOverLabel);
        scaleUp.setFromX(0);
        scaleUp.setFromY(0);
        scaleUp.setToX(1.3);
        scaleUp.setToY(1.3);

        ScaleTransition bounceBack = new ScaleTransition(Duration.millis(200), gameOverLabel);
        bounceBack.setFromX(1.3);
        bounceBack.setFromY(1.3);
        bounceBack.setToX(1);
        bounceBack.setToY(1);

        SequentialTransition sequence = new SequentialTransition(scaleUp, bounceBack, fadeIn);
        sequence.play();
    }

    private void gameOver() {
        running = false;
        showGameOverAnimation();
    }
}
