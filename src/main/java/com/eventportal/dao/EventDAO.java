
package com.eventportal.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import com.eventportal.database.DBConnection;
import com.eventportal.model.Event;

public class EventDAO {

    // Save event into database
    public boolean addEvent(Event event) {

        String sql = "INSERT INTO events " +
                "(event_name, description, category, event_date, " +
                "event_time, venue, total_seats, available_seats) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, event.getEventName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory());
            ps.setDate(4, Date.valueOf(event.getEventDate()));
            ps.setTime(5, Time.valueOf(event.getEventTime()));
            ps.setString(6, event.getVenue());
            ps.setInt(7, event.getTotalSeats());
            ps.setInt(8, event.getAvailableSeats());

            int rows = ps.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all events from database
    public List<Event> getAllEvents() {

        List<Event> events = new ArrayList<>();

        String sql = "SELECT * FROM events";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Event event = new Event();

                event.setEventId(rs.getInt("event_id"));
                event.setEventName(rs.getString("event_name"));
                event.setDescription(rs.getString("description"));
                event.setCategory(rs.getString("category"));
                event.setEventDate(
                        rs.getDate("event_date").toLocalDate()
                );
                event.setEventTime(
                        rs.getTime("event_time").toLocalTime()
                );
                event.setVenue(rs.getString("venue"));
                event.setTotalSeats(rs.getInt("total_seats"));
                event.setAvailableSeats(rs.getInt("available_seats"));

                events.add(event);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }
}
