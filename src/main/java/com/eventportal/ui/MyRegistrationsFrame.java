package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.Event;
import com.eventportal.model.Participant;
import com.eventportal.model.Registration;
import com.eventportal.service.StudentService;

/**
 * My Registrations: ONLY the logged-in student's registrations,
 * with status, attendance and a Cancel button when cancelling is allowed.
 */
public class MyRegistrationsFrame extends JFrame {

    private static final String FILTER_ALL = "All";
    private static final String FILTER_REGISTERED = "Registered";
    private static final String FILTER_CANCELLED = "Cancelled";

    private final Participant student;
    private final StudentService service;

    private JComboBox<String> filterCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel hintLabel;
    private JButton cancelButton;

    private List<Registration> allRegistrations = new ArrayList<>();
    private Map<Integer, Event> eventsById = new HashMap<>();
    private final List<Registration> shownRegistrations = new ArrayList<>();

    public MyRegistrationsFrame(Participant student) {
        this(student, new StudentService());
    }

    public MyRegistrationsFrame(Participant student,
                                StudentService service) {

        this.student = student;
        this.service = service;

        setTitle("My Registrations");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new StudentSidebar(this, student, service,
                StudentSidebar.REGISTRATIONS), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadData();
    }

    // ================= LAYOUT =================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(StudentTheme.BACKGROUND);

        main.add(StudentTheme.pageHeader("My Registrations"),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(StudentTheme.BACKGROUND);
        body.setBorder(new EmptyBorder(25, 30, 25, 30));

        body.add(createFilterCard(), BorderLayout.NORTH);
        body.add(createTableCard(), BorderLayout.CENTER);
        body.add(createActionCard(), BorderLayout.SOUTH);

        main.add(body, BorderLayout.CENTER);

        return main;
    }

    private JPanel createFilterCard() {

        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        card.setBackground(StudentTheme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StudentTheme.BORDER),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel label = new JLabel("Show");
        label.setFont(StudentTheme.font(Font.BOLD, 13));
        label.setForeground(StudentTheme.TEXT);

        filterCombo = new JComboBox<>(new String[]{
                FILTER_ALL, FILTER_REGISTERED, FILTER_CANCELLED});
        filterCombo.setFont(StudentTheme.font(Font.PLAIN, 13));
        filterCombo.setPreferredSize(new Dimension(170, 36));
        filterCombo.addActionListener(e -> applyFilter());

        JButton refresh = StudentTheme.primaryButton("REFRESH");
        refresh.setPreferredSize(new Dimension(110, 36));
        refresh.addActionListener(e -> loadData());

        card.add(label);
        card.add(filterCombo);
        card.add(refresh);

        return card;
    }

    private JPanel createTableCard() {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(StudentTheme.WHITE);
        card.setBorder(BorderFactory.createLineBorder(StudentTheme.BORDER));

        String[] columns = {
                "Event", "Event Date", "Registered On",
                "Status", "Attendance"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        StudentTheme.styleTable(table, 34);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setPreferredWidth(260);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);

        table.getColumnModel().getColumn(3)
                .setCellRenderer(new StatusRenderer());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateCancelState();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(StudentTheme.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActionCard() {

        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(StudentTheme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StudentTheme.BORDER),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        hintLabel = new JLabel("Select a registration to see "
                + "available actions.");
        hintLabel.setFont(StudentTheme.font(Font.PLAIN, 13));
        hintLabel.setForeground(StudentTheme.MUTED);

        cancelButton = StudentTheme.dangerButton("CANCEL REGISTRATION");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelSelected());

        card.add(hintLabel, BorderLayout.CENTER);
        card.add(cancelButton, BorderLayout.EAST);

        return card;
    }

    // ================= LOADING =================

    private void loadData() {

        try {

            allRegistrations = service.getMyRegistrations(student);
            eventsById = service.getEventsById();

        } catch (StudentServiceException e) {

            allRegistrations = new ArrayList<>();
            eventsById = new HashMap<>();

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Could Not Load Registrations",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        applyFilter();
    }

    private void applyFilter() {

        String filter = (String) filterCombo.getSelectedItem();

        shownRegistrations.clear();
        tableModel.setRowCount(0);

        for (Registration registration : allRegistrations) {

            String status = registration.getStatus();

            if (FILTER_REGISTERED.equals(filter)
                    && !StudentService.STATUS_REGISTERED.equals(status)) {
                continue;
            }

            if (FILTER_CANCELLED.equals(filter)
                    && !StudentService.STATUS_CANCELLED.equals(status)) {
                continue;
            }

            Event event = eventsById.get(registration.getEventId());

            shownRegistrations.add(registration);

            tableModel.addRow(new Object[]{
                    event == null ? "Event #" + registration.getEventId()
                            : event.getEventName(),
                    event == null ? ""
                            : service.formatDate(event.getEventDate()),
                    service.formatDateTime(
                            registration.getRegistrationDate()),
                    service.prettyStatus(status),
                    attendanceText(registration)
            });
        }

        updateCancelState();
    }

    private String attendanceText(Registration registration) {

        if (StudentService.STATUS_CANCELLED.equals(
                registration.getStatus())) {
            return "-";
        }

        return registration.isAttendance() ? "Present" : "Not marked";
    }

    // ================= CANCEL =================

    private Registration selectedRegistration() {

        int row = table.getSelectedRow();

        if (row < 0 || row >= shownRegistrations.size()) {
            return null;
        }

        return shownRegistrations.get(row);
    }

    private void updateCancelState() {

        Registration registration = selectedRegistration();

        if (registration == null) {

            hintLabel.setText(shownRegistrations.isEmpty()
                    ? "You have no registrations to show."
                    : "Select a registration to see available actions.");
            hintLabel.setForeground(StudentTheme.MUTED);
            cancelButton.setEnabled(false);
            return;
        }

        String reason = service.getCancelBlockReason(
                registration, eventsById.get(registration.getEventId()));

        if (reason == null) {
            hintLabel.setText("You can cancel this registration. "
                    + "Your seat will be released.");
            hintLabel.setForeground(StudentTheme.MUTED);
        } else {
            hintLabel.setText(reason);
            hintLabel.setForeground(Color.GRAY);
        }

        cancelButton.setEnabled(reason == null);
    }

    private void cancelSelected() {

        Registration registration = selectedRegistration();

        if (registration == null) {
            return;
        }

        Event event = eventsById.get(registration.getEventId());

        String name = event == null
                ? "this event" : "\"" + event.getEventName() + "\"";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Cancel your registration for " + name + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            service.cancelRegistration(
                    student, registration.getRegistrationId());

            JOptionPane.showMessageDialog(
                    this,
                    "Your registration has been cancelled.",
                    "Cancelled",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (StudentServiceException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Cancellation Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        loadData();
    }

    // ================= STATUS COLOURS =================

    private static class StatusRenderer extends DefaultTableCellRenderer {

        StatusRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            setFont(StudentTheme.font(Font.BOLD, 12));

            setForeground("Registered".equals(String.valueOf(value))
                    ? StudentTheme.GREEN : StudentTheme.RED);

            return this;
        }
    }
}
