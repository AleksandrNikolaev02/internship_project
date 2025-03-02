package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.example.exceptions.NotFoundException;
import org.example.model.ParsedCommand;
import org.example.model.Topic;
import org.example.model.Vote;
import org.example.model.VoteUserSession;
import org.example.repository.DataRepository;
import org.example.validotor.TopicExistValidator;
import org.example.validotor.VoteNameValidator;

import java.util.Map;

@AllArgsConstructor
public class VoteHandler implements Handler {
    private DataRepository dataRepository;
    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        if (command.params().containsKey("t") && command.params().containsKey("v")) {
            String topicName = command.getParam("t");
            String voteName = command.getParam("v");
            TopicExistValidator.checkTopicExist(topicName, dataRepository.getTopics());
            if (!VoteNameValidator.checkVoteNameExists(topicName, voteName, dataRepository.getTopics())) {
                return String.format("Vote with name '%s' not exists!", voteName);
            }

            Vote vote = dataRepository.findVoteByNameAndTopic(topicName, voteName)
                    .orElseThrow(() -> new NotFoundException(String.format("Vote with name %s and topic name %s not found!",
                            voteName, topicName)));

            StringBuilder response = new StringBuilder();
            int counter = 1;
            for (Map.Entry<String, Integer> entry : vote.getAnswers().entrySet()) {
                response.append(counter).append(") ").append(entry.getKey()).append(";\n");
                ++counter;
            }

            dataRepository.getVoteUserSessionMap().put(ctx.channel().id(), new VoteUserSession(vote));

            response.append("Enter your number:\n");

            return response.toString();
        } else if (command.params().containsKey("m")) {
            try {
                int voteNumber = Integer.parseInt(command.getParam("m"));
                VoteUserSession session = dataRepository.getVoteUserSessionMap().get(ctx.channel().id());

                for (Topic topic : dataRepository.getTopics()) {
                    if (topic.getName().equals(session.getVote().getTopicName())) {
                        for (Vote vote : topic.getVotes()) {
                            if (vote.getName().equals(session.getVote().getName())) {
                                int start = 0;
                                for (Map.Entry<String, Integer> entry : vote.getAnswers().entrySet()) {
                                    if (start == voteNumber - 1) {
                                        entry.setValue(entry.getValue() + 1);
                                        break;
                                    }
                                    ++start;
                                }
                            }
                        }
                    }
                }

                dataRepository.getVoteUserSessionMap().remove(ctx.channel().id());

                return "Your vote has been counted!";
            } catch (NumberFormatException exception) {
                return "Invalid number. Try again:\n";
            }
        }

        return "Incorrect command!";
    }
}
