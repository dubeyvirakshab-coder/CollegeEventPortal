package com.eventportal.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.eventportal.dao.AdminDAO;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;

    private final AdminDAO adminDAO = new AdminDAO();

    private final Color NAVY = new Color(15, 23, 42);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color LIGHT = new Color(241, 245, 249);

    private JButton setupButton;

    public LoginFrame() {

        setTitle("College Event Portal - Admin Login");
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        createUI();

        setVisible(true);
    }

    private void createUI() {

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // LEFT PANEL
        JPanel leftPanel = new JPanel();

        leftPanel.setBackground(NAVY);
        leftPanel.setLayout(
                new BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        );

        leftPanel.setBorder(
                new EmptyBorder(110, 45, 60, 35)
        );

        JLabel logo = new JLabel("COLLEGE");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 36));

        JLabel portal = new JLabel("EVENT PORTAL");
        portal.setForeground(new Color(96, 165, 250));
        portal.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel description = new JLabel(
                "<html>Manage college events,<br>"
                + "participants and registrations<br>"
                + "from one platform.</html>"
        );

        description.setForeground(new Color(203, 213, 225));
        description.setFont(
                new Font("Arial", Font.PLAIN, 16)
        );

        JLabel footer = new JLabel("ADMINISTRATION PANEL");

        footer.setForeground(new Color(148, 163, 184));
        footer.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        leftPanel.add(logo);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(portal);
        leftPanel.add(Box.createVerticalStrut(35));
        leftPanel.add(description);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(footer);

        // RIGHT PANEL
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(LIGHT);

        // LOGIN CARD
        JPanel formPanel = new JPanel(new GridBagLayout());

        formPanel.setBackground(Color.WHITE);

        formPanel.setBorder(
                new EmptyBorder(35, 35, 35, 35)
        );

        formPanel.setPreferredSize(
                new Dimension(410, 530)
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);

        // HEADING
        JLabel heading = new JLabel("Welcome Back!");

        heading.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        heading.setForeground(NAVY);

        gbc.gridy = 0;
        formPanel.add(heading, gbc);

        JLabel subtitle = new JLabel(
                "Sign in to your admin account"
        );

        subtitle.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        subtitle.setForeground(Color.GRAY);

        gbc.gridy = 1;
        gbc.insets = new Insets(3, 0, 25, 0);

        formPanel.add(subtitle, gbc);

        // USERNAME
        JLabel usernameLabel = new JLabel("Username");

        styleLabel(usernameLabel);

        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 5, 0);

        formPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();

        styleTextField(usernameField);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);

        formPanel.add(usernameField, gbc);

        // PASSWORD
        JLabel passwordLabel = new JLabel("Password");

        styleLabel(passwordLabel);

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 0, 5, 0);

        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();

        styleTextField(passwordField);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 5, 0);

        formPanel.add(passwordField, gbc);

        // SHOW PASSWORD
        showPassword = new JCheckBox("Show password");

        showPassword.setBackground(Color.WHITE);
        showPassword.setFocusPainted(false);

        showPassword.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        showPassword.addActionListener(e -> {

            passwordField.setEchoChar(
                    showPassword.isSelected()
                            ? (char) 0 : '\u2022'
            );
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 22, 0);

        formPanel.add(showPassword, gbc);

        // LOGIN BUTTON
        JButton loginButton = new JButton("LOGIN");

        loginButton.setFont(
                new Font("Arial", Font.BOLD, 15)
        );

        loginButton.setBackground(BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setOpaque(true);
        loginButton.setContentAreaFilled(true);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);

        loginButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        loginButton.setPreferredSize(
                new Dimension(320, 48)
        );

        loginButton.addActionListener(e -> login());

        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 12, 0);

        formPanel.add(loginButton, gbc);

        // FIRST ADMIN SETUP
        setupButton = new JButton(
                "Create First Admin Account"
        );

        setupButton.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        setupButton.setForeground(BLUE);
        setupButton.setBackground(Color.WHITE);
        setupButton.setBorderPainted(false);
        setupButton.setFocusPainted(false);

        setupButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        setupButton.addActionListener(
                e -> createFirstAdmin()
        );

        gbc.gridy = 8;
        gbc.insets = new Insets(5, 0, 5, 0);

        formPanel.add(setupButton, gbc);

        updateSetupButton();

        // STUDENT PORTAL BUTTON
        JButton studentButton = new JButton("Student Portal");

        studentButton.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        studentButton.setForeground(BLUE);
        studentButton.setBackground(Color.WHITE);
        studentButton.setBorderPainted(false);
        studentButton.setFocusPainted(false);

        studentButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        studentButton.addActionListener(e -> {

            dispose();

            new StudentLoginFrame();
        });

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0);

        formPanel.add(studentButton, gbc);

        // CENTER LOGIN CARD
        GridBagConstraints cardConstraints =
                new GridBagConstraints();

        cardConstraints.gridx = 0;
        cardConstraints.gridy = 0;
        cardConstraints.anchor = GridBagConstraints.CENTER;

        rightPanel.add(formPanel, cardConstraints);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
    }

    // LABEL STYLING
    private void styleLabel(JLabel label) {

        label.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        label.setForeground(NAVY);
    }

    // TEXT FIELD STYLING
    private void styleTextField(JTextField field) {

        field.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        field.setPreferredSize(
                new Dimension(320, 43)
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                new Color(203, 213, 225)
                        ),

                        new EmptyBorder(5, 10, 5, 10)
                )
        );
    }

    // UPDATE FIRST ADMIN BUTTON
    private void updateSetupButton() {

        setupButton.setVisible(
                !adminDAO.hasAnyAdmin()
        );
    }

    // ADMIN LOGIN
    private void login() {

        String username = usernameField.getText().trim();

        String password = new String(
                passwordField.getPassword()
        );

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (adminDAO.authenticateAdmin(username, password)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Login successful! Welcome.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

            new MainFrame();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );

            passwordField.setText("");
        }
    }

    // CREATE FIRST ADMIN
    private void createFirstAdmin() {

        if (adminDAO.hasAnyAdmin()) {

            JOptionPane.showMessageDialog(
                    this,
                    "An admin account already exists."
            );

            updateSetupButton();

            return;
        }

        JTextField nameField = new JTextField();

        JTextField newUsernameField = new JTextField();

        JPasswordField newPasswordField =
                new JPasswordField();

        JPasswordField confirmPasswordField =
                new JPasswordField();

        Object[] fields = {

                "Full Name:",
                nameField,

                "Username:",
                newUsernameField,

                "Password:",
                newPasswordField,

                "Confirm Password:",
                confirmPasswordField
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Create First Admin",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String fullName = nameField.getText().trim();

        String username = newUsernameField.getText().trim();

        String password = new String(
                newPasswordField.getPassword()
        );

        String confirmPassword = new String(
                confirmPasswordField.getPassword()
        );

        if (fullName.isEmpty()
                || username.isEmpty()
                || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "All fields are required."
            );

            return;
        }

        if (username.length() < 3) {

            JOptionPane.showMessageDialog(
                    this,
                    "Username must contain at least 3 characters."
            );

            return;
        }

        if (password.length() < 8) {

            JOptionPane.showMessageDialog(
                    this,
                    "Password must contain at least 8 characters."
            );

            return;
        }

        if (!password.equals(confirmPassword)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match."
            );

            return;
        }

        // Check again before registration
        if (adminDAO.hasAnyAdmin()) {

            JOptionPane.showMessageDialog(
                    this,
                    "An admin account already exists."
            );

            updateSetupButton();

            return;
        }

        boolean created = adminDAO.registerAdmin(
                fullName,
                username,
                password
        );

        if (created) {

            JOptionPane.showMessageDialog(
                    this,
                    "Admin account created successfully!"
            );

            dispose();

            new LoginFrame();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Admin account creation failed. "
                    + "Check database connection.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}