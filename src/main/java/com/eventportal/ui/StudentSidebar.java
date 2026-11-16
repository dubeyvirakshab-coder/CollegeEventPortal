package com.eventportal.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.eventportal.model.Participant;
import com.eventportal.service.StudentService;

/**
 * The navy sidebar used by every Student Portal screen, and the code that
 * moves between screens.
 *
 * Each screen is its own JFrame (like the Admin Portal). When the student
 * clicks a menu item, the next screen is opened in the same position and
 * size, then the old one is closed, so it feels like one window.
 */
public class StudentSidebar extends JPanel {

    public static final String DASHBOARD = "Dashboard";
    public static final String BROWSE = "Browse Events";
    public static final String REGISTRATIONS = "My Registrations";
    public static final String PROFILE = "My Profile";
    public static final String LOGOUT = "Logout";

    private final JFrame owner;
    private final Participant student;
    private final StudentService service;

    public StudentSidebar(JFrame owner, Participant student,
                          StudentService service, String activeItem) {

        this.owner = owner;
        this.student = student;
        this.service = service;

        setPreferredSize(new Dimension(235, 800));
        setBackground(StudentTheme.NAVY);
        setLayout(new BorderLayout());

        add(createLogoPanel(), BorderLayout.NORTH);
        add(createMenu(activeItem), BorderLayout.CENTER);
    }

    private JPanel createLogoPanel() {

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(StudentTheme.NAVY);
        logoPanel.setBorder(new EmptyBorder(30, 15, 30, 15));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("EVENT PORTAL");
        logo.setForeground(StudentTheme.WHITE);
        logo.setFont(StudentTheme.font(Font.BOLD, 22));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("STUDENT PORTAL");
        subtitle.setForeground(StudentTheme.SIDEBAR_SUBTITLE);
        subtitle.setFont(StudentTheme.font(Font.PLAIN, 10));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        logoPanel.add(logo);
        logoPanel.add(Box.createVerticalStrut(8));
        logoPanel.add(subtitle);

        return logoPanel;
    }

    private JPanel createMenu(String activeItem) {

        JPanel menu = new JPanel();
        menu.setBackground(StudentTheme.NAVY);
        menu.setBorder(new EmptyBorder(15, 12, 15, 12));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        menu.add(createNavButton(DASHBOARD, activeItem));
        menu.add(createNavButton(BROWSE, activeItem));
        menu.add(createNavButton(REGISTRATIONS, activeItem));
        menu.add(createNavButton(PROFILE, activeItem));

        menu.add(Box.createVerticalStrut(25));

        menu.add(createNavButton(LOGOUT, activeItem));

        return menu;
    }

    private JButton createNavButton(String text, String activeItem) {

        boolean active = text.equals(activeItem);

        JButton button = new JButton(text);

        button.setMaximumSize(new Dimension(210, 45));
        button.setPreferredSize(new Dimension(210, 45));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 18, 0, 0));

        button.setFont(StudentTheme.font(Font.BOLD, 13));
        button.setForeground(StudentTheme.WHITE);
        button.setBackground(active ? StudentTheme.BLUE : StudentTheme.NAVY);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {

            if (active) {
                return;
            }

            navigate(text);
        });

        return button;
    }

    private void navigate(String target) {

        if (LOGOUT.equals(target)) {

            int choice = JOptionPane.showConfirmDialog(
                    owner,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                owner.dispose();
                new StudentLoginFrame(service);
            }

            return;
        }

        open(target, owner, student, service);
    }

    /**
     * Opens a screen by name, placing it exactly where the current one is,
     * then closes the current one. Also used by the dashboard's
     * Quick Action buttons.
     */
    public static void open(String target, JFrame current,
                            Participant student, StudentService service) {

        JFrame next;

        switch (target) {
            case BROWSE:
                next = new BrowseEventsFrame(student, service);
                break;
            case REGISTRATIONS:
                next = new MyRegistrationsFrame(student, service);
                break;
            case PROFILE:
                next = new StudentProfileFrame(student, service);
                break;
            default:
                next = new StudentDashboardFrame(student, service);
                break;
        }

        next.setBounds(current.getBounds());
        next.setExtendedState(current.getExtendedState());
        next.setVisible(true);

        current.dispose();
    }
}
