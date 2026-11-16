package com.eventportal.ui;

import java.awt.CardLayout;
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

import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.Participant;
import com.eventportal.service.StudentService;

/**
 * Student Login screen.
 *
 * Looks like the Admin LoginFrame (same two-panel layout, fonts and
 * colours) but signs students in with EMAIL + PASSWORD, and lets a new
 * student create an account on the second "card" of the same window.
 *
 * It never opens any Admin screen.
 */
public class StudentLoginFrame extends JFrame {

    // Same shades as the Admin LoginFrame
    private final Color NAVY = new Color(15, 23, 42);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color LIGHT = new Color(241, 245, 249);

    private static final String CARD_LOGIN = "login";
    private static final String CARD_SIGNUP = "signup";

    private final StudentService service;

    private final CardLayout cards = new CardLayout();
    private final JPanel cardHolder = new JPanel(cards);

    // Login card
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JCheckBox loginShowPassword;
    private JButton loginButton;

    // Sign-up card
    private JTextField signupNameField;
    private JTextField signupEmailField;
    private JTextField signupPhoneField;
    private JPasswordField signupPasswordField;
    private JPasswordField signupConfirmField;
    private JCheckBox signupShowPassword;
    private JButton signupButton;

    public StudentLoginFrame() {
        this(new StudentService());
    }

    /** The service is passed in so a preview can use demo data. */
    public StudentLoginFrame(StudentService service) {

        this.service = service;

        setTitle("College Event Portal - Student Login");
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        createUI();

        setVisible(true);
    }

    // ================= LAYOUT =================

    private void createUI() {

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        mainPanel.add(createLeftPanel());
        mainPanel.add(createRightPanel());

        add(mainPanel);

        showCard(CARD_LOGIN);
    }

    private JPanel createLeftPanel() {

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(NAVY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(110, 45, 60, 35));

        JLabel logo = new JLabel("COLLEGE");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 36));

        JLabel portal = new JLabel("EVENT PORTAL");
        portal.setForeground(new Color(96, 165, 250));
        portal.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel description = new JLabel(
                "<html>Discover college events,<br>"
                        + "register in a few clicks<br>"
                        + "and track your registrations.</html>"
        );
        description.setForeground(new Color(203, 213, 225));
        description.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel footer = new JLabel("STUDENT PORTAL");
        footer.setForeground(new Color(148, 163, 184));
        footer.setFont(new Font("Arial", Font.BOLD, 12));

        leftPanel.add(logo);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(portal);
        leftPanel.add(Box.createVerticalStrut(35));
        leftPanel.add(description);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(footer);

        return leftPanel;
    }

    private JPanel createRightPanel() {

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(LIGHT);

        cardHolder.setBackground(Color.WHITE);
        cardHolder.setBorder(new EmptyBorder(30, 35, 25, 35));
        cardHolder.setPreferredSize(new Dimension(410, 650));

        cardHolder.add(createLoginCard(), CARD_LOGIN);
        cardHolder.add(createSignupCard(), CARD_SIGNUP);

        rightPanel.add(cardHolder, new GridBagConstraints());

        return rightPanel;
    }

    // ================= LOGIN CARD =================

    private JPanel createLoginCard() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = newConstraints();

        int row = 0;

        JLabel heading = new JLabel("Welcome Back!");
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(NAVY);
        addRow(panel, gbc, row++, heading, new Insets(5, 0, 5, 0));

        JLabel subtitle = new JLabel("Sign in to your student account");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        addRow(panel, gbc, row++, subtitle, new Insets(3, 0, 25, 0));

        addRow(panel, gbc, row++, label("Email"), new Insets(5, 0, 5, 0));

        loginEmailField = new JTextField();
        styleTextField(loginEmailField);
        addRow(panel, gbc, row++, loginEmailField, new Insets(0, 0, 15, 0));

        addRow(panel, gbc, row++, label("Password"), new Insets(5, 0, 5, 0));

        loginPasswordField = new JPasswordField();
        styleTextField(loginPasswordField);
        addRow(panel, gbc, row++, loginPasswordField, new Insets(0, 0, 5, 0));

        loginShowPassword = showPasswordBox(loginPasswordField);
        addRow(panel, gbc, row++, loginShowPassword, new Insets(0, 0, 22, 0));

        loginButton = bigButton("LOGIN");
        loginButton.addActionListener(e -> login());
        addRow(panel, gbc, row++, loginButton, new Insets(5, 0, 12, 0));

        JButton createLink = linkButton("Create Student Account");
        createLink.addActionListener(e -> showCard(CARD_SIGNUP));
        addRow(panel, gbc, row++, createLink, new Insets(5, 0, 5, 0));

        JButton adminLink = linkButton("Admin Login");
        adminLink.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        addRow(panel, gbc, row++, adminLink, new Insets(0, 0, 0, 0));

        // pushes everything to the top
        gbc.gridy = row;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    // ================= SIGN-UP CARD =================

    private JPanel createSignupCard() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = newConstraints();

        int row = 0;

        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(NAVY);
        addRow(panel, gbc, row++, heading, new Insets(5, 0, 5, 0));

        JLabel subtitle = new JLabel(
                "<html>Already added by the college? Use the same<br>"
                        + "email and phone number to set a password.</html>");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(Color.GRAY);
        addRow(panel, gbc, row++, subtitle, new Insets(3, 0, 12, 0));

        addRow(panel, gbc, row++, label("Full Name"), new Insets(3, 0, 4, 0));
        signupNameField = new JTextField();
        styleTextField(signupNameField);
        addRow(panel, gbc, row++, signupNameField, new Insets(0, 0, 8, 0));

        addRow(panel, gbc, row++, label("Email"), new Insets(3, 0, 4, 0));
        signupEmailField = new JTextField();
        styleTextField(signupEmailField);
        addRow(panel, gbc, row++, signupEmailField, new Insets(0, 0, 8, 0));

        addRow(panel, gbc, row++, label("Phone Number"),
                new Insets(3, 0, 4, 0));
        signupPhoneField = new JTextField();
        styleTextField(signupPhoneField);
        addRow(panel, gbc, row++, signupPhoneField, new Insets(0, 0, 8, 0));

        addRow(panel, gbc, row++, label("Password (min. 8 characters)"),
                new Insets(3, 0, 4, 0));
        signupPasswordField = new JPasswordField();
        styleTextField(signupPasswordField);
        addRow(panel, gbc, row++, signupPasswordField,
                new Insets(0, 0, 8, 0));

        addRow(panel, gbc, row++, label("Confirm Password"),
                new Insets(3, 0, 4, 0));
        signupConfirmField = new JPasswordField();
        styleTextField(signupConfirmField);
        addRow(panel, gbc, row++, signupConfirmField, new Insets(0, 0, 4, 0));

        signupShowPassword = new JCheckBox("Show passwords");
        signupShowPassword.setBackground(Color.WHITE);
        signupShowPassword.setFocusPainted(false);
        signupShowPassword.setFont(new Font("Arial", Font.PLAIN, 13));
        signupShowPassword.addActionListener(e -> {
            char echo = signupShowPassword.isSelected()
                    ? (char) 0 : '\u2022';
            signupPasswordField.setEchoChar(echo);
            signupConfirmField.setEchoChar(echo);
        });
        addRow(panel, gbc, row++, signupShowPassword,
                new Insets(0, 0, 12, 0));

        signupButton = bigButton("CREATE ACCOUNT");
        signupButton.addActionListener(e -> createAccount());
        addRow(panel, gbc, row++, signupButton, new Insets(0, 0, 8, 0));

        JButton backLink = linkButton("Back to Login");
        backLink.addActionListener(e -> showCard(CARD_LOGIN));
        addRow(panel, gbc, row++, backLink, new Insets(0, 0, 0, 0));

        gbc.gridy = row;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    // ================= ACTIONS =================

    private void showCard(String name) {

        cards.show(cardHolder, name);

        // Enter key presses the button of the visible card
        getRootPane().setDefaultButton(
                CARD_LOGIN.equals(name) ? loginButton : signupButton);

        if (CARD_LOGIN.equals(name)) {
            loginPasswordField.setText("");
            loginEmailField.requestFocusInWindow();
        } else {
            signupNameField.requestFocusInWindow();
        }
    }

    private void login() {

        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both email and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            Participant student = service.login(email, password);

            openDashboard(student);

        } catch (StudentServiceException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );

            loginPasswordField.setText("");
        }
    }

    private void createAccount() {

        String password = new String(signupPasswordField.getPassword());
        String confirm = new String(signupConfirmField.getPassword());

        try {

            Participant student = service.createAccount(
                    signupNameField.getText(),
                    signupEmailField.getText(),
                    signupPhoneField.getText(),
                    password,
                    confirm
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully! Welcome, "
                            + student.getFullName() + ".",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            openDashboard(student);

        } catch (StudentServiceException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Could Not Create Account",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openDashboard(Participant student) {

        StudentDashboardFrame dashboard =
                new StudentDashboardFrame(student, service);

        dashboard.setVisible(true);

        dispose();
    }

    // ================= SMALL UI HELPERS =================

    private GridBagConstraints newConstraints() {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        javax.swing.JComponent component, Insets insets) {

        gbc.gridy = row;
        gbc.insets = insets;

        panel.add(component, gbc);
    }

    private JLabel label(String text) {

        JLabel label = new JLabel(text);

        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(NAVY);

        return label;
    }

    private void styleTextField(JTextField field) {

        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(320, 38));

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(203, 213, 225)),
                        new EmptyBorder(5, 10, 5, 10)
                )
        );
    }

    private JCheckBox showPasswordBox(JPasswordField field) {

        JCheckBox box = new JCheckBox("Show password");

        box.setBackground(Color.WHITE);
        box.setFocusPainted(false);
        box.setFont(new Font("Arial", Font.PLAIN, 13));

        box.addActionListener(e ->
                field.setEchoChar(box.isSelected() ? (char) 0 : '\u2022'));

        return box;
    }

    private JButton bigButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setBackground(BLUE);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(320, 46));

        return button;
    }

    private JButton linkButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setForeground(BLUE);
        button.setBackground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}
