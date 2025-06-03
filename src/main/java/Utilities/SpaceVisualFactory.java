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

    public static Node createVisual(Space space, boolean editable, Runnable onDelete, int totalRows, int totalCols) {
        double width = space.getWidth() * 60;
        double height = space.getHeight() * 60;

        Rectangle rect = new Rectangle(width, height);

        // Por defecto, sin esquinas redondeadas
        rect.setArcWidth(0);
        rect.setArcHeight(0);

        StackPane pane = new StackPane(rect);
        pane.setAlignment(Pos.CENTER);

        if (space.getType() == SpaceType.PASILLO) {
            // Color mate café, sin brillo
            rect.setFill(Color.web("#8d6e63"));
            rect.setStroke(Color.web("#5d4037"));
            rect.setStrokeWidth(1.5);
            rect.setEffect(new DropShadow(6, Color.rgb(70, 50, 40, 0.09)));

            // Esquinas solo en las esquinas absolutas
            int row = space.getStartRow();
            int col = space.getStartCol();
            int filas = totalRows;
            int columnas = totalCols;
            double arc = 24;
            boolean esquinaSupIzq = (row == 0 && col == 0);
            boolean esquinaSupDer = (row == 0 && col == columnas - 1);
            boolean esquinaInfIzq = (row == filas - 1 && col == 0);
            boolean esquinaInfDer = (row == filas - 1 && col == columnas - 1);

            if (esquinaSupIzq || esquinaSupDer || esquinaInfIzq || esquinaInfDer) {
                rect.setArcWidth(arc);
                rect.setArcHeight(arc);
            }

            // No poner nada de texto ni tooltip, solo decoración
            return pane;
        }

        // --------- OTROS ESPACIOS -----------
        Color baseColor = getColorByType(space.getType());
        rect.setFill(space.getAvailable() ? baseColor : baseColor.darker().darker());
        rect.setStroke(Color.web("#222"));
        rect.setStrokeWidth(2);
        rect.setArcWidth(18);
        rect.setArcHeight(18);
        rect.setEffect(new DropShadow(10, Color.rgb(60, 60, 60, 0.15)));

        // Ícono según tipo
        Label icon = new Label();
        icon.setFont(Font.font("Segoe UI Symbol", 28));
        icon.setTextFill(Color.WHITE);

        switch (space.getType()) {
            case AREA_COMUN:
                icon.setText("\uD83D\uDC65"); // 👥 (personas)
                break;
            case SALA_REUNIONES:
                icon.setText("\uD83D\uDC6B"); // 👫 (pareja/personas reunión)
                break;
            case ESCRITORIO:
                icon.setText("\uD83D\uDCC4"); // 📄 (papel/escritorio)
                break;
            default:
                icon.setText("");
        }

        Label lblNombre = new Label(space.getName());
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblNombre.setTextFill(Color.WHITE);

        Label lblCapacidad = null;
        if (space.getType() != SpaceType.ESCRITORIO) {
            lblCapacidad = new Label("Cap: " + space.getCapacity());
            lblCapacidad.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            lblCapacidad.setTextFill(Color.WHITE);
        }

        VBox box = (lblCapacidad == null)
                ? new VBox(icon, lblNombre)
                : new VBox(icon, lblNombre, lblCapacidad);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(2);

        pane.getChildren().add(box);

        // Tooltip visual (solo para no-pasillos)
        String tooltipText = "Nombre: " + space.getName()
                + "\nTipo: " + getTypeName(space.getType())
                + "\nCapacidad: " + space.getCapacity()
                + "\nDisponible: " + (space.getAvailable() ? "Sí" : "No");
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(pane, tooltip);

        // Menú contextual para eliminar
        if (editable) {
            ContextMenu menu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Eliminar espacio");
            deleteItem.setOnAction(e -> {
                if (onDelete != null) {
                    onDelete.run();
                }
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

    private static String truncar(String texto, int max) {
        if (texto.length() <= max) {
            return texto;
        }
        return texto.substring(0, max - 3) + "...";
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
