package Utilities;

import Models.Room;
import Models.Space;
import Models.SpaceType;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.Optional;
import java.util.function.BiPredicate;

public class AddSpaceHelper {

    private boolean placing = false;
    private final Room room;
    private final GridPane gridPlano;
    private final OnSpaceAddedListener listener;
    private final BiPredicate<Integer, Integer> celdaDisponible;

    public interface OnSpaceAddedListener {
        void onSpaceAdded(Space newSpace);
    }

    public AddSpaceHelper(Room room, GridPane gridPlano, OnSpaceAddedListener listener, BiPredicate<Integer, Integer> celdaDisponible) {
        this.room = room;
        this.gridPlano = gridPlano;
        this.listener = listener;
        this.celdaDisponible = celdaDisponible;
    }

    public void startPlacing() {
        placing = true;
    }

    public void setupCellClick(StackPane celda, int row, int col) {
        celda.setOnMouseEntered(e -> {
            if (placing) {
                celda.setStyle("-fx-background-color: #c7e7ff; -fx-border-color: #2874A6;");
            }
        });
        celda.setOnMouseExited(e -> {
            if (placing) {
                celda.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #cccccc;");
            }
        });
        celda.setOnMouseClicked(event -> {
            if (placing) {
                abrirDialogoNuevoEspacio(row, col);
            }
        });
    }

    private void abrirDialogoNuevoEspacio(int fila, int columna) {
        try {
            // Elegir tipo de espacio
            ChoiceDialog<SpaceType> tipoDialog = new ChoiceDialog<>(SpaceType.ESCRITORIO, SpaceType.values());
            tipoDialog.setTitle("Agregar Espacio");
            tipoDialog.setHeaderText("Selecciona el tipo de espacio a agregar");
            tipoDialog.setContentText("Tipo:");
            Optional<SpaceType> tipoResult = tipoDialog.showAndWait();
            if (!tipoResult.isPresent()) {
                placing = false;
                return;
            }
            SpaceType tipo = tipoResult.get();

            int width = 1, height = 1;
            switch (tipo) {
                case ESCRITORIO:
                    width = 1;
                    height = 1;
                    break;
                case AREA_COMUN:
                    width = 2;
                    height = 2;
                    break;
                case SALA_REUNIONES:
                    width = 3;
                    height = 2;
                    break;
                case PASILLO:
                    width = 1;
                    height = 1;
                    break;
            }

            // Validar disponibilidad
            if (!espacioDisponible(fila, columna, width, height)) {
                mostrarAlerta("No se puede agregar", "El espacio se sale del plano o se superpone con otro.", "Intenta otra posición.");
                placing = false;
                return;
            }

            // Pedir nombre
            TextInputDialog nombreDialog = new TextInputDialog(tipo.toString());
            nombreDialog.setTitle("Nombre");
            nombreDialog.setHeaderText("Ponle un nombre al espacio:");
            nombreDialog.setContentText("Nombre:");
            Optional<String> nombreResult = nombreDialog.showAndWait();
            if (!nombreResult.isPresent() || nombreResult.get().trim().isEmpty()) {
                mostrarAlerta("Nombre vacío", null, "El espacio debe tener un nombre.");
                placing = false;
                return;
            }
            String nombre = nombreResult.get().trim();

            // Pedir capacidad solo si es necesario
            int capacidad = 1;
            if (tipo != SpaceType.ESCRITORIO && tipo != SpaceType.PASILLO) {
                TextInputDialog capDialog = new TextInputDialog("1");
                capDialog.setTitle("Capacidad");
                capDialog.setHeaderText("Ingresa la capacidad del espacio:");
                capDialog.setContentText("Capacidad:");
                Optional<String> capResult = capDialog.showAndWait();
                if (!capResult.isPresent()) {
                    placing = false;
                    return;
                }
                try {
                    capacidad = Math.max(1, Integer.parseInt(capResult.get().trim()));
                } catch (NumberFormatException ex) {
                    mostrarAlerta("Capacidad inválida", null, "Debes ingresar un número entero mayor o igual a 1.");
                    placing = false;
                    return;
                }
            }

            // Crear el espacio y mostrar info (debug)
            Space nuevo = new Space(nombre, fila, columna, width, height, capacidad, tipo, room);
            System.out.println("[DEBUG] Nuevo espacio: nombre=" + nombre + ", fila=" + fila + ", columna=" + columna +
                    ", ancho=" + width + ", alto=" + height + ", capacidad=" + capacidad + ", tipo=" + tipo);

            // Avisar al listener
            if (listener != null) {
                listener.onSpaceAdded(nuevo);
            }
        } finally {
            placing = false;
        }
    }

    private boolean espacioDisponible(int fila, int columna, int ancho, int alto) {
        if (fila < 0 || columna < 0) {
            return false;
        }
        if (fila + alto > room.getRows() || columna + ancho > room.getCols()) {
            return false;
        }
        for (int f = fila; f < fila + alto; f++) {
            for (int c = columna; c < columna + ancho; c++) {
                if (!celdaDisponible.test(f, c)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void mostrarAlerta(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        if (header != null) alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public boolean isPlacing() {
        return placing;
    }

    public Room getRoom() {
        return room;
    }
}