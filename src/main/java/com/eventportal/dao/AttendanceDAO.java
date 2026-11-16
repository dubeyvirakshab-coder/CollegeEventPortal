
package com.eventportal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eventportal.database.DBConnection;

public class AttendanceDAO {

    // Get registered participants for an event
    public List<Object[]> getEventParticipants(int eventId) {

        List<Object[]> participants = new ArrayList<>();

        String sql = "SELECT r.registration_id, p.participant_id, " +
                "p.full_name, p.email, r.attendance " +
                "FROM registrations r " +
                "JOIN participants p ON r.participant_id = p.participant_id " +
                "WHERE r.event_id = ? AND r.status = 'REGISTERED' " +
                "ORDER BY p.full_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Object[] row = {
                            rs.getInt("registration_id"),
                            rs.getInt("participant_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBoolean("attendance")
                    };

                    participants.add(row);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading attendance records!");
            e.printStackTrace();
        }

        return participants;
    }

    // Update attendance for a participant
    public boolean updateAttendance(int registrationId, boolean present) {

        String sql = "UPDATE registrations SET attendance = ? " +
                "WHERE registration_id = ? AND status = 'REGISTERED'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, present);
            ps.setInt(2, registrationId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating attendance!");
            e.printStackTrace();
            return false;
        }
    }

    // Mark all registered participants as present
    public boolean markAttendance(int eventId) {

        String sql = "UPDATE registrations " +
                "SET attendance = TRUE " +
                "WHERE event_id = ? AND status = 'REGISTERED'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error marking attendance!");
            e.printStackTrace();
            return false;
        }
    }

    // Generate attendance report for an event
    public Object[] generateEventReport(int eventId) {

        String sql = "SELECT " +
                "COUNT(*) AS total_registrations, " +
                "SUM(CASE WHEN attendance = TRUE THEN 1 ELSE 0 END) AS present_count, " +
                "SUM(CASE WHEN attendance = FALSE THEN 1 ELSE 0 END) AS absent_count " +
                "FROM registrations " +
                "WHERE event_id = ? AND status = 'REGISTERED'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int total = rs.getInt("total_registrations");
                    int present = rs.getInt("present_count");
                    int absent = rs.getInt("absent_count");

                    double percentage = total == 0
                            ? 0
                            : (present * 100.0) / total;

                    return new Object[]{
                            total,
                            present,
                            absent,
                            percentage
                    };
                }
            }

        } catch (SQLException e) {
            System.out.println("Error generating event report!");
            e.printStackTrace();
        }

        return new Object[]{0, 0, 0, 0.0};
    }
}
