package org.example.exceptions;

public class RepeatUsernameException extends RuntimeException {
    public RepeatUsernameException(String message) {
        super(message);
    }
}
