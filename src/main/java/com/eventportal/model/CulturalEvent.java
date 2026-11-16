
package com.eventportal.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class CulturalEvent extends Event {

    public CulturalEvent(String eventName, String description,
                         LocalDate eventDate, LocalTime eventTime,
                         String venue, int totalSeats) {

        super(eventName, description, "Cultural",
                eventDate, eventTime, venue, totalSeats);
    }
}
