package com.eventportal;

import javax.swing.SwingUtilities;

import com.eventportal.ui.LoginFrame;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new LoginFrame();

        });
    }
}