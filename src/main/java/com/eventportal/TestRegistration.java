
package com.eventportal;

import java.util.List;

import com.eventportal.dao.EventDAO;
import com.eventportal.dao.ParticipantDAO;
import com.eventportal.dao.RegistrationDAO;
import com.eventportal.exception.EventFullException;
import com.eventportal.model.Event;
import com.eventportal.model.Participant;

public class TestRegistration {

    public static void main(String[] args) {

        EventDAO eventDAO = new EventDAO();
        ParticipantDAO participantDAO = new ParticipantDAO();
        RegistrationDAO registrationDAO = new RegistrationDAO();

        // Step 1: Get available events
        List<Event> events = eventDAO.getAllEvents();

        if (events.isEmpty()) {
            System.out.println("No events available!");
            return;
        }

        Event event = events.get(0);

        System.out.println("Selected Event: " + event.getEventName());

        // Step 2: Create or find participant
        String email = "student1@example.com";

        Participant participant =
                participantDAO.getParticipantByEmail(email);

        if (participant == null) {

            participant = new Participant(
                    "Rahul Sharma",
                    email,
                    "9876543210"
            );

            boolean added =
                    participantDAO.addParticipant(participant);

            if (!added) {
                System.out.println("Participant creation failed!");
                return;
            }

            participant =
                    participantDAO.getParticipantByEmail(email);
        }

        if (participant == null) {
            System.out.println("Participant not found!");
            return;
        }

        System.out.println("Participant: " + participant.getFullName());

        // Step 3: Register participant
        try {

            boolean registered = registrationDAO.registerParticipant(
                    event.getEventId(),
                    participant.getParticipantId()
            );

            if (registered) {
                System.out.println("Participant registered successfully!");
            } else {
                System.out.println("Registration failed!");
            }

        } catch (EventFullException e) {

            System.out.println(e.getMessage());
        }
    }
}
