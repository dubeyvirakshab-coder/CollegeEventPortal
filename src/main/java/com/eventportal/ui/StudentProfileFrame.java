package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.Participant;
import com.eventportal.service.StudentService;

/**
 * Student Profile: shows the logged-in student's details and lets them
 * change their name and phone number.
 *
 * The email is the student's login, so it is shown but not editable here.
 */
public class StudentProfileFrame extends JFrame {

    private final Participant student;
    private final StudentService service;

    private JTextField idField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;

    public StudentProfileFrame(Participant student) {
        this(student, new StudentService());
    }

    public StudentProfileFrame(Participant student, StudentService service) {

        this.student = student;
        this.service = service;

        setTitle("My Profile");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new StudentSidebar(this, student, service,
                StudentSidebar.PROFILE), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadProfile();
    }

    // ================= LAYOUT =================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(StudentTheme.BACKGROUND);

        main.add(StudentTheme.pageHeader("My Profile"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(StudentTheme.BACKGROUND);
        body.setBorder(new EmptyBorder(25, 30, 25, 30));

        // keeps the form card at a comfortable width, aligned left
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        holder.setBackground(StudentTheme.BACKGROUND);
        holder.add(createFormCard());

        body.add(holder, BorderLayout.NORTH);

        main.add(body, BorderLayout.CENTER);

        return main;
    }

    private JPanel createFormCard() {

        JPanel card = StudentTheme.borderedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(620, 470));

        JLabel heading = new JLabel("Personal Information");
        heading.setFont(StudentTheme.font(Font.BOLD, 19));
        heading.setForeground(StudentTheme.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(22));

        idField = createField(false);
        nameField = createField(true);
        emailField = createField(false);
        phoneField = createField(true);

        addField(card, "Student ID", idField, null);
        addField(card, "Full Name", nameField, null);
        addField(card, "Email Address", emailField,
                "Your email is your login, so it cannot be changed here.");
        addField(card, "Phone Number", phoneField,
                "10 to 15 digits, numbers only.");

        card.add(Box.createVerticalStrut(8));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setBackground(StudentTheme.WHITE);
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton save = StudentTheme.primaryButton("SAVE CHANGES");
        save.addActionListener(e -> saveProfile());

        JButton reset = StudentTheme.outlineButton("RESET");
        reset.setPreferredSize(new Dimension(110, 40));
        reset.addActionListener(e -> loadProfile());

        buttons.add(save);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(reset);

        card.add(buttons);

        return card;
    }

    private void addField(JPanel card, String labelText, JTextField field,
                          String hint) {

        JLabel label = new JLabel(labelText);
        label.setFont(StudentTheme.font(Font.BOLD, 13));
        label.setForeground(StudentTheme.TEXT);
        label.setAlignmentX(LEFT_ALIGNMENT);

        field.setAlignmentX(LEFT_ALIGNMENT);

        card.add(label);
        card.add(Box.createVerticalStrut(6));
        card.add(field);

        if (hint != null) {
            JLabel hintLabel = StudentTheme.hintLabel(hint);
            hintLabel.setAlignmentX(LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(4));
            card.add(hintLabel);
        }

        card.add(Box.createVerticalStrut(14));
    }

    private JTextField createField(boolean editable) {

        JTextField field = new JTextField();

        field.setFont(StudentTheme.font(Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(570, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setEditable(editable);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(5, 10, 5, 10)));

        if (!editable) {
            field.setBackground(new Color(245, 247, 250));
            field.setForeground(Color.GRAY);
        }

        return field;
    }

    // ================= ACTIONS =================

    /** Fills the form with the latest saved details. */
    private void loadProfile() {

        Participant shown = student;

        try {
            shown = service.getProfile(student);

        } catch (StudentServiceException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Could Not Load Profile",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        idField.setText(String.valueOf(shown.getParticipantId()));
        nameField.setText(shown.getFullName());
        emailField.setText(shown.getEmail());
        phoneField.setText(shown.getPhone());
    }

    private void saveProfile() {

        try {

            service.updateProfile(
                    student, nameField.getText(), phoneField.getText());

            JOptionPane.showMessageDialog(
                    this,
                    "Your profile has been updated.",
                    "Profile Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadProfile();

        } catch (StudentServiceException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Could Not Save Profile",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
