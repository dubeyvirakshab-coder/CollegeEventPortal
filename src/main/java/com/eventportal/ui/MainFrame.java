
package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.eventportal.dao.DashboardDAO;

public class MainFrame extends JFrame {

    private final Color NAVY = new Color(20, 32, 55);
    private final Color BLUE = new Color(48, 110, 220);
    private final Color BACKGROUND = new Color(242, 245, 250);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(45, 55, 72);

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public MainFrame() {

        setTitle("College Event Registration Portal");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        setVisible(true);
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(235, 800));
        sidebar.setBackground(NAVY);
        sidebar.setLayout(new BorderLayout());

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(NAVY);
        logoPanel.setBorder(new EmptyBorder(30, 15, 30, 15));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("EVENT PORTAL");
        logo.setForeground(WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("MANAGEMENT SYSTEM");
        subtitle.setForeground(new Color(170, 185, 210));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        logoPanel.add(logo);
        logoPanel.add(Box.createVerticalStrut(8));
        logoPanel.add(subtitle);

        JPanel menu = new JPanel();
        menu.setBackground(NAVY);
        menu.setBorder(new EmptyBorder(15, 12, 15, 12));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        menu.add(createNavButton("Dashboard", true));
        menu.add(createNavButton("Manage Events", false));
        menu.add(createNavButton("Participants", false));
        menu.add(createNavButton("Registrations", false));
        menu.add(createNavButton("Attendance & Reports", false));

        menu.add(Box.createVerticalStrut(25));

        menu.add(createNavButton("Settings", false));
        menu.add(createNavButton("Logout", false));

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(menu, BorderLayout.CENTER);

        return sidebar;
    }

    private JButton createNavButton(String text, boolean active) {

        JButton button = new JButton(text);

        button.setMaximumSize(new Dimension(210, 45));
        button.setPreferredSize(new Dimension(210, 45));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 18, 0, 0));

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(active ? BLUE : NAVY);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {

            if (text.equals("Dashboard")) {
                return;
            }

            if (text.equals("Logout")) {

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to logout?",
                        "Logout",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                }

                return;
            }

            if (text.equals("Manage Events")) {

                new EventManagementFrame().setVisible(true);

            } else if (text.equals("Participants")) {

                new ParticipantManagementFrame().setVisible(true);

            } else if (text.equals("Registrations")) {

                new RegistrationManagementFrame().setVisible(true);

            } else if (text.equals("Attendance & Reports")) {

                new AttendanceManagementFrame().setVisible(true);

            } else {

                showModuleMessage(text);
            }
        });

        return button;
    }

    // ================= MAIN CONTENT =================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BACKGROUND);

        main.add(createHeader(), BorderLayout.NORTH);

        JPanel dashboard = createDashboard();

        JScrollPane scrollPane = new JScrollPane(dashboard);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

        main.add(scrollPane, BorderLayout.CENTER);

        return main;
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(TEXT);

        JLabel admin = new JLabel("ADMIN PANEL");
        admin.setFont(new Font("SansSerif", Font.BOLD, 12));
        admin.setForeground(BLUE);

        header.add(title, BorderLayout.WEST);
        header.add(admin, BorderLayout.EAST);

        return header;
    }

    // ================= DASHBOARD =================

    private JPanel createDashboard() {

        JPanel dashboard = new JPanel();
        dashboard.setBackground(BACKGROUND);
        dashboard.setBorder(new EmptyBorder(25, 30, 30, 30));
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome to Event Management");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcome.setForeground(TEXT);

        JLabel description = new JLabel(
                "Manage events, participants, registrations and attendance."
        );
        description.setFont(new Font("SansSerif", Font.PLAIN, 13));
        description.setForeground(Color.GRAY);

        dashboard.add(welcome);
        dashboard.add(Box.createVerticalStrut(8));
        dashboard.add(description);
        dashboard.add(Box.createVerticalStrut(25));

        // Statistics cards

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        statsPanel.setBackground(BACKGROUND);

        statsPanel.add(createStatCard(
                "TOTAL EVENTS",
                String.valueOf(dashboardDAO.getTotalEvents()),
                BLUE
        ));

        statsPanel.add(createStatCard(
                "PARTICIPANTS",
                String.valueOf(dashboardDAO.getTotalParticipants()),
                new Color(30, 160, 120)
        ));

        statsPanel.add(createStatCard(
                "REGISTRATIONS",
                String.valueOf(dashboardDAO.getTotalRegistrations()),
                new Color(230, 145, 40)
        ));

        statsPanel.add(createStatCard(
                "ATTENDANCE MARKED",
                String.valueOf(dashboardDAO.getPresentCount()),
                new Color(145, 90, 200)
        ));

        dashboard.add(statsPanel);
        dashboard.add(Box.createVerticalStrut(30));

        // Information panels

        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        infoPanel.setBackground(BACKGROUND);

        infoPanel.add(createUpcomingEventsPanel());
        infoPanel.add(createRecentRegistrationsPanel());

        dashboard.add(infoPanel);
        dashboard.add(Box.createVerticalStrut(30));

        // Quick actions

        JLabel quickTitle = new JLabel("Quick Actions");
        quickTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        quickTitle.setForeground(TEXT);

        dashboard.add(quickTitle);
        dashboard.add(Box.createVerticalStrut(15));
        dashboard.add(createQuickActions());

        return dashboard;
    }

    // ================= STAT CARD =================

    private JPanel createStatCard(String title, String value, Color color) {

        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color),
                new EmptyBorder(20, 18, 20, 18)
        ));

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueLabel.setForeground(TEXT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);

        return card;
    }

    // ================= UPCOMING EVENTS =================

    private JPanel createUpcomingEventsPanel() {

        JPanel panel = createInfoPanel("Upcoming Events");

        List<String> events = dashboardDAO.getUpcomingEvents();

        if (events.isEmpty()) {

            panel.add(createInfoLabel("No upcoming events found."));

        } else {

            for (String event : events) {
                panel.add(createInfoLabel(event));
            }
        }

        return panel;
    }

    // ================= RECENT REGISTRATIONS =================

    private JPanel createRecentRegistrationsPanel() {

        JPanel panel = createInfoPanel("Recent Registrations");

        List<String> registrations =
                dashboardDAO.getRecentRegistrations();

        if (registrations.isEmpty()) {

            panel.add(createInfoLabel("No registrations found."));

        } else {

            for (String registration : registrations) {
                panel.add(createInfoLabel(registration));
            }
        }

        return panel;
    }

    // ================= INFO PANEL =================

    private JPanel createInfoPanel(String title) {

        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        heading.setForeground(TEXT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(18));

        return panel;
    }

    private JLabel createInfoLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(90, 100, 115));
        label.setBorder(new EmptyBorder(7, 0, 7, 0));

        return label;
    }

    // ================= QUICK ACTIONS =================

    private JPanel createQuickActions() {

        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 12));
        panel.setBackground(BACKGROUND);

        panel.add(createActionButton(
                "＋ Create Event",
                "Event Management"
        ));

        panel.add(createActionButton(
                "＋ Add Participant",
                "Participant Management"
        ));

        panel.add(createActionButton(
                "▤ View Registrations",
                "Registration Management"
        ));

        panel.add(createActionButton(
                "✓ Mark Attendance",
                "Attendance Management"
        ));

        panel.add(createActionButton(
                "▧ Generate Report",
                "Report Generation"
        ));

        return panel;
    }

    private JButton createActionButton(String text, String module) {

        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(BLUE);
        button.setBackground(WHITE);

        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                new EmptyBorder(15, 8, 15, 8)
        ));

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {

            if (module.equals("Event Management")) {

                new EventManagementFrame().setVisible(true);

            } else if (module.equals("Participant Management")) {

                new ParticipantManagementFrame().setVisible(true);

            } else if (module.equals("Registration Management")) {

                new RegistrationManagementFrame().setVisible(true);

            } else if (module.equals("Attendance Management")
                    || module.equals("Report Generation")) {

                new AttendanceManagementFrame().setVisible(true);

            } else {

                showModuleMessage(module);
            }
        });

        return button;
    }

    // ================= PLACEHOLDER MODULE =================

    private void showModuleMessage(String module) {

        JOptionPane.showMessageDialog(
                this,
                module + " module will be implemented next.",
                "Module Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
