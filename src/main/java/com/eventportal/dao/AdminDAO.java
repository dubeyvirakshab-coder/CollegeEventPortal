package com.eventportal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eventportal.database.DBConnection;
import com.eventportal.service.PasswordUtil;

public class AdminDAO {

    // Register a new admin
    public boolean registerAdmin(String fullName,
                                 String username,
                                 String password) {

        String sql = "INSERT INTO admins " +
                "(full_name, username, password_hash, password_salt) " +
                "VALUES (?, ?, ?, ?)";

        String salt = PasswordUtil.generateSalt();

        String hashedPassword =
                PasswordUtil.hashPassword(password, salt);

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, fullName);
                ps.setString(2, username);
                ps.setString(3, hashedPassword);
                ps.setString(4, salt);

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {

            System.out.println("Admin registration failed!");
            e.printStackTrace();

            return false;
        }
    }

    // Authenticate admin login
    public boolean authenticateAdmin(String username,
                                     String password) {

        String sql = "SELECT password_hash, password_salt " +
                "FROM admins WHERE username = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        String storedHash =
                                rs.getString("password_hash");

                        String salt =
                                rs.getString("password_salt");

                        return PasswordUtil.verifyPassword(
                                password,
                                salt,
                                storedHash
                        );
                    }
                }
            }

        } catch (SQLException e) {

            System.out.println("Admin authentication failed!");
            e.printStackTrace();
        }

        return false;
    }

    // Check whether username already exists
    public boolean usernameExists(String username) {

        String sql =
                "SELECT admin_id FROM admins WHERE username = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return false;
    }

    // Check whether at least one admin exists
    public boolean hasAnyAdmin() {

        String sql =
                "SELECT admin_id FROM admins LIMIT 1";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return true;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return true;
        }
    }
}