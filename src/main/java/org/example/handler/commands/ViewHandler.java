package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.example.model.ParsedCommand;
import org.example.model.Topic;
import org.example.model.Vote;
import org.example.repository.DataRepository;

import java.util.Map;

@AllArgsConstructor
public class ViewHandler implements Handler {
    private DataRepository dataRepository;
    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        if (command.params().isEmpty()) {
            return collectAllTopicInResponse();
        }

        if (command.params().containsKey("t") && command.params().containsKey("v")) {
            return findTopicByNameAndNameVote(command.getParam("t"), command.getParam("v"));
        } else if (command.params().containsKey("t")) {
            return findTopicByName(command.getParam("t"));
        }

        return "Not found topics or incorrect params!";
    }

    private String collectAllTopicInResponse() {
        StringBuilder response = new StringBuilder("Topics:\n");

        for (Topic topic : dataRepository.getTopics()) {
            response.append(String.format("<%s (votes in topic=%d)>\n",
                    topic.getName(), topic.getVotes().size()));
        }

        return response.toString();
    }

    private String findTopicByNameAndNameVote(String topicName, String voteName) {
        StringBuilder response = new StringBuilder("INFO:\n");
        for (Topic topic : dataRepository.getTopics()) {
            if (topic.getName().equals(topicName)) {
                for (Vote vote : topic.getVotes()) {
                    if (vote.getName().equals(voteName)) {
                        response.append("Name topic: ").append(topicName).append("\n");

                        response.append("Variants:\n");
                        for (Map.Entry<String, Integer> entry : vote.getAnswers().entrySet()) {
                            response.append(entry.getKey()).append(" : ").append(entry.getValue()).append(" ");
                        }
                    }
                }
            }
        }

        return response.toString();
    }

    private String findTopicByName(String name) {
        for (Topic topic : dataRepository.getTopics()) {
            if (topic.getName().equals(name)) {
                return String.format("<%s (votes in topic=%d)>", name, topic.getVotes().size());
            }
        }

        return "Topic not found!";
    }
}
