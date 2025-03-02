package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import lombok.AllArgsConstructor;
import org.example.model.ParsedCommand;
import org.example.model.TopicList;
import org.example.model.VoteSession;
import org.example.repository.DataRepository;
import org.example.validotor.TopicExistValidator;
import org.example.validotor.VoteNameValidator;

import java.io.IOException;

@AllArgsConstructor
public class CreateVoteHandler implements Handler {
    private DataRepository dataRepository;
    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        ChannelId id = ctx.channel().id();
        if (!command.params().containsKey("t") && !dataRepository.getVoteSessionMap().containsKey(id)) {
            return "Topic name is required!";
        }

        String tokenName = command.getParam("t");
        if (!dataRepository.getVoteSessionMap().containsKey(id)) {
            TopicExistValidator.checkTopicExist(command.getParam("t"), dataRepository.getTopics());
            dataRepository.getVoteSessionMap().put(id, new VoteSession(tokenName));
            return "Enter vote unique name:\n";
        } else {
            VoteSession session = dataRepository.getVoteSessionMap().get(id);
            switch (session.getStep()) {
                case WAITING_FOR_NAME -> {
                    String nameVote = command.getParam("m");
                    if (VoteNameValidator.checkVoteNameExists(session.getVote().getTopicName(),
                            nameVote, dataRepository.getTopics())) {
                        return "Vote with this name already exists. Try another name:\n";
                    }
                    session.getVote().setName(nameVote);
                    session.nextStep();
                    return "Enter vote description:\n";
                }
                case WAITING_FOR_DESCRIPTION -> {
                    session.getVote().setDescription(command.getParam("m"));
                    session.nextStep();
                    return "Enter number of answer options:\n";
                }
                case WAITING_FOR_ANSWERS_COUNT -> {
                    try {
                        int count = Integer.parseInt(command.getParam("m"));
                        if (count <= 0) {
                            return "Number must be positive. Try again:\n";
                        }
                        session.getVote().setAnswersCount(count);
                        session.nextStep();
                        return "Enter option 1:\n";
                    } catch (NumberFormatException e) {
                        return "Invalid number. Try again:\n";
                    }
                }
                case WAITING_FOR_ANSWERS -> {
                    session.getVote().getAnswers().put(command.getParam("m"), 0);
                    if (session.getVote().getAnswers().size() < session.getVote().getAnswersCount()) {
                        return "Enter option " + (session.getVote().getAnswers().size() + 1) + ":\n";
                    } else {
                        session.nextStep();
                        String author = dataRepository.getUsernameFromSession(ctx);
                        session.getVote().setAuthor(author);
                        dataRepository.saveVoteFromVoteSession(session);
                        dataRepository.getVoteSessionMap().remove(id);
                        saveTopic();
                        return "Vote created successfully!\n";
                    }
                }
            }
        }

        return "Please try again!";
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
