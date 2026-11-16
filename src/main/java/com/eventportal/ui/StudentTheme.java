package com.eventportal.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Colours, fonts and small UI building blocks shared by every Student
 * Portal screen.
 *
 * The colours and fonts are copied from the Admin Portal's MainFrame and
 * RegistrationManagementFrame so both portals look like one application.
 * Keeping them in ONE place means a colour change is made once.
 */
public final class StudentTheme {

    private StudentTheme() {
    }

    // ---- Colours (from MainFrame) ----
    public static final Color NAVY = new Color(20, 32, 55);
    public static final Color BLUE = new Color(48, 110, 220);
    public static final Color BACKGROUND = new Color(242, 245, 250);
    public static final Color WHITE = Color.WHITE;
    public static final Color TEXT = new Color(45, 55, 72);
    public static final Color MUTED = new Color(90, 100, 115);

    // ---- Accent colours (stat cards in MainFrame / cancel button) ----
    public static final Color GREEN = new Color(30, 160, 120);
    public static final Color ORANGE = new Color(230, 145, 40);
    public static final Color PURPLE = new Color(145, 90, 200);
    public static final Color RED = new Color(205, 65, 65);

    public static final Color BORDER = new Color(225, 230, 238);
    public static final Color SIDEBAR_SUBTITLE = new Color(170, 185, 210);

    // ---- Fonts ----
    public static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    // =================== BUTTONS ===================

    /** Solid blue button (same look as the Admin Portal's action buttons). */
    public static JButton primaryButton(String text) {
        return solidButton(text, BLUE);
    }

    public static JButton dangerButton(String text) {
        return solidButton(text, RED);
    }

    private static JButton solidButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(font(Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(190, 40));

        return button;
    }

    /** White button with blue text and a light border (Quick Actions style). */
    public static JButton outlineButton(String text) {

        JButton button = new JButton(text);

        button.setFont(font(Font.BOLD, 12));
        button.setForeground(BLUE);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                new EmptyBorder(15, 8, 15, 8)));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // =================== PAGE HEADER ===================

    /** White top bar: page title on the left, "STUDENT PORTAL" on the right. */
    public static JPanel pageHeader(String title) {

        JPanel header = new JPanel(new java.awt.BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 25));
        titleLabel.setForeground(TEXT);

        JLabel tag = new JLabel("STUDENT PORTAL");
        tag.setFont(font(Font.BOLD, 12));
        tag.setForeground(BLUE);

        header.add(titleLabel, java.awt.BorderLayout.WEST);
        header.add(tag, java.awt.BorderLayout.EAST);

        return header;
    }

    // =================== CARDS AND PANELS ===================

    /** Statistic card with a coloured left edge (same as MainFrame). */
    public static JPanel statCard(String title, String value, Color color) {

        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color),
                new EmptyBorder(20, 18, 20, 18)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 11));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(font(Font.BOLD, 30));
        valueLabel.setForeground(TEXT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);

        return card;
    }

    /** White panel with a heading, used for list sections on the dashboard. */
    public static JPanel infoPanel(String title) {

        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(font(Font.BOLD, 17));
        heading.setForeground(TEXT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(18));

        return panel;
    }

    public static JLabel infoLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 13));
        label.setForeground(MUTED);
        label.setBorder(new EmptyBorder(7, 0, 7, 0));

        return label;
    }

    /** White card with a thin border (same as Admin form panels). */
    public static JPanel borderedCard() {

        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)));

        return panel;
    }

    /** Fills a panel with up to N lines, or an "empty" message. */
    public static void addLines(JPanel panel, List<String> lines,
                                String emptyMessage) {

        if (lines.isEmpty()) {
            panel.add(infoLabel(emptyMessage));
            return;
        }

        for (String line : lines) {
            panel.add(infoLabel(line));
        }
    }

    /** A row of equally sized components. */
    public static JPanel gridRow(int columns, int gap) {

        JPanel row = new JPanel(new GridLayout(1, columns, gap, gap));
        row.setBackground(BACKGROUND);

        return row;
    }

    // =================== TABLES ===================

    /** Gives a JTable the same look as the Admin Portal tables. */
    public static void styleTable(JTable table, int rowHeight) {

        table.setRowHeight(rowHeight);
        table.setFont(font(Font.PLAIN, 12));
        table.setSelectionBackground(new Color(214, 228, 250));
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(235, 238, 244));

        table.getTableHeader().setFont(font(Font.BOLD, 12));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(WHITE);
        table.getTableHeader().setReorderingAllowed(false);
    }

    /** Light grey, small text used for hints and field labels. */
    public static JLabel hintLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 12));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.LEFT);

        return label;
    }
}
