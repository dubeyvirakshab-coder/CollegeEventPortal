
package com.eventportal;

import com.eventportal.dao.AttendanceDAO;

public class TestAttendance {

    public static void main(String[] args) {

        AttendanceDAO attendanceDAO = new AttendanceDAO();

        int eventId = 1;
        int registrationId = 1;

        // Mark attendance
        System.out.println("MARKING ATTENDANCE...");

        boolean marked =
                attendanceDAO.markAttendance(registrationId);

        if (marked) {
            System.out.println("Attendance test successful!");
        } else {
            System.out.println("Attendance marking failed!");
        }

        // Generate event report
        System.out.println("\nGENERATING EVENT REPORT...");

        attendanceDAO.generateEventReport(eventId);
    }
}
