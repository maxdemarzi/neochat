package com.maxdemarzi.facts;

import java.time.LocalTime;

public class TimeOfDay {

    private static final LocalTime MORNING = LocalTime.of(0, 0, 0);
    private static final LocalTime AFTER_NOON = LocalTime.of(12, 0, 0);
    private static final LocalTime EVENING = LocalTime.of(16, 0, 0);
    private static final LocalTime NIGHT = LocalTime.of(21, 0, 0);

    private LocalTime now;

    public TimeOfDay(LocalTime now) {
        this.now = now;
    }

    public String getTimeOfDay() {
        if (between(MORNING, AFTER_NOON)) {
            return("morning");
        } else if (between(AFTER_NOON, EVENING)) {
            return("afternoon");
        } else if (between(EVENING, NIGHT)) {
            return ("evening");
        } else {
            return ("night");
        }
    }

    private boolean between(LocalTime start, LocalTime end) {
        return (!now.isBefore(start)) && now.isBefore(end);
    }
}
