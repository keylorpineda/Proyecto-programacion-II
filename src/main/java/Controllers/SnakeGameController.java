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
    private int width;
    private int height;

    private List<Rectangle> snake;
    private String direction = "RIGHT";
    private boolean running = true;
    private Timeline timeline;
    private Rectangle food;
    private int score = 0;
    private double currentSpeed = 0.2;

    private List<Rectangle> obstacles = new ArrayList<>();

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

            if (e.getCode() == KeyCode.W && !direction.equals("DOWN")) {
                direction = "UP";
            }
            if (e.getCode() == KeyCode.S && !direction.equals("UP")) {
                direction = "DOWN";
            }
            if (e.getCode() == KeyCode.A && !direction.equals("RIGHT")) {
                direction = "LEFT";
            }
            if (e.getCode() == KeyCode.D && !direction.equals("LEFT")) {
                direction = "RIGHT";
            }
        });

        gamePane.setFocusTraversable(true);
    }

    private void startGame() {
        gamePane.getChildren().clear();
        obstacles.clear();
        score = 0;
        updateScoreLabel();

        double gamePaneWidth = gamePane.getWidth();
        double gamePaneHeight = gamePane.getHeight();

        if (gamePaneWidth == 0) {
            gamePaneWidth = 1200.0;
        }
        if (gamePaneHeight == 0) {
            gamePaneHeight = 800.0;
        }

        width = (int) (gamePaneWidth / tileSize);
        height = (int) (gamePaneHeight / tileSize);

        snake = new ArrayList<>();
        Rectangle head = createRectangle(tileSize * 5, tileSize * 5, Color.LIME);
        snake.add(head);
        gamePane.getChildren().add(head);

        direction = "RIGHT";
        running = true;
        spawnObstacles();
        spawnFood();

        startTimeline(currentSpeed);
    }

    private void restartGame() {
        if (timeline != null) {
            timeline.stop();
        }
        currentSpeed = 0.2;
        startGame();
        gamePane.requestFocus();
    }

    private void startTimeline(double speed) {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(speed), e -> {
            if (running) {
                moveSnake();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void moveSnake() {
        Rectangle head = snake.get(0);
        double x = head.getX();
        double y = head.getY();

        switch (direction) {
            case "UP":
                y -= tileSize;
                break;
            case "DOWN":
                y += tileSize;
                break;
            case "LEFT":
                x -= tileSize;
                break;
            case "RIGHT":
                x += tileSize;
                break;
        }

        double gamePaneWidth = gamePane.getWidth();
        double gamePaneHeight = gamePane.getHeight();

        if (gamePaneWidth == 0) {
            gamePaneWidth = 1200.0;
        }
        if (gamePaneHeight == 0) {
            gamePaneHeight = 800.0;
        }

        if (x < 0) {
            x = gamePaneWidth - tileSize;
        }
        if (x + tileSize > gamePaneWidth) {
            x = 0;
        }
        if (y < 0) {
            y = gamePaneHeight - tileSize;
        }
        if (y + tileSize > gamePaneHeight) {
            y = 0;
        }

        if (hitsItself(x, y) || isOnObstacle(x, y)) {
            handleCollision(x, y);
            return;
        }

        Rectangle newHead = createRectangle(x, y, Color.LIMEGREEN);
        snake.add(0, newHead);
        gamePane.getChildren().add(newHead);

        if (x == food.getX() && y == food.getY()) {
            score++;
            updateScoreLabel();

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), newHead);
            scaleUp.setFromX(1);
            scaleUp.setFromY(1);
            scaleUp.setToX(1.5);
            scaleUp.setToY(1.5);
            scaleUp.setCycleCount(1);
            scaleUp.setAutoReverse(true);
            scaleUp.play();

            spawnFood();
        } else {
            Rectangle tail = snake.remove(snake.size() - 1);
            gamePane.getChildren().remove(tail);
        }

        if (currentSpeed > 0.05) {
            currentSpeed -= 0.0001;
            startTimeline(currentSpeed);
        }
    }

    private boolean hitsItself(double x, double y) {
        for (int i = 1; i < snake.size(); i++) {
            Rectangle part = snake.get(i);
            if (part.getX() == x && part.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void spawnFood() {
        Random random = new Random();
        double x, y;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            x = random.nextInt(width) * tileSize;
            y = random.nextInt(height) * tileSize;
            attempts++;
        } while ((isOnSnake(x, y) || isOnObstacle(x, y)) && attempts < maxAttempts);

        if (food != null) {
            gamePane.getChildren().remove(food);
        }

        food = createRectangle(x, y, Color.YELLOW);
        gamePane.getChildren().add(food);
    }

    private boolean isOnSnake(double x, double y) {
        for (Rectangle part : snake) {
            if (part.getX() == x && part.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnObstacle(double x, double y) {
        for (Rectangle obstacle : obstacles) {
            if (obstacle.getX() == x && obstacle.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void spawnObstacles() {
        Random random = new Random();

        int totalTiles = width * height;
        int numObstacles = Math.max(30, totalTiles / 50);

        for (int i = 0; i < numObstacles; i++) {
            double x, y;
            int attempts = 0;
            int maxAttempts = 100;

            do {
                x = random.nextInt(width) * tileSize;
                y = random.nextInt(height) * tileSize;
                attempts++;
            } while ((isOnSnake(x, y) || isOnObstacle(x, y) || isNearSnakeStart(x, y)) && attempts < maxAttempts);

            if (attempts < maxAttempts) {
                Rectangle obstacle = createRectangle(x, y, Color.DARKRED);
                obstacles.add(obstacle);
                gamePane.getChildren().add(obstacle);
            }
        }
    }

    private boolean isNearSnakeStart(double x, double y) {
        double startX = tileSize * 5;
        double startY = tileSize * 5;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (x == startX + (dx * tileSize) && y == startY + (dy * tileSize)) {
                    return true;
                }
            }
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
        if (lblScore != null) {
            lblScore.setText("Puntos: " + score);
        }
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

    private void handleCollision(double x, double y) {
        Rectangle head = snake.get(0);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), head);
        scaleUp.setFromX(1);
        scaleUp.setFromY(1);
        scaleUp.setToX(1.5);
        scaleUp.setToY(1.5);
        scaleUp.setCycleCount(1);
        scaleUp.setAutoReverse(true);
        scaleUp.play();

        head.setFill(Color.RED);
        gameOver();
    }

    private void gameOver() {
        running = false;
        showGameOverAnimation();
    }
}
