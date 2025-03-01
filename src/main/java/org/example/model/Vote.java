package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class Vote {
    private String topicName;
    private String name;
    private String description;
    private Map<String, Integer> answers;
    private Integer answersCount;
    private String author;

    public Vote() {
        answers = new LinkedHashMap<>();
    }
}
