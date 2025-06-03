package Utilities;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimeSlots {

    public static List<String> getDefaultTimeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(8, 0);   
        LocalTime end = LocalTime.of(18, 0);  

        while (!start.isAfter(end.minusMinutes(30))) {
            slots.add(start.toString()); 
            start = start.plusMinutes(30);
        }
        return slots;
    }

    public static LocalTime parse(String s) {
        return LocalTime.parse(s); 
    }
}
