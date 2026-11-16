package com.eventportal.preview;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.CulturalEvent;
import com.eventportal.model.Event;
import com.eventportal.model.Participant;
import com.eventportal.model.Registration;
import com.eventportal.model.SportsEvent;
import com.eventportal.model.TechnicalEvent;
import com.eventportal.service.StudentService;
import com.eventportal.ui.BrowseEventsFrame;
import com.eventportal.ui.MyRegistrationsFrame;
import com.eventportal.ui.StudentDashboardFrame;
import com.eventportal.ui.StudentLoginFrame;
import com.eventportal.ui.StudentProfileFrame;

/**
 * ===================== DEMO / PREVIEW ONLY =====================
 *
 * Lets you SEE and click through the Student Portal screens WITHOUT a
 * database. All data below is fake and lives only in memory.
 *
 * This is NOT part of the real application. The real application
 * (Main.java -> StudentLoginFrame) uses StudentService with the real
 * MySQL database. This file lives in src/test/java on purpose so it is
 * kept apart from the real code.
 *
 * How to run (VS Code): open this file and click "Run" above main().
 *
 *   Demo login:  demo@example.com   password: demo12345
 *
 * Optional argument to jump straight to a screen:
 *   login (default) | dashboard | browse | registrations | profile
 */
public class StudentUiPreview {

    public static final String DEMO_EMAIL = "demo@example.com";
    public static final String DEMO_PASSWORD = "demo12345";

    public static void main(String[] args) {

        String screen = args.length > 0 ? args[0] : "login";

        SwingUtilities.invokeLater(() -> {

            DemoStudentService service = new DemoStudentService();
            Participant student = service.demoStudent;

            switch (screen) {
                case "dashboard":
                    new StudentDashboardFrame(student, service)
                            .setVisible(true);
                    break;
                case "browse":
                    new BrowseEventsFrame(student, service)
                            .setVisible(true);
                    break;
                case "registrations":
                    new MyRegistrationsFrame(student, service)
                            .setVisible(true);
                    break;
                case "profile":
                    new StudentProfileFrame(student, service)
                            .setVisible(true);
                    break;
                default:
                    new StudentLoginFrame(service);
                    break;
            }
        });
    }

    /**
     * A StudentService that keeps everything in memory instead of
     * calling the database. The rules (can I register? can I cancel?)
     * are inherited from the real StudentService, so the preview
     * behaves like the real thing.
     */
    public static class DemoStudentService extends StudentService {

        public final Participant demoStudent = new Participant(
                "Demo Student", DEMO_EMAIL, "9876543210");

        private final List<Event> events = new ArrayList<>();
        private final List<Registration> registrations = new ArrayList<>();
        private int nextRegistrationId = 100;

        public DemoStudentService() {

            demoStudent.setParticipantId(1);

            addEvent(new TechnicalEvent("Hackathon 2026",
                    "24-hour coding competition open to all branches. "
                            + "Teams of up to four. Bring your laptop.",
                    LocalDate.now().plusDays(10), LocalTime.of(9, 0),
                    "Main Auditorium", 100), 1, 62);

            addEvent(new SportsEvent("Inter-College Cricket",
                    "Knockout cricket tournament between college teams.",
                    LocalDate.now().plusDays(20), LocalTime.of(8, 30),
                    "College Ground", 22), 2, 9);

            addEvent(new CulturalEvent("Cultural Night",
                    "Music, dance and drama performances by students.",
                    LocalDate.now().plusDays(30), LocalTime.of(18, 0),
                    "Open Air Theatre", 150), 3, 150);

            addEvent(new TechnicalEvent("AI Workshop",
                    "Hands-on introduction to machine learning.",
                    LocalDate.now().plusDays(5), LocalTime.of(14, 0),
                    "Computer Lab 2", 40), 4, 0);

            addEvent(new SportsEvent("Annual Marathon",
                    "5 km run around the campus.",
                    LocalDate.now().plusDays(45), LocalTime.of(6, 30),
                    "Main Gate", 200), 5, 140);

            addEvent(new CulturalEvent("Freshers' Welcome",
                    "Welcome party for the new batch.",
                    LocalDate.now().minusDays(12), LocalTime.of(17, 0),
                    "Auditorium", 120), 6, 20);

            // Pre-existing demo registrations
            addRegistration(1, 6, "REGISTERED", true, 30);
            addRegistration(3, 3, "REGISTERED", false, 2);
            addRegistration(5, 1, "CANCELLED", false, 1);
        }

        private void addEvent(Event event, int id, int seatsLeft) {

            event.setEventId(id);
            event.setAvailableSeats(seatsLeft);
            events.add(event);
        }

        private void addRegistration(int id, int eventId, String status,
                                     boolean attended, int daysAgo) {

            Registration registration = new Registration();

            registration.setRegistrationId(id);
            registration.setEventId(eventId);
            registration.setParticipantId(demoStudent.getParticipantId());
            registration.setStatus(status);
            registration.setAttendance(attended);
            registration.setRegistrationDate(
                    LocalDateTime.now().minusDays(daysAgo));

            registrations.add(registration);
        }

        // ---------- overrides: in-memory instead of database ----------

        @Override
        public Participant login(String email, String password)
                throws StudentServiceException {

            if (DEMO_EMAIL.equalsIgnoreCase(clean(email))
                    && DEMO_PASSWORD.equals(password)) {
                return demoStudent;
            }

            throw new StudentServiceException(
                    "Invalid email or password.\n\n(Preview mode - use "
                            + DEMO_EMAIL + " / " + DEMO_PASSWORD + ")");
        }

        @Override
        public Participant createAccount(String fullName, String email,
                                         String phone, String password,
                                         String confirmPassword)
                throws StudentServiceException {

            validateName(clean(fullName));
            validatePhone(clean(phone));

            if (password == null || password.length() < 8
                    || !password.equals(confirmPassword)) {
                throw new StudentServiceException(
                        "Password must be at least 8 characters and "
                                + "match the confirmation.");
            }

            demoStudent.setFullName(clean(fullName));
            demoStudent.setEmail(clean(email));
            demoStudent.setPhone(clean(phone));

            return demoStudent;
        }

        @Override
        public List<Event> getAllEvents() {

            List<Event> copy = new ArrayList<>(events);

            copy.sort(Comparator.comparing(Event::getEventDate));

            return copy;
        }

        @Override
        public void registerForEvent(Participant student, Event event)
                throws StudentServiceException {

            Event real = find(event.getEventId());

            boolean already = registrations.stream().anyMatch(r ->
                    r.getEventId() == real.getEventId()
                            && STATUS_REGISTERED.equals(r.getStatus()));

            String reason = getRegistrationBlockReason(real, already);

            if (reason != null) {
                throw new StudentServiceException(reason);
            }

            real.setAvailableSeats(real.getAvailableSeats() - 1);

            addRegistration(nextRegistrationId++, real.getEventId(),
                    STATUS_REGISTERED, false, 0);
        }

        @Override
        public List<Registration> getMyRegistrations(Participant student) {

            List<Registration> copy = new ArrayList<>(registrations);

            copy.sort(Comparator.comparing(
                    Registration::getRegistrationDate).reversed());

            return copy;
        }

        @Override
        public Map<Integer, Event> getEventsById() {

            Map<Integer, Event> map = new HashMap<>();

            for (Event event : events) {
                map.put(event.getEventId(), event);
            }

            return map;
        }

        @Override
        public void cancelRegistration(Participant student,
                                       int registrationId)
                throws StudentServiceException {

            for (Registration registration : registrations) {

                if (registration.getRegistrationId() != registrationId) {
                    continue;
                }

                Event event = find(registration.getEventId());

                String reason = getCancelBlockReason(registration, event);

                if (reason != null) {
                    throw new StudentServiceException(reason);
                }

                registration.setStatus(STATUS_CANCELLED);
                event.setAvailableSeats(event.getAvailableSeats() + 1);
                return;
            }

            throw new StudentServiceException(
                    "That registration was not found in your account.");
        }

        @Override
        public Participant getProfile(Participant student) {
            return student;
        }

        @Override
        public void updateProfile(Participant student, String fullName,
                                  String phone)
                throws StudentServiceException {

            validateName(clean(fullName));
            validatePhone(clean(phone));

            student.setFullName(clean(fullName));
            student.setPhone(clean(phone));
        }

        private Event find(int eventId) {

            for (Event event : events) {
                if (event.getEventId() == eventId) {
                    return event;
                }
            }

            throw new IllegalArgumentException("Unknown event " + eventId);
        }
    }
}
