package com.eventportal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eventportal.database.DBConnection;
import com.eventportal.model.Participant;

public class ParticipantDAO {

    // Add participant to database
    public boolean addParticipant(Participant participant) {

        String sql = "INSERT INTO participants "
                + "(full_name, email, phone) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, participant.getFullName());
                ps.setString(2, participant.getEmail());
                ps.setString(3, participant.getPhone());

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Find participant using email
    public Participant getParticipantByEmail(String email) {

        String sql = "SELECT * FROM participants WHERE email = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        Participant participant = new Participant();

                        participant.setParticipantId(
                                rs.getInt("participant_id")
                        );

                        participant.setFullName(
                                rs.getString("full_name")
                        );

                        participant.setEmail(
                                rs.getString("email")
                        );

                        participant.setPhone(
                                rs.getString("phone")
                        );

                        return participant;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Retrieve all participants
    public List<Participant> getAllParticipants() {

        List<Participant> participants = new ArrayList<>();

        String sql = "SELECT * FROM participants "
                + "ORDER BY participant_id DESC";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return participants;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Participant participant = new Participant();

                    participant.setParticipantId(
                            rs.getInt("participant_id")
                    );

                    participant.setFullName(
                            rs.getString("full_name")
                    );

                    participant.setEmail(
                            rs.getString("email")
                    );

                    participant.setPhone(
                            rs.getString("phone")
                    );

                    participants.add(participant);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participants;
    }

    // ================= STUDENT PORTAL ADDITIONS =================

    // Find participant using ID
    public Participant getParticipantById(int participantId)
            throws SQLException {

        String sql = "SELECT participant_id, full_name, email, phone "
                + "FROM participants WHERE participant_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException(
                        "Database connection unavailable."
                );
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, participantId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        Participant participant = new Participant();

                        participant.setParticipantId(
                                rs.getInt("participant_id")
                        );

                        participant.setFullName(
                                rs.getString("full_name")
                        );

                        participant.setEmail(
                                rs.getString("email")
                        );

                        participant.setPhone(
                                rs.getString("phone")
                        );

                        return participant;
                    }
                }
            }
        }

        return null;
    }

    // Update student profile
    public boolean updateProfile(int participantId,
                                 String fullName,
                                 String phone) throws SQLException {

        String sql = "UPDATE participants "
                + "SET full_name = ?, phone = ? "
                + "WHERE participant_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException(
                        "Database connection unavailable."
                );
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, fullName);
                ps.setString(2, phone);
                ps.setInt(3, participantId);

                ps.executeUpdate();

                return true;
            }
        }
    }
}