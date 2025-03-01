package org.example.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Topic {
    private String name;
    private List<Vote> votes;

    public Topic(String name) {
        this.name = name;
        votes = new ArrayList<>();
    }
}
