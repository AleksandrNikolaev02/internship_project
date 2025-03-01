package org.example.parser;

import org.example.enums.EventCommand;
import org.example.model.ParsedCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandParser implements Parser {
    @Override
    public ParsedCommand parse(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length == 0) {
            throw new IllegalArgumentException("Empty command!");
        }

        String command = tokens[0];
        Map<String, String> params = new HashMap<>();

        String subcommand = "";
        int start = 1;
        if (command.equals("create")) {
            subcommand = tokens[1];
            if (!subcommand.equals("topic") && !subcommand.equals("vote")) {
                throw new IllegalArgumentException("Not correct create command!");
            }
            ++start;
        }

        for (int i = start; i < tokens.length; ++i) {
            if (tokens[i].startsWith("-")) {
                String[] keyValue = tokens[i].split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0].substring(1), keyValue[1]);
                } else {
                    throw new IllegalArgumentException("Invalid parameter format: " + tokens[i]);
                }
            }
        }

        if (!subcommand.isEmpty()) {
            command += "_" + subcommand;
        }
        return new ParsedCommand(EventCommand.valueOf(command.toUpperCase()), params);
    }
}
