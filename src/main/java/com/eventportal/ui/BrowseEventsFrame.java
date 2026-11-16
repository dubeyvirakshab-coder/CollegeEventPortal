package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.Event;
import com.eventportal.model.Participant;
import com.eventportal.model.Registration;
import com.eventportal.service.StudentService;

/**
 * Browse Events: shows every event from the existing events table,
 * with search, category filter and a Register button.
 *
 * The rules (no duplicates, no full or past events) are decided by
 * StudentService, and the database update uses the existing
 * transaction in RegistrationDAO.
 */
public class BrowseEventsFrame extends JFrame {

    private static final String ALL_CATEGORIES = "All Categories";

    private final Participant student;
    private final StudentService service;

    // Filter controls
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JCheckBox showPastBox;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    // Detail panel
    private JLabel detailTitle;
    private JLabel detailMeta;
    private JTextArea detailDescription;
    private JLabel detailReason;
    private JButton registerButton;

    // Data
    private List<Event> allEvents = new ArrayList<>();
    private final List<Event> shownEvents = new ArrayList<>();
    private final Set<Integer> myActiveEventIds = new HashSet<>();

    private boolean loadingCategories = false;

    public BrowseEventsFrame(Participant student) {
        this(student, new StudentService());
    }

    public BrowseEventsFrame(Participant student, StudentService service) {

        this.student = student;
        this.service = service;

        setTitle("Browse Events");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new StudentSidebar(this, student, service,
                StudentSidebar.BROWSE), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        loadData();
    }

    // ================= LAYOUT =================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(StudentTheme.BACKGROUND);

        main.add(StudentTheme.pageHeader("Browse Events"),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setBackground(StudentTheme.BACKGROUND);
        body.setBorder(new EmptyBorder(25, 30, 25, 30));

        body.add(createFilterCard(), BorderLayout.NORTH);
        body.add(createTableCard(), BorderLayout.CENTER);
        body.add(createDetailCard(), BorderLayout.SOUTH);

        main.add(body, BorderLayout.CENTER);

        return main;
    }

    private JPanel createFilterCard() {

        JPanel card = StudentTheme.borderedCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StudentTheme.BORDER),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(StudentTheme.font(Font.BOLD, 13));
        searchLabel.setForeground(StudentTheme.TEXT);

        searchField = new JTextField();
        searchField.setFont(StudentTheme.font(Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.setToolTipText("Search by name, venue, category "
                + "or description");

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        applyFilters();
                    }
                });

        JLabel categoryLabel = new JLabel("Category");
        categoryLabel.setFont(StudentTheme.font(Font.BOLD, 13));
        categoryLabel.setForeground(StudentTheme.TEXT);

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(StudentTheme.font(Font.PLAIN, 13));
        categoryCombo.setPreferredSize(new Dimension(170, 36));
        categoryCombo.addActionListener(e -> {
            if (!loadingCategories) {
                applyFilters();
            }
        });

        showPastBox = new JCheckBox("Show past events");
        showPastBox.setFont(StudentTheme.font(Font.PLAIN, 13));
        showPastBox.setBackground(StudentTheme.WHITE);
        showPastBox.setFocusPainted(false);
        showPastBox.addActionListener(e -> applyFilters());

        JButton refresh = StudentTheme.primaryButton("REFRESH");
        refresh.setPreferredSize(new Dimension(110, 36));
        refresh.addActionListener(e -> loadData());

        card.add(searchLabel);
        card.add(searchField);
        card.add(categoryLabel);
        card.add(categoryCombo);
        card.add(showPastBox);
        card.add(refresh);

        return card;
    }

    private JPanel createTableCard() {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(StudentTheme.WHITE);
        card.setBorder(BorderFactory.createLineBorder(StudentTheme.BORDER));

        String[] columns = {
                "Event", "Category", "Date", "Time",
                "Venue", "Seats Left", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        StudentTheme.styleTable(table, 34);

        table.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);

        table.getColumnModel().getColumn(6)
                .setCellRenderer(new StatusRenderer());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedEvent();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(StudentTheme.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDetailCard() {

        JPanel card = StudentTheme.borderedCard();
        card.setLayout(new BorderLayout(25, 0));
        card.setPreferredSize(new Dimension(100, 175));

        // Left: title, meta line, description
        JPanel text = new JPanel();
        text.setBackground(StudentTheme.WHITE);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        detailTitle = new JLabel("Select an event");
        detailTitle.setFont(StudentTheme.font(Font.BOLD, 17));
        detailTitle.setForeground(StudentTheme.TEXT);
        detailTitle.setAlignmentX(LEFT_ALIGNMENT);

        detailMeta = new JLabel(" ");
        detailMeta.setFont(StudentTheme.font(Font.BOLD, 12));
        detailMeta.setForeground(StudentTheme.BLUE);
        detailMeta.setAlignmentX(LEFT_ALIGNMENT);

        detailDescription = new JTextArea(
                "Click an event in the table to see its description.");
        detailDescription.setFont(StudentTheme.font(Font.PLAIN, 13));
        detailDescription.setForeground(StudentTheme.MUTED);
        detailDescription.setBackground(StudentTheme.WHITE);
        detailDescription.setEditable(false);
        detailDescription.setLineWrap(true);
        detailDescription.setWrapStyleWord(true);
        detailDescription.setBorder(null);

        JScrollPane descriptionScroll = new JScrollPane(detailDescription);
        descriptionScroll.setBorder(null);
        descriptionScroll.setAlignmentX(LEFT_ALIGNMENT);

        text.add(detailTitle);
        text.add(Box.createVerticalStrut(6));
        text.add(detailMeta);
        text.add(Box.createVerticalStrut(10));
        text.add(descriptionScroll);

        // Right: register button and the reason it may be disabled
        JPanel action = new JPanel();
        action.setBackground(StudentTheme.WHITE);
        action.setLayout(new BoxLayout(action, BoxLayout.Y_AXIS));
        action.setPreferredSize(new Dimension(250, 100));

        registerButton = StudentTheme.primaryButton("REGISTER FOR EVENT");
        registerButton.setAlignmentX(LEFT_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(250, 42));
        registerButton.setEnabled(false);
        registerButton.addActionListener(e -> registerSelected());

        detailReason = new JLabel(" ");
        detailReason.setFont(StudentTheme.font(Font.PLAIN, 12));
        detailReason.setForeground(Color.GRAY);
        detailReason.setAlignmentX(LEFT_ALIGNMENT);

        action.add(Box.createVerticalGlue());
        action.add(registerButton);
        action.add(Box.createVerticalStrut(10));
        action.add(detailReason);
        action.add(Box.createVerticalGlue());

        card.add(text, BorderLayout.CENTER);
        card.add(action, BorderLayout.EAST);

        return card;
    }

    // ================= LOADING AND FILTERING =================

    /** Loads events and this student's registrations from the database. */
    private void loadData() {

        int previouslySelectedId = selectedEventId();

        try {

            allEvents = service.getAllEvents();

            myActiveEventIds.clear();

            for (Registration registration
                    : service.getMyRegistrations(student)) {

                if (StudentService.STATUS_REGISTERED
                        .equals(registration.getStatus())) {
                    myActiveEventIds.add(registration.getEventId());
                }
            }

        } catch (StudentServiceException e) {

            allEvents = new ArrayList<>();
            myActiveEventIds.clear();

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Could Not Load Events",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        fillCategories();
        applyFilters();
        reselect(previouslySelectedId);
    }

    private void fillCategories() {

        String current = (String) categoryCombo.getSelectedItem();

        loadingCategories = true;

        categoryCombo.removeAllItems();
        categoryCombo.addItem(ALL_CATEGORIES);

        for (String category : service.getCategories(allEvents)) {
            categoryCombo.addItem(category);
        }

        if (current != null) {
            categoryCombo.setSelectedItem(current);
        }

        loadingCategories = false;
    }

    private void applyFilters() {

        String search = searchField.getText().trim().toLowerCase();
        String category = (String) categoryCombo.getSelectedItem();
        boolean showPast = showPastBox.isSelected();

        shownEvents.clear();
        tableModel.setRowCount(0);

        for (Event event : allEvents) {

            if (!showPast && service.isPast(event)) {
                continue;
            }

            if (category != null && !ALL_CATEGORIES.equals(category)
                    && !category.equalsIgnoreCase(event.getCategory())) {
                continue;
            }

            if (!search.isEmpty() && !matches(event, search)) {
                continue;
            }

            shownEvents.add(event);

            tableModel.addRow(new Object[]{
                    event.getEventName(),
                    event.getCategory(),
                    service.formatDate(event.getEventDate()),
                    event.getEventTime() == null
                            ? "" : event.getEventTime().toString(),
                    event.getVenue(),
                    event.getAvailableSeats() + " / "
                            + event.getTotalSeats(),
                    statusOf(event)
            });
        }

        showSelectedEvent();
    }

    private boolean matches(Event event, String search) {

        return contains(event.getEventName(), search)
                || contains(event.getCategory(), search)
                || contains(event.getVenue(), search)
                || contains(event.getDescription(), search);
    }

    private boolean contains(String text, String search) {
        return text != null && text.toLowerCase().contains(search);
    }

    private String statusOf(Event event) {

        if (myActiveEventIds.contains(event.getEventId())) {
            return "Registered";
        }

        if (service.isPast(event)) {
            return "Closed";
        }

        if (event.getAvailableSeats() <= 0) {
            return "Full";
        }

        return "Open";
    }

    // ================= SELECTION =================

    private Event selectedEvent() {

        int row = table.getSelectedRow();

        if (row < 0 || row >= shownEvents.size()) {
            return null;
        }

        return shownEvents.get(row);
    }

    private int selectedEventId() {

        Event event = table == null ? null : selectedEvent();

        return event == null ? -1 : event.getEventId();
    }

    private void reselect(int eventId) {

        if (eventId < 0) {
            return;
        }

        for (int i = 0; i < shownEvents.size(); i++) {
            if (shownEvents.get(i).getEventId() == eventId) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private void showSelectedEvent() {

        Event event = selectedEvent();

        if (event == null) {

            detailTitle.setText(shownEvents.isEmpty()
                    ? "No events found" : "Select an event");
            detailMeta.setText(" ");
            detailDescription.setText(shownEvents.isEmpty()
                    ? "Try a different search or category, or tick "
                    + "\"Show past events\"."
                    : "Click an event in the table to see its description.");
            detailReason.setText(" ");
            registerButton.setEnabled(false);
            return;
        }

        detailTitle.setText(event.getEventName());

        detailMeta.setText(event.getCategory() + "   |   "
                + service.formatDate(event.getEventDate()) + " at "
                + (event.getEventTime() == null
                ? "" : event.getEventTime().toString())
                + "   |   " + event.getVenue());

        String description = event.getDescription();

        detailDescription.setText(
                description == null || description.isBlank()
                        ? "No description provided." : description);
        detailDescription.setCaretPosition(0);

        String reason = service.getRegistrationBlockReason(
                event, myActiveEventIds.contains(event.getEventId()));

        registerButton.setEnabled(reason == null);
        detailReason.setText(reason == null
                ? event.getAvailableSeats() + " seat(s) available"
                : reason);
    }

    // ================= REGISTER =================

    private void registerSelected() {

        Event event = selectedEvent();

        if (event == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Register for \"" + event.getEventName() + "\" on "
                        + service.formatDate(event.getEventDate()) + "?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            service.registerForEvent(student, event);

            JOptionPane.showMessageDialog(
                    this,
                    "You are registered for \"" + event.getEventName()
                            + "\".",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (StudentServiceException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Registration Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        // Reload so seats and statuses are up to date either way
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

            String status = String.valueOf(value);

            setFont(StudentTheme.font(Font.BOLD, 12));

            switch (status) {
                case "Open":
                    setForeground(StudentTheme.GREEN);
                    break;
                case "Registered":
                    setForeground(StudentTheme.BLUE);
                    break;
                case "Full":
                    setForeground(StudentTheme.RED);
                    break;
                default:
                    setForeground(Color.GRAY);
                    break;
            }

            return this;
        }
    }
}
