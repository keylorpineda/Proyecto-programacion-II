package Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class SnakeGameController implements Initializable {

    @FXML
    private Pane gamePane;

    private final int tileSize = 20;
    private final int width = 30;
    private final int height = 20;

    private List<Rectangle> snake;
    private String direction = "RIGHT";
    private boolean running = true;
    private Timeline timeline;
    private Rectangle food;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startGame();

        Platform.runLater(() -> gamePane.requestFocus());

        gamePane.setOnKeyPressed(e -> {
            if (!running && e.getCode() == KeyCode.R) {
                restartGame();
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
        snake = new ArrayList<>();
        Rectangle head = createRectangle(tileSize * 5, tileSize * 5, Color.LIME);
        snake.add(head);
        gamePane.getChildren().add(head);

        direction = "RIGHT";
        running = true;
        spawnFood();

        timeline = new Timeline(new KeyFrame(Duration.millis(150), e -> moveSnake()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void restartGame() {
        if (timeline != null) {
            timeline.stop();
        }
        startGame();
        gamePane.requestFocus();
    }

    private void moveSnake() {
        if (!running) return;

        Rectangle head = snake.get(0);
        double x = head.getX();
        double y = head.getY();

        switch (direction) {
            case "UP": y -= tileSize; break;
            case "DOWN": y += tileSize; break;
            case "LEFT": x -= tileSize; break;
            case "RIGHT": x += tileSize; break;
        }

        if (x < 0 || x >= width * tileSize || y < 0 || y >= height * tileSize || hitsItself(x, y)) {
            gameOver();
            return;
        }

        Rectangle newHead = createRectangle(x, y, Color.LIMEGREEN);
        snake.add(0, newHead);
        gamePane.getChildren().add(newHead);

        if (x == food.getX() && y == food.getY()) {
            spawnFood();
        } else {
            Rectangle tail = snake.remove(snake.size() - 1);
            gamePane.getChildren().remove(tail);
        }
    }

    private boolean hitsItself(double x, double y) {
        for (Rectangle part : snake) {
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

    private void gameOver() {
        running = false;
        timeline.stop();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("¡Perdiste! Presiona R para reiniciar.");
            alert.show();
        });
    }
}
