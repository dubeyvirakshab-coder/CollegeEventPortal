
package com.eventportal.model;

import java.time.LocalDateTime;

public class Registration implements Registrable {

    private int registrationId;
    private int eventId;
    private int participantId;
    private LocalDateTime registrationDate;
    private String status;
    private boolean attendance;

    public Registration() {
    }

    public Registration(int eventId, int participantId) {

        this.eventId = eventId;
        this.participantId = participantId;
        this.registrationDate = LocalDateTime.now();
        this.status = "REGISTERED";
        this.attendance = false;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAttendance() {
        return attendance;
    }

    public void setAttendance(boolean attendance) {
        this.attendance = attendance;
    }

    @Override
    public void register() {
        this.status = "REGISTERED";
    }

    @Override
    public void cancel() {
        this.status = "CANCELLED";
    }
}
