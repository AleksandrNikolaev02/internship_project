package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.example.exceptions.TopicIsAlreadyExists;
import org.example.model.ParsedCommand;
import org.example.model.Topic;
import org.example.model.TopicList;
import org.example.repository.DataRepository;

import java.io.IOException;

@AllArgsConstructor
public class CreateTopicHandler implements Handler {
    private final DataRepository dataRepository;
    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        if (!command.params().containsKey("n")) {
            return "Topic name is required!";
        }

        String topicName = command.getParam("n");
        validateRepeatTopicName(topicName);

        dataRepository.getTopics().add(new Topic(topicName));
        saveTopic();

        return "Topic '" + topicName + "' created.";
    }

    private void validateRepeatTopicName(String topicName) {
        dataRepository.getTopics().forEach(topic -> {
            if (topic.getName().equals(topicName)) {
                throw new TopicIsAlreadyExists("Топик с таким именем уже существует!");
            }
        });
    }

    private void saveTopic() {
        try {
            dataRepository.getTopicsList().save("src/main/resources/db/topics.json",
                    new TopicList(dataRepository.getTopics()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
