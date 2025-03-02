package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.example.exceptions.NotFoundException;
import org.example.model.ParsedCommand;
import org.example.model.Topic;
import org.example.model.Vote;
import org.example.repository.DataRepository;
import org.example.validotor.TopicExistValidator;
import org.example.validotor.VoteNameValidator;

@AllArgsConstructor
public class DeleteVoteHandler implements Handler {
    private DataRepository dataRepository;
    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        if (command.params().containsKey("t") && command.params().containsKey("v")) {
            String topicName = command.getParam("t");
            String username = dataRepository.getUsernameFromSession(ctx);
            String voteName = command.getParam("v");
            TopicExistValidator.checkTopicExist(topicName, dataRepository.getTopics());
            if (!VoteNameValidator.checkVoteNameExists(topicName, voteName, dataRepository.getTopics())) {
                return String.format("Vote with name '%s' not exists!", voteName);
            }

            Vote vote = dataRepository.findVoteByNameAndTopic(topicName, voteName)
                    .orElseThrow(() -> new NotFoundException(String.format("Vote with name %s and topic name %s not found!",
                            voteName, topicName)));

            for (Topic topic : dataRepository.getTopics()) {
                if (topic.getName().equals(topicName)) {
                    if (vote.getAuthor().equals(username)) {
                        topic.getVotes().remove(vote);
                        return "Topic has been deleted!";
                    }
                }
            }
            return "You are not creator of vote!";
        }
        return "Please try again!";
    }
}
