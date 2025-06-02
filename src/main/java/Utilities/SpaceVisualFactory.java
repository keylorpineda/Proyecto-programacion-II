package Utilities;

import Models.Space;
import Models.SpaceType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SpaceVisualFactory {

    public static Node createVisual(Space space, boolean editable, Runnable onDelete) {
        double width = space.getWidth() * 60;
        double height = space.getHeight() * 60;

        Rectangle rect = new Rectangle(width, height);
        rect.setArcWidth(18);
        rect.setArcHeight(18);
        rect.setStroke(Color.web("#2c3e50"));
        rect.setStrokeWidth(2);

        Color baseColor = getColorByType(space.getType());
        rect.setFill(space.getAvailable() ? baseColor : baseColor.darker().darker());
        rect.setOpacity(space.getAvailable() ? 1.0 : 0.7);
        rect.setEffect(new DropShadow(8, Color.rgb(80, 80, 80, 0.18)));

        // Etiquetas de nombre y capacidad
        Label lblNombre = new Label(space.getName());
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblNombre.setTextFill(Color.web("#222"));

        Label lblCapacidad = new Label("Cap: " + space.getCapacity());
        lblCapacidad.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lblCapacidad.setTextFill(Color.web("#444"));

        VBox infoBox = new VBox(lblNombre, lblCapacidad);
        infoBox.setAlignment(Pos.CENTER);

        StackPane pane = new StackPane(rect, infoBox);
        pane.setAlignment(Pos.CENTER);

        // Tooltip completo
        String tooltipText = "Nombre: " + space.getName()
                + "\nTipo: " + getTypeName(space.getType())
                + "\nCapacidad: " + space.getCapacity()
                + "\nDisponible: " + (space.getAvailable() ? "Sí" : "No");
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(pane, tooltip);

        // Opcional: animación al hacer click
        pane.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                rect.setFill(Color.YELLOW);
            }
        });
        pane.setOnMouseReleased(event -> {
            rect.setFill(space.getAvailable() ? baseColor : baseColor.darker().darker());
        });

        // Menú contextual para eliminar
        if (editable) {
            ContextMenu menu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Eliminar espacio");
            deleteItem.setOnAction(e -> {
                if (onDelete != null) onDelete.run();
            });
            menu.getItems().add(deleteItem);

            pane.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    menu.show(pane, event.getScreenX(), event.getScreenY());
                } else {
                    menu.hide();
                }
            });
        }

        return pane;
    }

    private static Color getColorByType(SpaceType type) {
        switch (type) {
            case ESCRITORIO:
                return Color.web("#FBA834");      // Naranja pastel
            case AREA_COMUN:
                return Color.web("#95E1D3");      // Verde agua
            case SALA_REUNIONES:
                return Color.web("#9B59B6");      // Morado elegante
            case PASILLO:
                return Color.web("#D7DDE8");      // Gris azulado claro
            default:
                return Color.LIGHTGRAY;
        }
    }

    private static String getTypeName(SpaceType type) {
        switch (type) {
            case ESCRITORIO:
                return "Escritorio";
            case AREA_COMUN:
                return "Área común";
            case SALA_REUNIONES:
                return "Sala de reuniones";
            case PASILLO:
                return "Pasillo";
            default:
                return "Otro";
        }
    }
}