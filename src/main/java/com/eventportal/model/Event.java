
package com.eventportal.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {

    private int eventId;
    private String eventName;
    private String description;
    private String category;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venue;
    private int totalSeats;
    private int availableSeats;

    public Event() {
    }

    public Event(String eventName, String description,
                 String category, LocalDate eventDate,
                 LocalTime eventTime, String venue,
                 int totalSeats) {

        this.eventName = eventName;
        this.description = description;
        this.category = category;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
