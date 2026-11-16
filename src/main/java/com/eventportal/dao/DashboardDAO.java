
package com.eventportal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eventportal.database.DBConnection;

public class DashboardDAO {

    // ================= STATISTICS =================

    public int getTotalEvents() {
        return getCount("SELECT COUNT(*) FROM events");
    }

    public int getTotalParticipants() {
        return getCount("SELECT COUNT(*) FROM participants");
    }

    public int getTotalRegistrations() {
        return getCount("SELECT COUNT(*) FROM registrations");
    }

    public int getPresentCount() {
        return getCount(
                "SELECT COUNT(*) FROM registrations WHERE attendance = 1"
        );
    }

    // ================= COMMON COUNT METHOD =================

    private int getCount(String query) {

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return 0;
            }

            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ================= UPCOMING EVENTS =================

    public List<String> getUpcomingEvents() {

        List<String> eventsList = new ArrayList<>();

        String query =
                "SELECT event_name, event_date, event_time " +
                "FROM events " +
                "WHERE event_date >= CURDATE() " +
                "ORDER BY event_date ASC, event_time ASC " +
                "LIMIT 5";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return eventsList;
            }

            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String name = rs.getString("event_name");
                    String date = rs.getDate("event_date").toString();
                    String time = rs.getTime("event_time").toString();

                    eventsList.add(
                            name + "  |  " + date + "  |  " + time
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return eventsList;
    }

    // ================= RECENT REGISTRATIONS =================

    public List<String> getRecentRegistrations() {

        List<String> registrationList = new ArrayList<>();

        String query =
                "SELECT p.full_name, e.event_name, r.status " +
                "FROM registrations r " +
                "JOIN participants p " +
                "ON r.participant_id = p.participant_id " +
                "JOIN events e " +
                "ON r.event_id = e.event_id " +
                "ORDER BY r.registration_date DESC " +
                "LIMIT 5";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return registrationList;
            }

            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String participant = rs.getString("full_name");
                    String event = rs.getString("event_name");
                    String status = rs.getString("status");

                    registrationList.add(
                            participant + "  |  " + event + "  |  " + status
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return registrationList;
    }
}
