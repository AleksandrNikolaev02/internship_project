package org.example.parser;

import org.example.model.ParsedCommand;

public interface Parser {
    ParsedCommand parse(String input);
}
