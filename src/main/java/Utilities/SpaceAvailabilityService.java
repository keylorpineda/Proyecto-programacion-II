package Utilities;

import Models.Room;
import Models.Space;
import Models.Reservation;
import Services.ReservationService;
import Services.RoomService;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class SpaceAvailabilityService {

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();


    public List<Reservation> getReservationsForSpace(Long spaceId, LocalDate date, LocalTime start, LocalTime end) {
        return reservationService.findBySpaceAndDateAndTime(spaceId, date, start, end);
    }


    public List<Space> getOccupiedSpaces(Long roomId, LocalDate date, LocalTime start, LocalTime end) {
        List<Reservation> reservas = reservationService.findByRoomAndDateAndTime(roomId, date, start, end);
        return reservas.stream()
                .map(Reservation::getSpace)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Space> getFreeSpaces(Room room, LocalDate date, LocalTime start, LocalTime end) {
        List<Space> ocupados = getOccupiedSpaces(room.getId(), date, start, end);
        return room.getSpaces().stream()
                .filter(space -> !ocupados.contains(space))
                .collect(Collectors.toList());
    }

    public List<LocalTime[]> getOccupiedTimeRangesForSpace(Long spaceId, LocalDate date) {
        List<Reservation> reservas = reservationService.findBySpaceAndDate(spaceId, date);
        List<LocalTime[]> result = new ArrayList<>();
        for (Reservation r : reservas) {
            result.add(new LocalTime[]{r.getStartTime().toLocalTime(), r.getEndTime().toLocalTime()});
        }
        return result;
    }

    public List<LocalTime[]> suggestAvailableTimeRanges(Long spaceId, LocalDate date, int durationMinutes) {
        List<LocalTime[]> ocupadas = getOccupiedTimeRangesForSpace(spaceId, date);
        List<LocalTime[]> libres = new ArrayList<>();
        LocalTime start = LocalTime.of(8, 0), end = LocalTime.of(18, 0);
        LocalTime actual = start;
        while (actual.plusMinutes(durationMinutes).isBefore(end) || actual.plusMinutes(durationMinutes).equals(end)) {
            boolean overlap = false;
            LocalTime next = actual.plusMinutes(durationMinutes);
            for (LocalTime[] range : ocupadas) {
                if (!(next.isBefore(range[0]) || actual.isAfter(range[1]))) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) {
                libres.add(new LocalTime[]{actual, next});
            }
            actual = actual.plusMinutes(30); 
        }
        return libres;
    }

    public double getOccupancyRateForRoom(Long roomId, LocalDate date, LocalTime start, LocalTime end) {
        Room room = roomService.findByIdWithSpaces(roomId);
        if (room == null) {
            return 0.0;
        }
        int total = room.getSpaces().size();
        int ocupados = getOccupiedSpaces(roomId, date, start, end).size();
        return total == 0 ? 0.0 : (double) ocupados / total;
    }

    public Map<Space, List<LocalTime[]>> getSpacesOccupiedTimeRangesForRoom(Long roomId, LocalDate date) {
        Room room = roomService.findByIdWithSpaces(roomId);
        Map<Space, List<LocalTime[]>> map = new HashMap<>();
        if (room != null) {
            for (Space s : room.getSpaces()) {
                map.put(s, getOccupiedTimeRangesForSpace(s.getId(), date));
            }
        }
        return map;
    }

    public boolean isSpaceAvailable(Long spaceId, LocalDate date, LocalTime start, LocalTime end) {
        List<Reservation> reservas = reservationService.findBySpaceAndDateAndTime(spaceId, date, start, end);
        return reservas.isEmpty();
    }

}
