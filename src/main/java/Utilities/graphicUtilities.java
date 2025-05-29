/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilities;

import Models.Room;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class graphicUtilities {

    public void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void drawFloorPlan(Pane pane, List<Room> rooms) {
        // Limpia cualquier dibujo previo
        pane.getChildren().clear();

        if (rooms == null || rooms.isEmpty()) {
            // Opcional: muestra un mensaje o placeholder
            Text aviso = new Text(10, 20, "No hay salas disponibles");
            aviso.setFill(Color.GRAY);
            aviso.setFont(Font.font(14));
            pane.getChildren().add(aviso);
            return;
        }

        // Cálculo de filas y columnas para el grid
        int total = rooms.size();
        int cols = (int) Math.ceil(Math.sqrt(total));
        int rows = (int) Math.ceil((double) total / cols);

        double width = pane.getWidth() - 20; // margen interno
        double height = pane.getHeight() - 20;
        double cellW = width / cols - 20;
        double cellH = height / rows - 20;

        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (idx >= total) {
                    break;
                }
                Room room = rooms.get(idx++);

                double x = 10 + c * (cellW + 20);
                double y = 10 + r * (cellH + 20);

                // Rectángulo de fondo
                Rectangle rect = new Rectangle(x, y, cellW, cellH);
                rect.setFill(Color.web("#AED6F1", 0.5));
                rect.setStroke(Color.web("#1B4F72"));
                rect.setStrokeWidth(1);

                // Texto con el nombre de la sala
                Text label = new Text(x + 5, y + 20, room.getRoomName());
                label.setFill(Color.web("#1B2631"));
                label.setFont(Font.font(12));

                pane.getChildren().addAll(rect, label);
            }
        }
    }
}
