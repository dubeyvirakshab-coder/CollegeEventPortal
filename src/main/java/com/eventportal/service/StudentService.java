package com.eventportal.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eventportal.dao.EventDAO;
import com.eventportal.dao.ParticipantDAO;
import com.eventportal.dao.RegistrationDAO;
import com.eventportal.dao.StudentDAO;
import com.eventportal.exception.EventFullException;
import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.Event;
import com.eventportal.model.Participant;
import com.eventportal.model.Registration;

/**
 * All Student Portal business logic lives here.
 *
 * The UI classes (frames) only show things and call these methods.
 * The DAO classes only talk to the database.
 * This class sits in the middle: it validates input, applies the rules
 * (no duplicates, no past events, only my own registrations...) and
 * turns database problems into messages a student can understand.
 *
 * Registration and cancellation reuse the EXISTING transaction logic
 * in RegistrationDAO, so seats are updated exactly like the Admin Portal.
 */
public class StudentService {

    // Same validation rules as ParticipantManagementFrame (Admin Portal)
    private static final String NAME_PATTERN = "[\\p{L} .'-]+";
    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_PATTERN = "[0-9]{10,15}";

    // Same minimum as the Admin account setup
    private static final int MIN_PASSWORD_LENGTH = 8;

    public static final String STATUS_REGISTERED = "REGISTERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private static final String DB_DOWN =
            "Could not reach the database. Please check the connection "
                    + "and try again.";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final StudentDAO studentDAO;
    private final ParticipantDAO participantDAO;
    private final EventDAO eventDAO;
    private final RegistrationDAO registrationDAO;

    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.participantDAO = new ParticipantDAO();
        this.eventDAO = new EventDAO();
        this.registrationDAO = new RegistrationDAO();
    }

    // =====================================================================
    // RESULT HOLDER FOR THE DASHBOARD
    // =====================================================================

    /** Numbers and short lists shown on the Student Dashboard. */
    public static class DashboardStats {

        public int activeRegistrations;
        public int upcomingRegistrations;
        public int attendedEvents;
        public int cancelledRegistrations;

        public List<String> upcomingLines = new ArrayList<>();
        public List<String> recentLines = new ArrayList<>();
    }

    // =====================================================================
    // LOGIN AND ACCOUNT CREATION
    // =====================================================================

    /**
     * Student login. Returns the logged-in Participant.
     */
    public Participant login(String email, String password)
            throws StudentServiceException {

        email = clean(email);

        if (email.isEmpty() || password == null || password.isEmpty()) {
            throw new StudentServiceException(
                    "Please enter both email and password.");
        }

        try {

            Boolean state = studentDAO.getPasswordState(email);

            if (state == null) {
                throw new StudentServiceException(
                        "Invalid email or password.");
            }

            if (!state) {
                throw new StudentServiceException(
                        "This email has no password yet. Click "
                                + "\"Create Student Account\" to set one.");
            }

            Participant student = studentDAO.authenticate(email, password);

            if (student == null) {
                throw new StudentServiceException(
                        "Invalid email or password.");
            }

            return student;

        } catch (SQLException | RuntimeException e) {
            throw dbProblem(e);
        }
    }

    /**
     * Creates a student account.
     *
     * Case 1 - email is not in the database: a new participant is created.
     * Case 2 - email exists (added earlier by an admin) but has no password:
     *          the student proves it is them by entering the SAME phone
     *          number that is stored for that record, then sets a password.
     * Case 3 - email already has a password: refused.
     */
    public Participant createAccount(String fullName, String email,
                                     String phone, String password,
                                     String confirmPassword)
            throws StudentServiceException {

        fullName = clean(fullName);
        email = clean(email);
        phone = clean(phone);

        validateName(fullName);
        validateEmail(email);
        validatePhone(phone);
        validateNewPassword(password, confirmPassword);

        try {

            Boolean state = studentDAO.getPasswordState(email);

            if (state != null && state) {
                throw new StudentServiceException(
                        "An account with this email already exists. "
                                + "Please log in instead.");
            }

            if (state == null) {

                // Case 1: brand-new participant
                boolean created = studentDAO.createStudentAccount(
                        fullName, email, phone, password);

                if (!created) {
                    throw new StudentServiceException(
                            "Account could not be created. Please try again.");
                }

            } else {

                // Case 2: participant exists without a password
                Participant existing =
                        participantDAO.getParticipantByEmail(email);

                if (existing == null
                        || !phone.equals(clean(existing.getPhone()))) {

                    throw new StudentServiceException(
                            "This email is already registered with "
                                    + "different details. Enter the phone "
                                    + "number the college has on record, or "
                                    + "contact the event administrator.");
                }

                boolean updated = studentDAO.setInitialPassword(
                        existing.getParticipantId(), password);

                if (!updated) {
                    throw new StudentServiceException(
                            "Account could not be activated. "
                                    + "Please try logging in.");
                }
            }

            Participant student = studentDAO.authenticate(email, password);

            if (student == null) {
                throw new StudentServiceException(
                        "Account was created but login failed. "
                                + "Please log in manually.");
            }

            return student;

        } catch (SQLException | RuntimeException e) {
            throw dbProblem(e);
        }
    }

    // =====================================================================
    // EVENTS
    // =====================================================================

    /** All events, soonest first. */
    public List<Event> getAllEvents() throws StudentServiceException {

        try {

            List<Event> events = new ArrayList<>(eventDAO.getAllEvents());

            events.sort(Comparator
                    .comparing(Event::getEventDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Event::getEventTime,
                            Comparator.nullsLast(Comparator.naturalOrder())));

            return events;

        } catch (RuntimeException e) {
            // EventDAO does not catch a missing connection itself
            throw dbProblem(e);
        }
    }

    /** Distinct category names found in the given events. */
    public List<String> getCategories(List<Event> events) {

        Set<String> categories = new LinkedHashSet<>();

        for (Event event : events) {
            if (event.getCategory() != null
                    && !event.getCategory().isBlank()) {
                categories.add(event.getCategory());
            }
        }

        return new ArrayList<>(categories);
    }

    /** Has the event already started (or finished)? */
    public boolean isPast(Event event) {

        if (event.getEventDate() == null) {
            return false;
        }

        LocalDateTime start = event.getEventTime() == null
                ? event.getEventDate().atTime(23, 59)
                : event.getEventDate().atTime(event.getEventTime());

        return start.isBefore(LocalDateTime.now());
    }

    // =====================================================================
    // REGISTRATION
    // =====================================================================

    /**
     * Tells the UI why this student cannot register for the event right now.
     * Returns null when registration is allowed.
     * (Used to enable or disable the Register button.)
     */
    public String getRegistrationBlockReason(Event event,
                                             boolean alreadyRegistered) {

        if (alreadyRegistered) {
            return "You are already registered for this event.";
        }

        if (isPast(event)) {
            return "This event has already started or finished.";
        }

        if (event.getAvailableSeats() <= 0) {
            return "No seats are available for this event.";
        }

        return null;
    }

    /**
     * Registers the logged-in student for an event.
     * The participant ID always comes from the logged-in student,
     * never from anything typed in the UI.
     */
    public void registerForEvent(Participant student, Event event)
            throws StudentServiceException {

        if (student == null || event == null) {
            throw new StudentServiceException(
                    "Please select an event first.");
        }

        try {

            boolean already = registrationDAO.hasActiveRegistration(
                    event.getEventId(), student.getParticipantId());

            String reason = getRegistrationBlockReason(event, already);

            if (reason != null) {
                throw new StudentServiceException(reason);
            }

            boolean registered = registrationDAO.registerParticipant(
                    event.getEventId(), student.getParticipantId());

            if (!registered) {
                throw new StudentServiceException(
                        "Registration could not be completed. "
                                + "Please try again.");
            }

        } catch (EventFullException e) {

            // Someone else took the last seat after the list was loaded
            throw new StudentServiceException(
                    "Sorry, this event just became full.");

        } catch (SQLException | RuntimeException e) {
            throw dbProblem(e);
        }
    }

    // =====================================================================
    // MY REGISTRATIONS
    // =====================================================================

    /** Only THIS student's registrations, newest first. */
    public List<Registration> getMyRegistrations(Participant student)
            throws StudentServiceException {

        try {
            return registrationDAO.getRegistrationsByParticipant(
                    student.getParticipantId());

        } catch (SQLException | RuntimeException e) {
            throw dbProblem(e);
        }
    }

    /** Event ID -> Event, used to show event names and dates. */
    public Map<Integer, Event> getEventsById()
            throws StudentServiceException {

        Map<Integer, Event> map = new HashMap<>();

        for (Event event : getAllEvents()) {
            map.put(event.getEventId(), event);
        }

        return map;
    }

    /**
     * Tells the UI why a registration cannot be cancelled.
     * Returns null when cancellation is allowed.
     *
     * Rules: the registration must still be active, the event must not
     * have started, and attendance must not already be marked.
     */
    public String getCancelBlockReason(Registration registration,
                                       Event event) {

        if (registration == null) {
            return "Select a registration first.";
        }

        if (!STATUS_REGISTERED.equals(registration.getStatus())) {
            return "This registration is already cancelled.";
        }

        if (registration.isAttendance()) {
            return "Attendance is already marked for this event.";
        }

        if (event != null && isPast(event)) {
            return "This event has already started or finished.";
        }

        return null;
    }

    /**
     * Cancels one of the logged-in student's registrations.
     *
     * Safety: the registration is looked up among THIS student's own
     * registrations first, and the database update also checks the owner,
     * so changing an ID cannot cancel another student's booking.
     */
    public void cancelRegistration(Participant student, int registrationId)
            throws StudentServiceException {

        List<Registration> mine = getMyRegistrations(student);

        Registration target = null;

        for (Registration registration : mine) {
            if (registration.getRegistrationId() == registrationId) {
                target = registration;
                break;
            }
        }

        if (target == null) {
            throw new StudentServiceException(
                    "That registration was not found in your account.");
        }

        Event event = getEventsById().get(target.getEventId());

        String reason = getCancelBlockReason(target, event);

        if (reason != null) {
            throw new StudentServiceException(reason);
        }

        try {

            boolean cancelled = registrationDAO.cancelRegistration(
                    registrationId, student.getParticipantId());

            if (!cancelled) {
                throw new StudentServiceException(
                        "The registration could not be cancelled. "
                                + "Please try again.");
            }

        } catch (RuntimeException e) {
            throw dbProblem(e);
        }
    }

    // =====================================================================
    // DASHBOARD
    // =====================================================================

    public DashboardStats getDashboardStats(Participant student)
            throws StudentServiceException {

        List<Registration> mine = getMyRegistrations(student);
        Map<Integer, Event> events = getEventsById();

        DashboardStats stats = new DashboardStats();

        List<Registration> upcoming = new ArrayList<>();

        for (Registration registration : mine) {

            Event event = events.get(registration.getEventId());

            if (STATUS_CANCELLED.equals(registration.getStatus())) {
                stats.cancelledRegistrations++;
                continue;
            }

            stats.activeRegistrations++;

            if (registration.isAttendance()) {
                stats.attendedEvents++;
            }

            if (event != null && !isPast(event)) {
                stats.upcomingRegistrations++;
                upcoming.add(registration);
            }
        }

        // Soonest upcoming events first (max 5)
        upcoming.sort(Comparator.comparing(
                (Registration r) -> events.get(r.getEventId()).getEventDate())
                .thenComparing(r -> events.get(r.getEventId()).getEventTime(),
                        Comparator.nullsLast(Comparator.naturalOrder())));

        for (Registration registration : upcoming) {

            if (stats.upcomingLines.size() == 5) {
                break;
            }

            Event event = events.get(registration.getEventId());

            stats.upcomingLines.add(
                    event.getEventName() + "  |  "
                            + formatDate(event.getEventDate()) + "  |  "
                            + (event.getEventTime() == null
                            ? "" : event.getEventTime().toString())
            );
        }

        // Most recent registrations first (list is already newest first)
        for (Registration registration : mine) {

            if (stats.recentLines.size() == 5) {
                break;
            }

            Event event = events.get(registration.getEventId());

            String name = event == null
                    ? "Event #" + registration.getEventId()
                    : event.getEventName();

            stats.recentLines.add(
                    name + "  |  " + prettyStatus(registration.getStatus())
            );
        }

        return stats;
    }

    // =====================================================================
    // PROFILE
    // =====================================================================

    /** Loads fresh profile data from the database. */
    public Participant getProfile(Participant student)
            throws StudentServiceException {

        try {

            Participant fresh = participantDAO.getParticipantById(
                    student.getParticipantId());

            if (fresh == null) {
                throw new StudentServiceException(
                        "Your profile could not be found.");
            }

            return fresh;

        } catch (SQLException | RuntimeException e) {
            throw dbProblem(e);
        }
    }

    /**
     * Saves a new name and phone number. The email is not editable because
     * it is the student's login. The logged-in Participant object is
     * updated so the rest of the portal shows the new name immediately.
     */
    public void updateProfile(Participant student, String fullName,
                              String phone) throws StudentServiceException {

        fullName = clean(fullName);
        phone = clean(phone);

        validateName(fullName);
        validatePhone(phone);

        try {

            participantDAO.updateProfile(
                    student.getParticipantId(), fullName, phone);

            student.setFullName(fullName);
            student.setPhone(phone);

        } catch (SQLException | RuntimeException e) {
            throw dbProblem(e);
        }
    }

    // =====================================================================
    // SMALL HELPERS (also used by the UI for display)
    // =====================================================================

    public String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? ""
                : dateTime.format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String prettyStatus(String status) {

        if (STATUS_REGISTERED.equals(status)) {
            return "Registered";
        }

        if (STATUS_CANCELLED.equals(status)) {
            return "Cancelled";
        }

        return status == null ? "" : status;
    }

    protected String clean(String text) {
        return text == null ? "" : text.trim();
    }

    protected void validateName(String name) throws StudentServiceException {

        if (name.isEmpty()) {
            throw new StudentServiceException("Full name is required.");
        }

        if (!name.matches(NAME_PATTERN)) {
            throw new StudentServiceException(
                    "Please enter a valid full name.");
        }
    }

    private void validateEmail(String email) throws StudentServiceException {

        if (email.isEmpty()) {
            throw new StudentServiceException("Email is required.");
        }

        if (!email.matches(EMAIL_PATTERN)) {
            throw new StudentServiceException(
                    "Please enter a valid email address.");
        }
    }

    protected void validatePhone(String phone) throws StudentServiceException {

        if (phone.isEmpty()) {
            throw new StudentServiceException(
                    "Phone number is required.");
        }

        if (!phone.matches(PHONE_PATTERN)) {
            throw new StudentServiceException(
                    "Phone number must contain 10 to 15 digits.");
        }
    }

    private void validateNewPassword(String password, String confirm)
            throws StudentServiceException {

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new StudentServiceException(
                    "Password must contain at least "
                            + MIN_PASSWORD_LENGTH + " characters.");
        }

        if (!password.equals(confirm)) {
            throw new StudentServiceException("Passwords do not match.");
        }
    }

    /**
     * Turns a low-level failure into a student-friendly message.
     * The real error is printed to the console so a developer can see it.
     */
    private StudentServiceException dbProblem(Exception cause) {

        if (cause instanceof StudentServiceException) {
            return (StudentServiceException) cause;
        }

        cause.printStackTrace();

        return new StudentServiceException(DB_DOWN, cause);
    }
}
