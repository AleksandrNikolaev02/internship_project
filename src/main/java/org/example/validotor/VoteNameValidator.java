package org.example.validotor;

import org.example.model.Topic;
import org.example.model.Vote;

import java.util.List;

public class VoteNameValidator {
    public static boolean checkVoteNameExists(String topicName, String nameVote, List<Topic> topics) {
        for (Topic topic : topics) {
            if (topic.getName().equals(topicName)) {
                var votes = topic.getVotes();

                for (Vote vote : votes) {
                    if (vote.getName().equals(nameVote)) return true;
                }
            }
        }

        return false;
    }
}
