package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.example.exceptions.NotFoundException;
import org.example.model.ParsedCommand;
import org.example.model.Topic;
import org.example.model.Vote;
import org.example.repository.DataRepository;

@AllArgsConstructor
public class DeleteVoteHandler implements Handler {
    private DataRepository dataRepository;
    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        if (command.params().containsKey("t") && command.params().containsKey("v")) {
            String topicName = command.getParam("t");
            String username = dataRepository.getUsernameFromSession(ctx);
            String voteName = command.getParam("v");
            Topic topic = dataRepository.findTopicByName(topicName)
                    .orElseThrow(() -> new NotFoundException(String.format("Topic with name %s not found!",
                            topicName)));

            Vote vote = dataRepository.findVoteByNameAndTopic(topicName, voteName)
                    .orElseThrow(() -> new NotFoundException(String.format("Vote with name %s not found!",
                            voteName)));

            if (vote.getAuthor().equals(username)) {
                topic.getVotes().remove(vote);
                return "Vote has been deleted!";
            }

            return "You are not creator of vote!";
        }
        return "Please try again!";
    }
}
