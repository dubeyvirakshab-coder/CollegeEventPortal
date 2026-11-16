package com.eventportal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.eventportal.database.DBConnection;
import com.eventportal.exception.EventFullException;
import com.eventportal.model.Registration;

public class RegistrationDAO {

    // Register participant using event ID and participant ID
    public boolean registerParticipant(int eventId, int participantId)
            throws EventFullException {

        Registration registration = new Registration(
                eventId,
                participantId
        );

        registration.register();

        return addRegistration(registration);
    }

    // Add registration to database
    public synchronized boolean addRegistration(Registration registration)
            throws EventFullException {

        String duplicateSQL =
                "SELECT registration_id FROM registrations "
                + "WHERE event_id = ? AND participant_id = ? "
                + "AND status = 'REGISTERED'";

        String seatSQL =
                "UPDATE events SET available_seats = available_seats - 1 "
                + "WHERE event_id = ? AND available_seats > 0";

        String insertSQL =
                "INSERT INTO registrations "
                + "(event_id, participant_id, status, attendance, registration_date) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            if (con == null) {
                return false;
            }

            con.setAutoCommit(false);

            // Check duplicate registration
            try (PreparedStatement ps = con.prepareStatement(duplicateSQL)) {

                ps.setInt(1, registration.getEventId());
                ps.setInt(2, registration.getParticipantId());

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            // Check and reduce available seats
            try (PreparedStatement ps = con.prepareStatement(seatSQL)) {

                ps.setInt(1, registration.getEventId());

                int updatedRows = ps.executeUpdate();

                if (updatedRows == 0) {

                    con.rollback();

                    throw new EventFullException(
                            "Event is full or no seats are available!"
                    );
                }
            }

            // Insert registration
            try (PreparedStatement ps = con.prepareStatement(insertSQL)) {

                ps.setInt(1, registration.getEventId());
                ps.setInt(2, registration.getParticipantId());
                ps.setString(3, "REGISTERED");
                ps.setBoolean(4, false);

                if (registration.getRegistrationDate() == null) {
                    registration.setRegistrationDate(
                            java.time.LocalDateTime.now()
                    );
                }

                ps.setTimestamp(
                        5,
                        Timestamp.valueOf(
                                registration.getRegistrationDate()
                        )
                );

                ps.executeUpdate();
            }

            con.commit();

            return true;

        } catch (EventFullException e) {

            throw e;

        } catch (SQLException e) {

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            e.printStackTrace();
            return false;

        } finally {

            if (con != null) {

                try {
                    con.setAutoCommit(true);
                    con.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Cancel registration (Admin)
    public boolean cancelRegistration(int registrationId) {

        return cancelRegistrationInternal(registrationId, 0);
    }

    // Cancel registration only if it belongs to this student
    public boolean cancelRegistration(int registrationId,
                                      int participantId) {

        if (participantId <= 0) {
            return false;
        }

        return cancelRegistrationInternal(
                registrationId,
                participantId
        );
    }

    // Shared cancellation transaction
    private boolean cancelRegistrationInternal(int registrationId,
                                                int participantId) {

        boolean checkOwner = participantId > 0;

        String findSQL =
                "SELECT event_id FROM registrations "
                + "WHERE registration_id = ? AND status = 'REGISTERED' "
                + (checkOwner ? "AND participant_id = ? " : "")
                + "FOR UPDATE";

        String cancelSQL =
                "UPDATE registrations SET status = 'CANCELLED' "
                + "WHERE registration_id = ? AND status = 'REGISTERED'";

        String restoreSeatSQL =
                "UPDATE events SET available_seats = available_seats + 1 "
                + "WHERE event_id = ? AND available_seats < total_seats";

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            if (con == null) {
                return false;
            }

            con.setAutoCommit(false);

            int eventId;

            // Find active registration
            try (PreparedStatement ps = con.prepareStatement(findSQL)) {

                ps.setInt(1, registrationId);

                if (checkOwner) {
                    ps.setInt(2, participantId);
                }

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }

                    eventId = rs.getInt("event_id");
                }
            }

            // Cancel registration
            try (PreparedStatement ps = con.prepareStatement(cancelSQL)) {

                ps.setInt(1, registrationId);

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            // Restore available seat
            try (PreparedStatement ps = con.prepareStatement(restoreSeatSQL)) {

                ps.setInt(1, eventId);

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();

            return true;

        } catch (SQLException e) {

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            e.printStackTrace();
            return false;

        } finally {

            if (con != null) {

                try {
                    con.setAutoCommit(true);
                    con.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Retrieve all registrations (Admin)
    public List<Registration> getAllRegistrations() {

        List<Registration> registrations = new ArrayList<>();

        String sql =
                "SELECT * FROM registrations "
                + "ORDER BY registration_date DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return registrations;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Registration registration = new Registration();

                    registration.setRegistrationId(
                            rs.getInt("registration_id")
                    );

                    registration.setEventId(
                            rs.getInt("event_id")
                    );

                    registration.setParticipantId(
                            rs.getInt("participant_id")
                    );

                    registration.setStatus(
                            rs.getString("status")
                    );

                    registration.setAttendance(
                            rs.getBoolean("attendance")
                    );

                    Timestamp timestamp =
                            rs.getTimestamp("registration_date");

                    if (timestamp != null) {
                        registration.setRegistrationDate(
                                timestamp.toLocalDateTime()
                        );
                    }

                    registrations.add(registration);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return registrations;
    }

    // ================= STUDENT PORTAL ADDITIONS =================

    // Retrieve all registrations belonging to one participant
    public List<Registration> getRegistrationsByParticipant(
            int participantId) throws SQLException {

        List<Registration> registrations = new ArrayList<>();

        String sql =
                "SELECT * FROM registrations WHERE participant_id = ? "
                + "ORDER BY registration_date DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException(
                        "Database connection unavailable."
                );
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, participantId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        Registration registration = new Registration();

                        registration.setRegistrationId(
                                rs.getInt("registration_id")
                        );

                        registration.setEventId(
                                rs.getInt("event_id")
                        );

                        registration.setParticipantId(
                                rs.getInt("participant_id")
                        );

                        registration.setStatus(
                                rs.getString("status")
                        );

                        registration.setAttendance(
                                rs.getBoolean("attendance")
                        );

                        Timestamp timestamp =
                                rs.getTimestamp("registration_date");

                        if (timestamp != null) {
                            registration.setRegistrationDate(
                                    timestamp.toLocalDateTime()
                            );
                        }

                        registrations.add(registration);
                    }
                }
            }
        }

        return registrations;
    }

    // Check whether student already has an active registration
    public boolean hasActiveRegistration(int eventId,
                                         int participantId)
            throws SQLException {

        String sql =
                "SELECT registration_id FROM registrations "
                + "WHERE event_id = ? AND participant_id = ? "
                + "AND status = 'REGISTERED'";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException(
                        "Database connection unavailable."
                );
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, eventId);
                ps.setInt(2, participantId);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }
}