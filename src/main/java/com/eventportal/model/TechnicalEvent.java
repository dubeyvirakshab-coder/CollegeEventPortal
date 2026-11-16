
package com.eventportal.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class TechnicalEvent extends Event {

    public TechnicalEvent(String eventName, String description,
                          LocalDate eventDate, LocalTime eventTime,
                          String venue, int totalSeats) {

        super(eventName, description, "Technical",
                eventDate, eventTime, venue, totalSeats);
    }
}
