
package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.eventportal.dao.ParticipantDAO;
import com.eventportal.model.Participant;

public class ParticipantManagementFrame extends JFrame {

    private static final Color NAVY = new Color(20, 35, 65);
    private static final Color BLUE = new Color(35, 90, 200);
    private static final Color BACKGROUND = new Color(242, 245, 250);
    private static final Color TEXT = new Color(45, 55, 75);

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;

    private JTable participantTable;
    private DefaultTableModel tableModel;

    private final ParticipantDAO participantDAO = new ParticipantDAO();

    public ParticipantManagementFrame() {

        setTitle("Participant Management");
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadParticipants();

        setVisible(true);
    }

    // HEADER
    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Participant Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Register and manage college event participants"
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(195, 205, 225));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        return header;
    }

    // MAIN PANEL
    private JPanel createMainPanel() {

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(BACKGROUND);
        main.setBorder(BorderFactory.createEmptyBorder(22, 25, 22, 25));

        main.add(createFormPanel(), BorderLayout.NORTH);
        main.add(createTablePanel(), BorderLayout.CENTER);

        return main;
    }

    // PARTICIPANT FORM
    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel heading = new JLabel("Add New Participant");
        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(NAVY);

        panel.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 3, 15, 10));
        fields.setBackground(Color.WHITE);

        nameField = createTextField();
        emailField = createTextField();
        phoneField = createTextField();

        fields.add(createField("Full Name", nameField));
        fields.add(createField("Email Address", emailField));
        fields.add(createField("Phone Number", phoneField));

        JButton saveButton = new JButton("SAVE PARTICIPANT");

        saveButton.setBackground(BLUE);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true);
        saveButton.setContentAreaFilled(true);
        saveButton.setPreferredSize(new Dimension(190, 42));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> saveParticipant());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(fields, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    // PARTICIPANT TABLE
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel heading = new JLabel("All Participants");
        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(NAVY);

        panel.add(heading, BorderLayout.NORTH);

        String[] columns = {
                "Participant ID",
                "Full Name",
                "Email Address",
                "Phone Number"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        participantTable = new JTable(tableModel);
        participantTable.setRowHeight(32);
        participantTable.setFont(new Font("SansSerif", Font.PLAIN, 13));

        participantTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        participantTable.getTableHeader().setBackground(NAVY);
        participantTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(participantTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // TEXT FIELD
    private JTextField createTextField() {

        JTextField field = new JTextField();

        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(180, 35));

        return field;
    }

    // FORM FIELD
    private JPanel createField(String labelText, JComponent component) {

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    // SAVE PARTICIPANT
    private void saveParticipant() {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill all the fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!name.matches("[\\p{L} .'-]+")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid full name.",
                    "Invalid Name",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!phone.matches("[0-9]{10,15}")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Phone number must contain 10 to 15 digits.",
                    "Invalid Phone Number",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // Check if participant already exists
        Participant existing =
                participantDAO.getParticipantByEmail(email);

        if (existing != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "A participant with this email already exists.",
                    "Duplicate Participant",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Participant participant = new Participant(
                name,
                email,
                phone
        );

        boolean success = participantDAO.addParticipant(participant);

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Participant registered successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            loadParticipants();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to register participant. Please check database connection.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // LOAD PARTICIPANTS
    private void loadParticipants() {

        tableModel.setRowCount(0);

        List<Participant> participants =
                participantDAO.getAllParticipants();

        for (Participant participant : participants) {

            Object[] row = {
                    participant.getParticipantId(),
                    participant.getFullName(),
                    participant.getEmail(),
                    participant.getPhone()
            };

            tableModel.addRow(row);
        }
    }

    // CLEAR FORM
    private void clearForm() {

        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }
}
