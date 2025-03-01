package org.example.exceptions;

public class TopicIsAlreadyExists extends RuntimeException {
    public TopicIsAlreadyExists(String message) {
        super(message);
    }
}
