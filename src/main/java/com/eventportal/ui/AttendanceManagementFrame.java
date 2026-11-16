
package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.eventportal.dao.AttendanceDAO;
import com.eventportal.dao.EventDAO;
import com.eventportal.model.Event;

public class AttendanceManagementFrame extends JFrame {

    private final Color NAVY = new Color(20, 32, 55);
    private final Color BLUE = new Color(48, 110, 220);
    private final Color BACKGROUND = new Color(242, 245, 250);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(45, 55, 72);

    private final EventDAO eventDAO = new EventDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private JComboBox<Event> eventComboBox;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    private JLabel participantCountLabel;

    public AttendanceManagementFrame() {

        setTitle("Attendance Management");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadEvents();

        setVisible(true);
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(
                25, 30, 25, 30
        ));

        JLabel title = new JLabel("Attendance Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel("Manage and track event attendance");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(190, 205, 225));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        return header;
    }

    // ================= MAIN PANEL =================

    private JPanel createMainPanel() {

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BACKGROUND);
        main.setBorder(BorderFactory.createEmptyBorder(
                25, 30, 25, 30
        ));

        main.add(createSelectionPanel(), BorderLayout.NORTH);
        main.add(createTablePanel(), BorderLayout.CENTER);
        main.add(createButtonPanel(), BorderLayout.SOUTH);

        return main;
    }

    // ================= EVENT SELECTION =================

    private JPanel createSelectionPanel() {

        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 15, 15
        ));

        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(
                8, 10, 8, 10
        ));

        JLabel eventLabel = new JLabel("Select Event:");
        eventLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        eventLabel.setForeground(TEXT);

        eventComboBox = new JComboBox<>();

        eventComboBox.setPreferredSize(new Dimension(300, 38));
        eventComboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        eventComboBox.setRenderer((list, event, index, selected, focus) -> {

            JLabel label = new JLabel();

            if (event != null) {
                label.setText(event.getEventName());
            }

            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

            if (selected) {
                label.setBackground(new Color(220, 232, 250));
            } else {
                label.setBackground(WHITE);
            }

            return label;
        });

        JButton loadButton = new JButton("Load Participants");
        styleButton(loadButton, BLUE);

        loadButton.addActionListener(e -> loadParticipants());

        panel.add(eventLabel);
        panel.add(eventComboBox);
        panel.add(loadButton);

        return panel;
    }

    // ================= TABLE PANEL =================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(
                20, 20, 20, 20
        ));

        JLabel heading = new JLabel("Registered Participants");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(TEXT);

        participantCountLabel = new JLabel("Participants: 0");
        participantCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        participantCountLabel.setForeground(Color.GRAY);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);

        top.add(heading, BorderLayout.WEST);
        top.add(participantCountLabel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Registration ID",
                        "Participant ID",
                        "Participant Name",
                        "Email",
                        "Present"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) {
                    return Boolean.class;
                }

                if (columnIndex == 0 || columnIndex == 1) {
                    return Integer.class;
                }

                return String.class;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(35);
        attendanceTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        attendanceTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        attendanceTable.getTableHeader().setBackground(
                new Color(230, 235, 245)
        );

        attendanceTable.setSelectionBackground(
                new Color(220, 232, 250)
        );

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(225, 230, 238)
        ));

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ================= BUTTON PANEL =================

    private JPanel createButtonPanel() {

        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 12, 10
        ));

        panel.setBackground(BACKGROUND);

        JButton saveButton = new JButton("Save Attendance");
        JButton reportButton = new JButton("Generate Report");
        JButton refreshButton = new JButton("Refresh");

        styleButton(saveButton, BLUE);
        styleButton(reportButton, new Color(30, 150, 115));
        styleButton(refreshButton, new Color(100, 110, 125));

        saveButton.addActionListener(e -> saveAttendance());
        reportButton.addActionListener(e -> generateReport());
        refreshButton.addActionListener(e -> loadParticipants());

        panel.add(refreshButton);
        panel.add(reportButton);
        panel.add(saveButton);

        return panel;
    }

    // ================= LOAD EVENTS =================

    private void loadEvents() {

        eventComboBox.removeAllItems();

        List<Event> events = eventDAO.getAllEvents();

        for (Event event : events) {
            eventComboBox.addItem(event);
        }

        if (eventComboBox.getItemCount() == 0) {

            participantCountLabel.setText("No events available");

        } else {

            loadParticipants();
        }
    }

    // ================= LOAD PARTICIPANTS =================

    private void loadParticipants() {

        Event selectedEvent =
                (Event) eventComboBox.getSelectedItem();

        if (selectedEvent == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an event first."
            );
            return;
        }

        tableModel.setRowCount(0);

        List<Object[]> participants =
                attendanceDAO.getEventParticipants(
                        selectedEvent.getEventId()
                );

        for (Object[] participant : participants) {
            tableModel.addRow(participant);
        }

        participantCountLabel.setText(
                "Participants: " + participants.size()
        );
    }

    // ================= SAVE ATTENDANCE =================

    private void saveAttendance() {

        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        if (tableModel.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "No registered participants to update."
            );

            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save attendance for this event?",
                "Confirm Attendance",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        int successCount = 0;
        int failedCount = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            int registrationId =
                    (Integer) tableModel.getValueAt(i, 0);

            boolean present =
                    (Boolean) tableModel.getValueAt(i, 4);

            if (attendanceDAO.updateAttendance(
                    registrationId, present)) {

                successCount++;

            } else {

                failedCount++;
            }
        }

        if (failedCount == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance saved successfully!\n" +
                    "Updated participants: " + successCount,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Updated: " + successCount +
                    "\nFailed: " + failedCount,
                    "Attendance Result",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        loadParticipants();
    }

    // ================= GENERATE REPORT =================

    private void generateReport() {

        Event selectedEvent =
                (Event) eventComboBox.getSelectedItem();

        if (selectedEvent == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an event first."
            );

            return;
        }

        Object[] report =
                attendanceDAO.generateEventReport(
                        selectedEvent.getEventId()
                );

        int total = ((Number) report[0]).intValue();
        int present = ((Number) report[1]).intValue();
        int absent = ((Number) report[2]).intValue();
        double percentage = ((Number) report[3]).doubleValue();

        String message =
                "EVENT ATTENDANCE REPORT\n\n" +
                "Event: " + selectedEvent.getEventName() + "\n\n" +
                "Total Registrations: " + total + "\n" +
                "Present: " + present + "\n" +
                "Absent / Not Marked: " + absent + "\n" +
                "Attendance Percentage: " +
                String.format("%.2f", percentage) + "%";

        JOptionPane.showMessageDialog(
                this,
                message,
                "Event Report",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= BUTTON STYLING =================

    private void styleButton(JButton button, Color color) {

        button.setBackground(color);
        button.setForeground(WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(
                11, 18, 11, 18
        ));
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(
                java.awt.Cursor.HAND_CURSOR
        ));
    }
}
