
package com.eventportal;

import com.eventportal.dao.RegistrationDAO;

public class TestCancellation {

    public static void main(String[] args) {

        RegistrationDAO registrationDAO = new RegistrationDAO();

        int registrationId = 1;

        System.out.println("CANCELLING REGISTRATION...");

        boolean cancelled =
                registrationDAO.cancelRegistration(registrationId);

        if (cancelled) {
            System.out.println("Cancellation successful!");
        } else {
            System.out.println("Cancellation failed!");
        }
    }
}
