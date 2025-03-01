package org.example.model;

import org.example.enums.EventCommand;

import java.util.Map;

public record ParsedCommand(EventCommand command, Map<String, String> params) {

    public String getParam(String key) {
        return params.get(key);
    }

    @Override
    public String toString() {
        return "ParsedCommand{" +
                "command=" + command +
                ", params=" + params +
                '}';
    }
}

