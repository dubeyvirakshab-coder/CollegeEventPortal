package com.eventportal.exception;

/**
 * Thrown by StudentService when something goes wrong in a way the student
 * should be told about (invalid input, a business rule, database down).
 *
 * The message is always written for the student to read, so the UI can
 * show it directly in a dialog.
 */
public class StudentServiceException extends Exception {

    public StudentServiceException(String message) {
        super(message);
    }

    public StudentServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
