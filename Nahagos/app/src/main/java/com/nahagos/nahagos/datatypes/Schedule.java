package com.nahagos.nahagos.datatypes;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.*;

public class Schedule {

    public enum Weekday {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }

    private final EnumMap<Weekday, List<Line>> scheduleMap = new EnumMap<>(Weekday.class);

    public List<Line> getLinesForDay(Weekday day) {
        return scheduleMap.getOrDefault(day, Collections.emptyList());
    }

    public void setLinesForDay(Weekday day, List<Line> lines) {
        scheduleMap.put(day, lines);
    }

    @Override
    public String toString() {
        return "Schedule{" + "scheduleMap=" + scheduleMap + '}';
    }

    // Custom Deserializer
    public static JsonDeserializer<Schedule> getDeserializer() {
        return (JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) -> {
            Schedule schedule = new Schedule();
            JsonObject jsonObject = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                Weekday day = Weekday.valueOf(entry.getKey().toUpperCase());
                List<Line> lines = Arrays.asList(context.deserialize(entry.getValue(), Line[].class));
                schedule.setLinesForDay(day, lines);
            }

            return schedule;
        };
    }
}
