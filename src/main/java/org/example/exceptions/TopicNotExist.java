package org.example.exceptions;

public class TopicNotExist extends RuntimeException {
    public TopicNotExist(String message) {
        super(message);
    }
}
