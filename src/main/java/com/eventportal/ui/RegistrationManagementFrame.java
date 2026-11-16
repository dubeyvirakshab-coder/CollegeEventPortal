
package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.eventportal.dao.EventDAO;
import com.eventportal.dao.ParticipantDAO;
import com.eventportal.dao.RegistrationDAO;
import com.eventportal.exception.EventFullException;
import com.eventportal.model.Event;
import com.eventportal.model.Participant;
import com.eventportal.model.Registration;

public class RegistrationManagementFrame extends JFrame {

    private final Color NAVY = new Color(20, 32, 55);
    private final Color BLUE = new Color(48, 110, 220);
    private final Color BACKGROUND = new Color(242, 245, 250);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(45, 55, 72);

    private final EventDAO eventDAO = new EventDAO();
    private final ParticipantDAO participantDAO = new ParticipantDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    private JComboBox<Event> eventCombo;
    private JComboBox<Participant> participantCombo;

    private JLabel availableSeatsLabel;

    private JTable registrationTable;
    private DefaultTableModel tableModel;

    private Map<Integer, String> eventNames = new HashMap<>();
    private Map<Integer, String> participantNames = new HashMap<>();

    public RegistrationManagementFrame() {

        setTitle("Registration Management");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadEvents();
        loadParticipants();
        loadRegistrations();

        setVisible(true);
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setBorder(
                BorderFactory.createEmptyBorder(22, 30, 22, 30)
        );

        JLabel title = new JLabel("REGISTRATION MANAGEMENT");

        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel("Manage event registrations");

        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(190, 205, 225));

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(NAVY);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        return header;
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(20, 20));

        main.setBackground(BACKGROUND);
        main.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        main.add(createRegistrationForm(), BorderLayout.NORTH);
        main.add(createRegistrationTable(), BorderLayout.CENTER);

        return main;
    }

    // ================= REGISTRATION FORM =================

    private JPanel createRegistrationForm() {

        JPanel panel = new JPanel();

        panel.setBackground(WHITE);
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        BorderFactory.createEmptyBorder(22, 22, 22, 22)
                )
        );

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("New Registration");

        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(TEXT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(20));

        JPanel fields = new JPanel(new GridLayout(2, 2, 20, 15));
        fields.setBackground(WHITE);

        // Event selection

        JPanel eventPanel = new JPanel(new BorderLayout(5, 7));
        eventPanel.setBackground(WHITE);

        JLabel eventLabel = new JLabel("Select Event");
        eventLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        eventCombo = new JComboBox<>();
        eventCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        eventCombo.setPreferredSize(new Dimension(300, 38));

        eventCombo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof Event) {
                    Event event = (Event) value;

                    setText(
                            event.getEventName()
                            + " (ID: " + event.getEventId() + ")"
                    );
                }

                return this;
            }
        });

        eventPanel.add(eventLabel, BorderLayout.NORTH);
        eventPanel.add(eventCombo, BorderLayout.CENTER);

        // Participant selection

        JPanel participantPanel = new JPanel(new BorderLayout(5, 7));
        participantPanel.setBackground(WHITE);

        JLabel participantLabel = new JLabel("Select Participant");
        participantLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        participantCombo = new JComboBox<>();
        participantCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        participantCombo.setPreferredSize(new Dimension(300, 38));

        participantCombo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof Participant) {
                    Participant participant = (Participant) value;

                    setText(
                            participant.getFullName()
                            + " (ID: " + participant.getParticipantId() + ")"
                    );
                }

                return this;
            }
        });

        participantPanel.add(participantLabel, BorderLayout.NORTH);
        participantPanel.add(participantCombo, BorderLayout.CENTER);

        // Available seats

        JPanel seatsPanel = new JPanel(new BorderLayout(5, 7));
        seatsPanel.setBackground(WHITE);

        JLabel seatsTitle = new JLabel("Available Seats");
        seatsTitle.setFont(new Font("SansSerif", Font.BOLD, 13));

        availableSeatsLabel = new JLabel("Select an event");

        availableSeatsLabel.setFont(
                new Font("SansSerif", Font.BOLD, 17)
        );

        availableSeatsLabel.setForeground(
                new Color(30, 160, 120)
        );

        seatsPanel.add(seatsTitle, BorderLayout.NORTH);
        seatsPanel.add(availableSeatsLabel, BorderLayout.CENTER);

        fields.add(eventPanel);
        fields.add(participantPanel);
        fields.add(seatsPanel);
        fields.add(new JPanel());

        fields.setBackground(WHITE);

        panel.add(fields);
        panel.add(Box.createVerticalStrut(20));

        // Buttons

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(WHITE);

        JButton registerButton = createButton(
                "REGISTER PARTICIPANT",
                BLUE
        );

        JButton refreshButton = createButton(
                "REFRESH",
                new Color(90, 105, 125)
        );

        buttonPanel.add(registerButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel);

        eventCombo.addActionListener(e -> updateAvailableSeats());

        registerButton.addActionListener(e -> registerParticipant());

        refreshButton.addActionListener(e -> refreshAll());

        return panel;
    }

    // ================= BUTTON DESIGN =================

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(color);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setPreferredSize(new Dimension(190, 42));

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    // ================= REGISTRATION TABLE =================

    private JPanel createRegistrationTable() {

        JPanel panel = new JPanel(new BorderLayout(10, 15));

        panel.setBackground(WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)
                )
        );

        JLabel heading = new JLabel("All Registrations");

        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(TEXT);

        panel.add(heading, BorderLayout.NORTH);

        String[] columns = {
                "Registration ID",
                "Event Name",
                "Participant Name",
                "Status",
                "Registration Date",
                "Attendance"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        registrationTable = new JTable(tableModel);

        registrationTable.setRowHeight(32);
        registrationTable.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        registrationTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        registrationTable.getTableHeader().setBackground(NAVY);
        registrationTable.getTableHeader().setForeground(WHITE);

        JScrollPane scrollPane = new JScrollPane(registrationTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Cancel button

        JButton cancelButton = createButton(
                "CANCEL REGISTRATION",
                new Color(205, 65, 65)
        );

        cancelButton.addActionListener(e -> cancelRegistration());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(WHITE);

        bottomPanel.add(cancelButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ================= LOAD EVENTS =================

    private void loadEvents() {

        eventCombo.removeAllItems();

        List<Event> events = eventDAO.getAllEvents();

        for (Event event : events) {
            eventCombo.addItem(event);
        }

        updateAvailableSeats();
    }

    // ================= LOAD PARTICIPANTS =================

    private void loadParticipants() {

        participantCombo.removeAllItems();

        List<Participant> participants =
                participantDAO.getAllParticipants();

        for (Participant participant : participants) {
            participantCombo.addItem(participant);
        }
    }

    // ================= UPDATE AVAILABLE SEATS =================

    private void updateAvailableSeats() {

        Event selectedEvent =
                (Event) eventCombo.getSelectedItem();

        if (selectedEvent == null) {

            availableSeatsLabel.setText("No event selected");
            availableSeatsLabel.setForeground(Color.GRAY);

            return;
        }

        // Refresh event data from database
        List<Event> events = eventDAO.getAllEvents();

        for (Event event : events) {

            if (event.getEventId() == selectedEvent.getEventId()) {

                availableSeatsLabel.setText(
                        String.valueOf(event.getAvailableSeats())
                );

                if (event.getAvailableSeats() > 0) {
                    availableSeatsLabel.setForeground(
                            new Color(30, 160, 120)
                    );
                } else {
                    availableSeatsLabel.setForeground(Color.RED);
                }

                return;
            }
        }

        availableSeatsLabel.setText("Unavailable");
    }

    // ================= REGISTER PARTICIPANT =================

    private void registerParticipant() {

        Event selectedEvent =
                (Event) eventCombo.getSelectedItem();

        Participant selectedParticipant =
                (Participant) participantCombo.getSelectedItem();

        if (selectedEvent == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an event.",
                    "Missing Event",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (selectedParticipant == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a participant.",
                    "Missing Participant",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Register " + selectedParticipant.getFullName()
                + " for " + selectedEvent.getEventName() + "?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean success = registrationDAO.registerParticipant(
                    selectedEvent.getEventId(),
                    selectedParticipant.getParticipantId()
            );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Participant registered successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                refreshAll();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "This participant is already registered "
                        + "for this event.",
                        "Registration Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (EventFullException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Event Full",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "An unexpected error occurred.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= LOAD REGISTRATIONS =================

    private void loadRegistrations() {

        tableModel.setRowCount(0);

        eventNames.clear();
        participantNames.clear();

        List<Event> events = eventDAO.getAllEvents();

        for (Event event : events) {
            eventNames.put(
                    event.getEventId(),
                    event.getEventName()
            );
        }

        List<Participant> participants =
                participantDAO.getAllParticipants();

        for (Participant participant : participants) {
            participantNames.put(
                    participant.getParticipantId(),
                    participant.getFullName()
            );
        }

        List<Registration> registrations =
                registrationDAO.getAllRegistrations();

        for (Registration registration : registrations) {

            String eventName = eventNames.getOrDefault(
                    registration.getEventId(),
                    "Unknown Event"
            );

            String participantName = participantNames.getOrDefault(
                    registration.getParticipantId(),
                    "Unknown Participant"
            );

            String registrationDate = "";

            if (registration.getRegistrationDate() != null) {
                registrationDate =
                        registration.getRegistrationDate().toString()
                                .replace("T", " ");
            }

            tableModel.addRow(new Object[]{

                    registration.getRegistrationId(),

                    eventName,

                    participantName,

                    registration.getStatus(),

                    registrationDate,

                    registration.isAttendance() ? "Present" : "Not Marked"
            });
        }
    }

    // ================= CANCEL REGISTRATION =================

    private void cancelRegistration() {

        int selectedRow = registrationTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a registration from the table.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int registrationId = (int) tableModel.getValueAt(
                selectedRow,
                0
        );

        String status = String.valueOf(
                tableModel.getValueAt(selectedRow, 3)
        );

        if (!"REGISTERED".equalsIgnoreCase(status)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Only active registrations can be cancelled.",
                    "Invalid Operation",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this registration?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success =
                registrationDAO.cancelRegistration(registrationId);

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Registration cancelled successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refreshAll();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to cancel registration.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= REFRESH =================

    private void refreshAll() {

        loadEvents();
        loadParticipants();
        loadRegistrations();

    }
}
