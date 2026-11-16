package com.eventportal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.eventportal.dao.EventDAO;
import com.eventportal.dao.ParticipantDAO;
import com.eventportal.dao.RegistrationDAO;
import com.eventportal.database.DBConnection;
import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.Event;
import com.eventportal.model.Participant;
import com.eventportal.model.Registration;
import com.eventportal.model.TechnicalEvent;
import com.eventportal.service.StudentService;
import com.eventportal.service.StudentService.DashboardStats;

/**
 * End-to-end check of the Student Portal logic against the REAL database.
 * Run it like the other Test classes (right-click > Run in VS Code).
 *
 * It creates its own temporary events and students (names start with
 * "ZZ_TEST" / "zz.test."), checks every rule, and DELETES them again at
 * the end. It does not touch any real event or student.
 *
 * Needs the Student Portal migration (sql/student_portal_migration.sql)
 * to have been run first.
 */
public class TestStudentPortal {

    private static int passed = 0;
    private static int failed = 0;

    private static final List<Integer> testParticipantIds = new ArrayList<>();

    public static void main(String[] args) {

        StudentService service = new StudentService();
        EventDAO eventDAO = new EventDAO();
        ParticipantDAO participantDAO = new ParticipantDAO();
        RegistrationDAO registrationDAO = new RegistrationDAO();

        String stamp = String.valueOf(System.currentTimeMillis());
        String emailA = "zz.test.a." + stamp + "@example.com";
        String emailB = "zz.test.b." + stamp + "@example.com";
        String emailC = "zz.test.c." + stamp + "@example.com";

        try {

            // ---------- temporary events (admin path: EventDAO) ----------
            eventDAO.addEvent(new TechnicalEvent("ZZ_TEST_Future",
                    "Temporary test event", LocalDate.now().plusDays(7),
                    LocalTime.of(10, 0), "Test Hall", 5));

            eventDAO.addEvent(new TechnicalEvent("ZZ_TEST_OneSeat",
                    "Temporary test event", LocalDate.now().plusDays(8),
                    LocalTime.of(10, 0), "Test Hall", 1));

            eventDAO.addEvent(new TechnicalEvent("ZZ_TEST_Past",
                    "Temporary test event", LocalDate.now().minusDays(3),
                    LocalTime.of(10, 0), "Test Hall", 5));

            Event future = findEvent(eventDAO, "ZZ_TEST_Future");
            Event oneSeat = findEvent(eventDAO, "ZZ_TEST_OneSeat");
            Event past = findEvent(eventDAO, "ZZ_TEST_Past");

            check("Temporary events created",
                    future != null && oneSeat != null && past != null);

            if (future == null || oneSeat == null || past == null) {
                return;
            }

            // ---------- account creation and login ----------
            Participant a = service.createAccount(
                    "Test Student A", emailA, "9000000001",
                    "password123", "password123");

            check("New account created and logged in",
                    a != null && a.getParticipantId() > 0);

            testParticipantIds.add(a.getParticipantId());

            expectFailure("Duplicate account refused",
                    () -> service.createAccount("Test Student A", emailA,
                            "9000000001", "password123", "password123"),
                    "already exists");

            check("Login with correct password works",
                    service.login(emailA, "password123")
                            .getParticipantId() == a.getParticipantId());

            expectFailure("Wrong password rejected",
                    () -> service.login(emailA, "wrongpassword"),
                    "Invalid email or password");

            expectFailure("Unknown email rejected",
                    () -> service.login("nobody." + stamp + "@example.com",
                            "password123"),
                    "Invalid email or password");

            // Password is stored hashed, never in plain text
            check("Stored password is hashed (not plain text)",
                    !storedHash(a.getParticipantId())
                            .contains("password123"));

            // ---------- validation ----------
            expectFailure("Bad email refused",
                    () -> service.createAccount("X Y", "not-an-email",
                            "9000000002", "password123", "password123"),
                    "valid email");

            expectFailure("Short password refused",
                    () -> service.createAccount("X Y", emailB,
                            "9000000002", "short", "short"),
                    "at least 8");

            expectFailure("Password mismatch refused",
                    () -> service.createAccount("X Y", emailB,
                            "9000000002", "password123", "password124"),
                    "do not match");

            expectFailure("Bad phone refused",
                    () -> service.createAccount("X Y", emailB,
                            "12ab", "password123", "password123"),
                    "10 to 15 digits");

            // ---------- participant added earlier by an admin ----------
            participantDAO.addParticipant(new Participant(
                    "Test Student B", emailB, "9000000002"));

            Participant bRecord = participantDAO.getParticipantByEmail(emailB);
            testParticipantIds.add(bRecord.getParticipantId());

            expectFailure("Admin-added student without password cannot "
                            + "log in yet",
                    () -> service.login(emailB, "password123"),
                    "no password yet");

            expectFailure("Activation with WRONG phone refused",
                    () -> service.createAccount("Test Student B", emailB,
                            "9111111111", "password123", "password123"),
                    "different details");

            Participant b = service.createAccount(
                    "Test Student B", emailB, "9000000002",
                    "password123", "password123");

            check("Activation with matching phone works and keeps the "
                            + "SAME participant id",
                    b.getParticipantId() == bRecord.getParticipantId());

            // ---------- browse and register ----------
            List<Event> events = service.getAllEvents();

            check("getAllEvents returns the events",
                    events.size() >= 3);

            check("isPast detects a finished event",
                    service.isPast(past) && !service.isPast(future));

            int seatsBefore = seatsOf(eventDAO, future.getEventId());

            service.registerForEvent(a, future);

            check("Registration reduces seats by exactly 1",
                    seatsOf(eventDAO, future.getEventId())
                            == seatsBefore - 1);

            expectFailure("Duplicate registration refused",
                    () -> service.registerForEvent(a, future),
                    "already registered");

            check("Seats unchanged after refused duplicate",
                    seatsOf(eventDAO, future.getEventId())
                            == seatsBefore - 1);

            expectFailure("Registering for a past event refused",
                    () -> service.registerForEvent(a, past),
                    "already started or finished");

            // ---------- full event ----------
            service.registerForEvent(a, oneSeat);

            Event staleCopy = findEvent(eventDAO, "ZZ_TEST_OneSeat");
            staleCopy.setAvailableSeats(1); // pretend the list is outdated

            expectFailure("Last seat taken - second student gets "
                            + "'just became full'",
                    () -> service.registerForEvent(b, staleCopy),
                    "just became full");

            Event fullNow = findEvent(eventDAO, "ZZ_TEST_OneSeat");

            expectFailure("Full event refused up front",
                    () -> service.registerForEvent(b, fullNow),
                    "No seats");

            // ---------- my registrations are private ----------
            List<Registration> mineA = service.getMyRegistrations(a);
            List<Registration> mineB = service.getMyRegistrations(b);

            check("Student A sees only their own registrations (2)",
                    mineA.size() == 2
                            && mineA.stream().allMatch(
                            r -> r.getParticipantId()
                                    == a.getParticipantId()));

            check("Student B sees none of A's registrations",
                    mineB.isEmpty());

            // ---------- cancellation safety ----------
            int futureRegId = mineA.stream()
                    .filter(r -> r.getEventId() == future.getEventId())
                    .findFirst().get().getRegistrationId();

            expectFailure("Student B cannot cancel A's registration "
                            + "(service)",
                    () -> service.cancelRegistration(b, futureRegId),
                    "not found in your account");

            check("Student B cannot cancel A's registration (DAO / SQL)",
                    !registrationDAO.cancelRegistration(
                            futureRegId, b.getParticipantId()));

            check("A's registration is still active after the attempts",
                    service.getMyRegistrations(a).stream().anyMatch(
                            r -> r.getRegistrationId() == futureRegId
                                    && "REGISTERED".equals(r.getStatus())));

            service.cancelRegistration(a, futureRegId);

            check("Own cancellation works and restores the seat",
                    seatsOf(eventDAO, future.getEventId()) == seatsBefore);

            expectFailure("Cancelling twice refused",
                    () -> service.cancelRegistration(a, futureRegId),
                    "already cancelled");

            // Admin cancel (original method) must still work as before
            int oneSeatRegId = service.getMyRegistrations(a).stream()
                    .filter(r -> r.getEventId() == oneSeat.getEventId())
                    .findFirst().get().getRegistrationId();

            check("Admin-style cancelRegistration(id) still works",
                    registrationDAO.cancelRegistration(oneSeatRegId));

            // Register again after cancelling
            boolean reRegistered;

            try {
                service.registerForEvent(a, findEvent(
                        eventDAO, "ZZ_TEST_Future"));
                reRegistered = true;
            } catch (StudentServiceException e) {
                reRegistered = false;
                System.out.println("      reason: " + e.getMessage());
            }

            check("Can register again after cancelling "
                            + "(fails if DB has UNIQUE(event,participant))",
                    reRegistered);

            // ---------- attendance blocks cancellation ----------
            int liveRegId = service.getMyRegistrations(a).stream()
                    .filter(r -> r.getEventId() == future.getEventId()
                            && "REGISTERED".equals(r.getStatus()))
                    .findFirst().get().getRegistrationId();

            runSql("UPDATE registrations SET attendance = TRUE "
                    + "WHERE registration_id = " + liveRegId);

            expectFailure("Cannot cancel after attendance is marked",
                    () -> service.cancelRegistration(a, liveRegId),
                    "Attendance is already marked");

            // ---------- dashboard ----------
            DashboardStats stats = service.getDashboardStats(a);

            check("Dashboard counts: 1 active, 1 upcoming, 1 attended, "
                            + "2 cancelled",
                    stats.activeRegistrations == 1
                            && stats.upcomingRegistrations == 1
                            && stats.attendedEvents == 1
                            && stats.cancelledRegistrations == 2);

            check("Dashboard lists are filled",
                    stats.upcomingLines.size() == 1
                            && stats.recentLines.size() == 3);

            // ---------- profile ----------
            service.updateProfile(a, "Test Student A Renamed", "9000000009");

            Participant reloaded = service.getProfile(a);

            check("Profile update saved",
                    "Test Student A Renamed".equals(reloaded.getFullName())
                            && "9000000009".equals(reloaded.getPhone()));

            check("Email unchanged by profile update",
                    emailA.equalsIgnoreCase(reloaded.getEmail()));

            expectFailure("Profile: bad phone refused",
                    () -> service.updateProfile(a, "Valid Name", "abc"),
                    "10 to 15 digits");

            expectFailure("Profile: empty name refused",
                    () -> service.updateProfile(a, "  ", "9000000009"),
                    "required");

            // account C: login after profile change still works
            Participant c = service.createAccount("Test Student C", emailC,
                    "9000000003", "password123", "password123");
            testParticipantIds.add(c.getParticipantId());

            check("Login still works for renamed student",
                    service.login(emailA, "password123")
                            .getFullName().equals("Test Student A Renamed"));

        } catch (Exception e) {

            failed++;
            System.out.println("FAIL  Unexpected exception: " + e);
            e.printStackTrace();

        } finally {

            cleanUp();

            System.out.println();
            System.out.println("=====================================");
            System.out.println(" PASSED: " + passed + "    FAILED: " + failed);
            System.out.println("=====================================");

            if (failed > 0) {
                System.exit(1);
            }
        }
    }

    // ================= helpers =================

    private interface Action {
        void run() throws Exception;
    }

    private static void check(String name, boolean condition) {

        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static void expectFailure(String name, Action action,
                                      String messagePart) {

        try {

            action.run();

            failed++;
            System.out.println("FAIL  " + name + "  (no error was thrown)");

        } catch (StudentServiceException e) {

            boolean ok = e.getMessage().contains(messagePart);

            if (ok) {
                passed++;
                System.out.println("PASS  " + name);
            } else {
                failed++;
                System.out.println("FAIL  " + name + "  (message was: "
                        + e.getMessage() + ")");
            }

        } catch (Exception e) {

            failed++;
            System.out.println("FAIL  " + name + "  (" + e + ")");
        }
    }

    private static Event findEvent(EventDAO eventDAO, String name) {

        for (Event event : eventDAO.getAllEvents()) {
            if (name.equals(event.getEventName())) {
                return event;
            }
        }

        return null;
    }

    private static int seatsOf(EventDAO eventDAO, int eventId) {

        for (Event event : eventDAO.getAllEvents()) {
            if (event.getEventId() == eventId) {
                return event.getAvailableSeats();
            }
        }

        return -1;
    }

    private static String storedHash(int participantId) throws Exception {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT password_hash FROM participants "
                             + "WHERE participant_id = ?")) {

            ps.setInt(1, participantId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }

    private static void runSql(String sql) throws Exception {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.executeUpdate();
        }
    }

    /** Removes everything this test created. */
    private static void cleanUp() {

        try {

            for (int id : testParticipantIds) {
                runSql("DELETE FROM registrations "
                        + "WHERE participant_id = " + id);
                runSql("DELETE FROM participants "
                        + "WHERE participant_id = " + id);
            }

            runSql("DELETE FROM registrations WHERE event_id IN "
                    + "(SELECT event_id FROM events "
                    + "WHERE event_name LIKE 'ZZ_TEST%')");
            runSql("DELETE FROM events WHERE event_name LIKE 'ZZ_TEST%'");

            System.out.println("Test data cleaned up.");

        } catch (Exception e) {

            System.out.println("Cleanup problem - delete rows named "
                    + "ZZ_TEST% / zz.test.% manually: " + e);
        }
    }
}
