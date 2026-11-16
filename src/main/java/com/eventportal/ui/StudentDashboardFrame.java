
package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.eventportal.exception.StudentServiceException;
import com.eventportal.model.Participant;
import com.eventportal.service.StudentService;
import com.eventportal.service.StudentService.DashboardStats;

public class StudentDashboardFrame extends JFrame {

    private final Participant student;
    private final StudentService service;

    private DashboardStats stats = new DashboardStats();
    private String loadError = null;

    private final Color NAVY = new Color(20, 32, 55);
    private final Color BLUE = new Color(48, 110, 220);
    private final Color BACKGROUND = new Color(242, 245, 250);
    private final Color TEXT = new Color(45, 55, 72);
    private final Color MUTED = new Color(110, 120, 135);
    private final Color WHITE = Color.WHITE;

    public StudentDashboardFrame(Participant student) {
        this(student, new StudentService());
    }

    public StudentDashboardFrame(
            Participant student,
            StudentService service) {

        this.student = student;
        this.service = service;

        setTitle("College Event Portal | Student Dashboard");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1050, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        loadStats();

        add(
                new StudentSidebar(
                        this,
                        student,
                        service,
                        StudentSidebar.DASHBOARD
                ),
                BorderLayout.WEST
        );

        add(createMainContent(), BorderLayout.CENTER);

        setVisible(true);
    }

    // LOAD DASHBOARD DATA

    private void loadStats() {

        try {

            stats = service.getDashboardStats(student);

        } catch (StudentServiceException e) {

            loadError = e.getMessage();
        }
    }

    // MAIN CONTENT

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(WHITE);
        header.setBorder(
                new EmptyBorder(18, 30, 18, 30)
        );

        JLabel title = new JLabel("Student Dashboard");

        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT);

        JLabel portal = new JLabel("COLLEGE EVENT PORTAL");

        portal.setFont(new Font("SansSerif", Font.BOLD, 12));
        portal.setForeground(BLUE);

        header.add(title, BorderLayout.WEST);
        header.add(portal, BorderLayout.EAST);

        JScrollPane scrollPane =
                new JScrollPane(createDashboard());

        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(BACKGROUND);

        main.add(header, BorderLayout.NORTH);
        main.add(scrollPane, BorderLayout.CENTER);

        return main;
    }

    // DASHBOARD BODY

    private JPanel createDashboard() {

        JPanel dashboard = new JPanel();

        dashboard.setLayout(
                new BoxLayout(dashboard, BoxLayout.Y_AXIS)
        );

        dashboard.setBackground(BACKGROUND);
        dashboard.setBorder(
                new EmptyBorder(25, 30, 35, 30)
        );

        dashboard.add(createWelcomeBanner());
        dashboard.add(Box.createVerticalStrut(25));

        dashboard.add(createSectionTitle("Your Activity"));
        dashboard.add(Box.createVerticalStrut(15));

        dashboard.add(createStatistics());
        dashboard.add(Box.createVerticalStrut(30));

        dashboard.add(createSectionTitle("Your Events"));
        dashboard.add(Box.createVerticalStrut(15));

        dashboard.add(createInformationPanels());
        dashboard.add(Box.createVerticalStrut(30));

        dashboard.add(createSectionTitle("Quick Actions"));
        dashboard.add(Box.createVerticalStrut(15));

        dashboard.add(createQuickActions());

        if (loadError != null) {

            dashboard.add(Box.createVerticalStrut(15));

            JLabel error = new JLabel(
                    "Unable to load dashboard data: " + loadError
            );

            error.setForeground(new Color(190, 45, 45));
            error.setFont(new Font("SansSerif", Font.BOLD, 12));

            dashboard.add(error);
        }

        return dashboard;
    }

    // WELCOME BANNER

    private JPanel createWelcomeBanner() {

        JPanel banner = new JPanel(new BorderLayout());

        banner.setBackground(NAVY);
        banner.setBorder(
                new EmptyBorder(28, 30, 28, 30)
        );

        JLabel welcome = new JLabel(
                "Welcome back, " + student.getFullName() + "!"
        );

        welcome.setForeground(WHITE);
        welcome.setFont(
                new Font("SansSerif", Font.BOLD, 25)
        );

        JLabel subtitle = new JLabel(
                "Discover events, participate and manage your activities."
        );

        subtitle.setForeground(
                new Color(205, 215, 230)
        );

        subtitle.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        JPanel left = new JPanel();

        left.setBackground(NAVY);
        left.setLayout(
                new BoxLayout(left, BoxLayout.Y_AXIS)
        );

        left.add(welcome);
        left.add(Box.createVerticalStrut(10));
        left.add(subtitle);

        JLabel date = new JLabel(
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern(
                                "EEEE, dd MMMM yyyy"
                        )
                )
        );

        date.setForeground(
                new Color(180, 200, 230)
        );

        date.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        banner.add(left, BorderLayout.CENTER);
        banner.add(date, BorderLayout.EAST);

        banner.setAlignmentX(LEFT_ALIGNMENT);

        return banner;
    }

    // SECTION TITLE

    private JLabel createSectionTitle(String text) {

        JLabel title = new JLabel(text);

        title.setFont(
                new Font("SansSerif", Font.BOLD, 19)
        );

        title.setForeground(TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);

        return title;
    }

    // STATISTICS CARDS

    private JPanel createStatistics() {

        JPanel panel = new JPanel(
                new GridLayout(1, 4, 16, 16)
        );

        panel.setBackground(BACKGROUND);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(
                createStatCard(
                        "MY REGISTRATIONS",
                        String.valueOf(stats.activeRegistrations),
                        "Currently active",
                        BLUE
                )
        );

        panel.add(
                createStatCard(
                        "UPCOMING EVENTS",
                        String.valueOf(stats.upcomingRegistrations),
                        "Ready to attend",
                        new Color(30, 160, 120)
                )
        );

        panel.add(
                createStatCard(
                        "EVENTS ATTENDED",
                        String.valueOf(stats.attendedEvents),
                        "Your participation",
                        new Color(145, 90, 200)
                )
        );

        panel.add(
                createStatCard(
                        "CANCELLED",
                        String.valueOf(stats.cancelledRegistrations),
                        "Cancelled bookings",
                        new Color(230, 145, 40)
                )
        );

        return panel;
    }

    private JPanel createStatCard(
            String heading,
            String value,
            String description,
            Color accent) {

        JPanel card = new JPanel();

        card.setBackground(WHITE);

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        new EmptyBorder(20, 18, 20, 18)
                )
        );

        JLabel title = new JLabel(heading);

        title.setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );

        title.setForeground(MUTED);

        JLabel number = new JLabel(value);

        number.setFont(
                new Font("SansSerif", Font.BOLD, 34)
        );

        number.setForeground(accent);

        JLabel info = new JLabel(description);

        info.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        info.setForeground(MUTED);

        card.add(title);
        card.add(Box.createVerticalStrut(13));
        card.add(number);
        card.add(Box.createVerticalStrut(8));
        card.add(info);

        return card;
    }

    // UPCOMING AND RECENT EVENTS

    private JPanel createInformationPanels() {

        JPanel panel = new JPanel(
                new GridLayout(1, 2, 18, 18)
        );

        panel.setBackground(BACKGROUND);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel upcoming = createInfoCard(
                "My Upcoming Events",
                stats.upcomingLines,
                "You have no upcoming registered events."
        );

        JPanel recent = createInfoCard(
                "Recent Registrations",
                stats.recentLines,
                "You have not registered for any events yet."
        );

        panel.add(upcoming);
        panel.add(recent);

        return panel;
    }

    private JPanel createInfoCard(
            String heading,
            java.util.List<String> lines,
            String emptyMessage) {

        JPanel card = new JPanel();

        card.setBackground(WHITE);

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        new EmptyBorder(20, 22, 20, 22)
                )
        );

        JLabel title = new JLabel(heading);

        title.setFont(
                new Font("SansSerif", Font.BOLD, 16)
        );

        title.setForeground(TEXT);

        card.add(title);
        card.add(Box.createVerticalStrut(18));

        if (lines == null || lines.isEmpty()) {

            JLabel empty = new JLabel(emptyMessage);

            empty.setFont(
                    new Font("SansSerif", Font.PLAIN, 13)
            );

            empty.setForeground(MUTED);

            card.add(empty);

        } else {

            for (String line : lines) {

                JLabel item = new JLabel("•  " + line);

                item.setFont(
                        new Font("SansSerif", Font.PLAIN, 13)
                );

                item.setForeground(MUTED);

                item.setBorder(
                        new EmptyBorder(7, 0, 7, 0)
                );

                card.add(item);
            }
        }

        return card;
    }

    // QUICK ACTIONS

    private JPanel createQuickActions() {

        JPanel panel = new JPanel(
                new GridLayout(1, 3, 15, 15)
        );

        panel.setBackground(BACKGROUND);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(
                createAction(
                        "Browse Events",
                        "Explore available college events",
                        StudentSidebar.BROWSE
                )
        );

        panel.add(
                createAction(
                        "My Registrations",
                        "Manage your event registrations",
                        StudentSidebar.REGISTRATIONS
                )
        );

        panel.add(
                createAction(
                        "My Profile",
                        "View and update your information",
                        StudentSidebar.PROFILE
                )
        );

        return panel;
    }

    private JPanel createAction(
            String title,
            String description,
            String target) {

        JPanel card = new JPanel();

        card.setBackground(WHITE);

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        new EmptyBorder(18, 18, 18, 18)
                )
        );

        JLabel heading = new JLabel(title);

        heading.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );

        heading.setForeground(TEXT);

        JLabel detail = new JLabel(description);

        detail.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        detail.setForeground(MUTED);

        JButton button = new JButton("Open →");

        button.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        button.setForeground(BLUE);
        button.setBackground(WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setAlignmentX(LEFT_ALIGNMENT);

        button.addActionListener(e ->
                StudentSidebar.open(
                        target,
                        this,
                        student,
                        service
                )
        );

        card.add(heading);
        card.add(Box.createVerticalStrut(8));
        card.add(detail);
        card.add(Box.createVerticalStrut(15));
        card.add(button);

        return card;
    }
}
