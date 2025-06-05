package Utilities;

import Models.Room;
import Models.Space;
import Models.SpaceType;
import Services.RoomService;
import Services.SpaceService;

import java.util.List;

public class DataInitializer {

    public static void initializeIfNeeded() {
        RoomService roomService = new RoomService();
        SpaceService spaceService = new SpaceService();

        List<Room> rooms = roomService.findAll();
        if (!rooms.isEmpty()) {
            return;
        }

        int totalPisos = 3;
        int filas = 8;
        int columnas = 12;

        for (int p = 1; p <= totalPisos; p++) {
            Room room = new Room("Piso " + p, p, filas, columnas);
            room = roomService.save(room);

            for (int i = 0; i < filas; i++) {
                spaceService.update(new Space("Pasillo", i, 0, 1, 1, 1, SpaceType.PASILLO, room));
                spaceService.update(new Space("Pasillo", i, columnas - 1, 1, 1, 1, SpaceType.PASILLO, room));
            }
            for (int j = 0; j < columnas; j++) {
                spaceService.update(new Space("Pasillo", 0, j, 1, 1, 1, SpaceType.PASILLO, room));
                spaceService.update(new Space("Pasillo", filas - 1, j, 1, 1, 1, SpaceType.PASILLO, room));
            }

            for (int i = 1; i < filas - 1; i++) {
                spaceService.update(new Space("Pasillo", i, columnas / 2, 1, 1, 1, SpaceType.PASILLO, room));
            }
        }
    }
}
