package org.example.validotor;

import org.example.exceptions.TopicNotExist;
import org.example.model.Topic;

import java.util.List;

public class TopicExistValidator {
    public static void checkTopicExist(String topicName, List<Topic> topics) {
        for (Topic topic : topics) {
            if (topic.getName().equals(topicName)) {
                return;
            }
        }

        throw new TopicNotExist("Topic with name '" + topicName + "' not exist!");
    }
}
