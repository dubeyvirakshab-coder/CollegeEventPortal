
package com.eventportal.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class SportsEvent extends Event {

    public SportsEvent(String eventName, String description,
                       LocalDate eventDate, LocalTime eventTime,
                       String venue, int totalSeats) {

        super(eventName, description, "Sports",
                eventDate, eventTime, venue, totalSeats);
    }
}
