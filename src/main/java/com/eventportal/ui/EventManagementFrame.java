
package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.eventportal.dao.EventDAO;
import com.eventportal.model.Event;

public class EventManagementFrame extends JFrame {

    private static final Color NAVY = new Color(20, 35, 65);
    private static final Color BLUE = new Color(35, 90, 200);
    private static final Color BACKGROUND = new Color(242, 245, 250);
    private static final Color TEXT = new Color(45, 55, 75);

    private JTextField nameField;
    private JComboBox<String> categoryBox;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField venueField;
    private JTextField seatsField;
    private JTextArea descriptionArea;

    private JTable eventTable;
    private DefaultTableModel tableModel;

    private final EventDAO eventDAO = new EventDAO();

    public EventManagementFrame() {

        setTitle("Event Management");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadEvents();
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Event Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Create and manage college events");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(195, 205, 225));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new javax.swing.BoxLayout(
                textPanel, javax.swing.BoxLayout.Y_AXIS
        ));

        textPanel.add(title);
        textPanel.add(javax.swing.Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        return header;
    }

    private JPanel createMainPanel() {

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(BACKGROUND);
        main.setBorder(BorderFactory.createEmptyBorder(22, 25, 22, 25));

        main.add(createFormPanel(), BorderLayout.NORTH);
        main.add(createTablePanel(), BorderLayout.CENTER);

        return main;
    }

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel heading = new JLabel("Create New Event");
        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(NAVY);

        panel.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(3, 4, 15, 12));
        fields.setBackground(Color.WHITE);

        nameField = createTextField();

        categoryBox = new JComboBox<>(new String[]{
                "Technical",
                "Cultural",
                "Sports",
                "Workshop",
                "Seminar",
                "Other"
        });

        dateField = createTextField();
        timeField = createTextField();
        venueField = createTextField();
        seatsField = createTextField();

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        fields.add(createField("Event Name", nameField));
        fields.add(createField("Category", categoryBox));
        fields.add(createField("Date (YYYY-MM-DD)", dateField));
        fields.add(createField("Time (HH:MM)", timeField));
        fields.add(createField("Venue", venueField));
        fields.add(createField("Total Seats", seatsField));

        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 5));
        descriptionPanel.setBackground(Color.WHITE);

        JLabel descriptionLabel = new JLabel("Description");
        descriptionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        descriptionLabel.setForeground(TEXT);

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(250, 65));

        descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
        descriptionPanel.add(descriptionScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(15, 10));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(descriptionPanel, BorderLayout.CENTER);

        // SAVE EVENT BUTTON
        JButton saveButton = new JButton("SAVE EVENT");

        saveButton.setBackground(BLUE);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true);
        saveButton.setContentAreaFilled(true);

        saveButton.setPreferredSize(new Dimension(150, 42));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> saveEvent());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(fields, BorderLayout.NORTH);
        centerPanel.add(bottomPanel, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel heading = new JLabel("All Events");
        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(NAVY);

        panel.add(heading, BorderLayout.NORTH);

        String[] columns = {
                "ID",
                "Event Name",
                "Category",
                "Date",
                "Time",
                "Venue",
                "Total Seats",
                "Available Seats"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(30);
        eventTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        eventTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        eventTable.getTableHeader().setBackground(NAVY);
        eventTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(eventTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createTextField() {

        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(150, 32));

        return field;
    }

    private JPanel createField(String labelText, JComponent component) {

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    // ================= SAVE EVENT =================

    private void saveEvent() {

        String name = nameField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();
        String venue = venueField.getText().trim();
        String seats = seatsField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty() || date.isEmpty() || time.isEmpty()
                || venue.isEmpty() || seats.isEmpty()
                || description.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill all the fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            LocalDate eventDate = LocalDate.parse(date);
            LocalTime eventTime = LocalTime.parse(time);

            if (eventDate.isBefore(LocalDate.now())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Event date cannot be in the past.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int totalSeats = Integer.parseInt(seats);

            if (totalSeats <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Total seats must be greater than zero.",
                        "Invalid Seats",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            Event event = new Event(
                    name,
                    description,
                    category,
                    eventDate,
                    eventTime,
                    venue,
                    totalSeats
            );

            boolean success = eventDAO.addEvent(event);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Event created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                clearForm();
                loadEvents();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to save event. Please check database connection.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter date as YYYY-MM-DD and time as HH:MM.",
                    "Invalid Date or Time",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number of seats.",
                    "Invalid Seats",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // ================= LOAD EVENTS =================

    private void loadEvents() {

        tableModel.setRowCount(0);

        List<Event> events = eventDAO.getAllEvents();

        for (Event event : events) {

            Object[] row = {
                    event.getEventId(),
                    event.getEventName(),
                    event.getCategory(),
                    event.getEventDate(),
                    event.getEventTime(),
                    event.getVenue(),
                    event.getTotalSeats(),
                    event.getAvailableSeats()
            };

            tableModel.addRow(row);
        }
    }

    // ================= CLEAR FORM =================

    private void clearForm() {

        nameField.setText("");
        categoryBox.setSelectedIndex(0);
        dateField.setText("");
        timeField.setText("");
        venueField.setText("");
        seatsField.setText("");
        descriptionArea.setText("");
    }
}
