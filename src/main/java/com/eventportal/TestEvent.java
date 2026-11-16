
package com.eventportal;

import java.time.LocalDate;
import java.time.LocalTime;

import com.eventportal.dao.EventDAO;
import com.eventportal.model.Event;

public class TestEvent {

    public static void main(String[] args) {

        EventDAO eventDAO = new EventDAO();

        // Create a sample event
        Event event = new Event(
                "Java Coding Competition",
                "A coding competition for college students",
                "Technical",
                LocalDate.now().plusDays(7),
                LocalTime.of(10, 0),
                "College Auditorium",
                50
        );

        // Save event
        boolean added = eventDAO.addEvent(event);

        if (added) {
            System.out.println("Event added successfully!");
        } else {
            System.out.println("Event could not be added.");
        }

        // Retrieve all events
        System.out.println("\nALL EVENTS:");

        for (Event e : eventDAO.getAllEvents()) {

            System.out.println("----------------------");
            System.out.println("ID: " + e.getEventId());
            System.out.println("Name: " + e.getEventName());
            System.out.println("Category: " + e.getCategory());
            System.out.println("Venue: " + e.getVenue());
            System.out.println("Available Seats: " +
                    e.getAvailableSeats());
        }
    }
}
