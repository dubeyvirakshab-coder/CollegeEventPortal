package com.eventportal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eventportal.database.DBConnection;
import com.eventportal.model.Participant;
import com.eventportal.service.PasswordUtil;

/**
 * Handles student LOGIN CREDENTIALS only.
 *
 * Students are the existing "participants" rows. This class reads and
 * writes two extra columns on that table (password_hash, password_salt)
 * in the same way AdminDAO does for administrators.
 *
 * Everything else about a participant (name, phone, registrations)
 * stays in ParticipantDAO / RegistrationDAO.
 *
 * Methods throw SQLException so the service layer can tell the student
 * "database unavailable" instead of wrongly saying "wrong password".
 */
public class StudentDAO {

    private Connection connect() throws SQLException {

        Connection con = DBConnection.getConnection();

        if (con == null) {
            throw new SQLException("Database connection unavailable.");
        }

        return con;
    }

    /**
     * Looks at the participant with this email.
     *
     * @return null  -> no participant with this email
     *         FALSE -> participant exists but has NO password yet
     *                  (added earlier by an admin)
     *         TRUE  -> participant already has a student account
     */
    public Boolean getPasswordState(String email) throws SQLException {

        String sql = "SELECT password_hash FROM participants WHERE email = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String hash = rs.getString("password_hash");

                return hash != null && !hash.isEmpty();
            }
        }
    }

    /**
     * Checks the password. Returns the Participant when it is correct,
     * otherwise null.
     */
    public Participant authenticate(String email, String password)
            throws SQLException {

        String sql = "SELECT participant_id, full_name, email, phone, "
                + "password_hash, password_salt "
                + "FROM participants WHERE email = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("password_salt");

                if (storedHash == null || salt == null) {
                    return null;
                }

                if (!PasswordUtil.verifyPassword(
                        password, salt, storedHash)) {
                    return null;
                }

                Participant participant = new Participant();

                participant.setParticipantId(rs.getInt("participant_id"));
                participant.setFullName(rs.getString("full_name"));
                participant.setEmail(rs.getString("email"));
                participant.setPhone(rs.getString("phone"));

                return participant;
            }
        }
    }

    /**
     * Creates a brand-new participant who can log in.
     */
    public boolean createStudentAccount(String fullName, String email,
                                        String phone, String password)
            throws SQLException {

        String sql = "INSERT INTO participants "
                + "(full_name, email, phone, password_hash, password_salt) "
                + "VALUES (?, ?, ?, ?, ?)";

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, hash);
            ps.setString(5, salt);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Gives a password to an existing participant that does not have one.
     * The "password_hash IS NULL" condition guarantees this can never
     * overwrite the password of an account that is already active.
     */
    public boolean setInitialPassword(int participantId, String password)
            throws SQLException {

        String sql = "UPDATE participants "
                + "SET password_hash = ?, password_salt = ? "
                + "WHERE participant_id = ? AND password_hash IS NULL";

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, participantId);

            return ps.executeUpdate() > 0;
        }
    }
}
