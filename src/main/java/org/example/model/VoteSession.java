package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.enums.VoteCreationStep;

@Getter
@Setter
public class VoteSession {
    private Vote vote;
    private VoteCreationStep step;

    public VoteSession(String topicName) {
        this.vote = new Vote();
        this.step = VoteCreationStep.WAITING_FOR_NAME;
        this.vote.setTopicName(topicName);
    }

    public void nextStep() {
        switch (step) {
            case WAITING_FOR_NAME -> step = VoteCreationStep.WAITING_FOR_DESCRIPTION;
            case WAITING_FOR_DESCRIPTION -> step = VoteCreationStep.WAITING_FOR_ANSWERS_COUNT;
            case WAITING_FOR_ANSWERS_COUNT -> step = VoteCreationStep.WAITING_FOR_ANSWERS;
            case WAITING_FOR_ANSWERS -> {
                if (vote.getAnswers().size() >= vote.getAnswersCount()) {
                    step = VoteCreationStep.COMPLETED;
                }
            }
        }
    }
}
