package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.example.exceptions.TopicIsAlreadyExists;
import org.example.exceptions.TopicNotExist;
import org.example.exceptions.UnauthorizedException;
import org.example.model.ParsedCommand;
import org.example.model.Topic;
import org.example.model.TopicList;
import org.example.model.Vote;
import org.example.model.VoteSession;
import org.example.model.VoteUserSession;
import org.example.util.FileStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {
    private final FileStorage<TopicList> topicsList = new FileStorage<>(TopicList.class);
    private final Map<ChannelId, String> sessions = new ConcurrentHashMap<>();
    private final Map<ChannelId, VoteSession> voteSessionMap = new ConcurrentHashMap<>();
    private final Map<ChannelId, VoteUserSession> voteUserSessionMap = new ConcurrentHashMap<>();
    public static List<Topic> topics = new ArrayList<>();

    public String handleCommand(ParsedCommand command, ChannelHandlerContext ctx) {
        switch (command.command()) {
            case LOGIN:
                return handleLogin(command, ctx);
            case CREATE_TOPIC:
                validateUserLogIn(ctx);
                return handleCreateTopic(command);
            case VIEW:
                validateUserLogIn(ctx);
                return handleView(command);
            case CREATE_VOTE:
                validateUserLogIn(ctx);
                return handleCreateVote(command, ctx);
            case VOTE:
                validateUserLogIn(ctx);
                return handleVote(command, ctx);
            case DELETE:
                validateUserLogIn(ctx);
                return handleDeleteVote(command, ctx);
            default:
                return "Bad command!";
        }
    }

    public boolean isVoteSessionOpen(ChannelHandlerContext ctx) {
        return voteSessionMap.containsKey(ctx.channel().id());
    }

    public boolean isVoteUserSessionOpen(ChannelHandlerContext ctx) {
        return voteUserSessionMap.containsKey(ctx.channel().id());
    }

    public String deleteInactiveUser(ChannelHandlerContext ctx) {
        ChannelId id = ctx.channel().id();
        voteUserSessionMap.remove(id);
        voteSessionMap.remove(id);
        return sessions.remove(id);
    }

    private String handleLogin(ParsedCommand command, ChannelHandlerContext ctx) {
        String username = command.getParam("u");
        if (username == null) {
            return "Username is required!";
        }

        sessions.put(ctx.channel().id(), username);
        return "User " + username + " logged in.";
    }

    private String handleCreateTopic(ParsedCommand command) {
        if (!command.params().containsKey("n")) {
            return "Topic name is required!";
        }

        String topicName = command.getParam("n");
        validateRepeatTopicName(topicName);

        topics.add(new Topic(topicName));
        saveTopic();

        return "Topic '" + topicName + "' created.";
    }

    private String handleView(ParsedCommand command) {
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

    private String handleCreateVote(ParsedCommand command, ChannelHandlerContext ctx) {
        ChannelId id = ctx.channel().id();
        if (!command.params().containsKey("t") && !voteSessionMap.containsKey(id)) {
            return "Topic name is required!";
        }

        String tokenName = command.getParam("t");
        if (!voteSessionMap.containsKey(id)) {
            checkTopicExist(command.getParam("t"));
            voteSessionMap.put(id, new VoteSession(tokenName));
            return "Enter vote unique name:\n";
        } else {
            VoteSession session = voteSessionMap.get(id);
            switch (session.getStep()) {
                case WAITING_FOR_NAME -> {
                    String nameVote = command.getParam("m");
                    if (checkVoteNameExists(session.getVote().getTopicName(), nameVote)) {
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
                        String author = getUsernameFromSession(ctx);
                        session.getVote().setAuthor(author);
                        saveVote(session);
                        voteSessionMap.remove(id);
                        saveTopic();
                        return "Vote created successfully!\n";
                    }
                }
            }
        }

        return "Please try again!";
    }

    private String handleVote(ParsedCommand command, ChannelHandlerContext ctx) {
        if (command.params().containsKey("t") && command.params().containsKey("v")) {
            String topicName = command.getParam("t");
            String voteName = command.getParam("v");
            checkTopicExist(topicName);
            if (!checkVoteNameExists(topicName, voteName)) {
                return String.format("Vote with name '%s' not exists!", voteName);
            }

            Vote vote = findVoteByNameAndTopic(topicName, voteName);
            StringBuilder response = new StringBuilder("");
            int counter = 1;
            for (Map.Entry<String, Integer> entry : vote.getAnswers().entrySet()) {
                response.append(counter).append(") ").append(entry.getKey()).append(";\n");
                ++counter;
            }

            voteUserSessionMap.put(ctx.channel().id(), new VoteUserSession(vote));

            response.append("Enter your number:\n");

            return response.toString();
        } else if (command.params().containsKey("m")) {
            try {
                int voteNumber = Integer.parseInt(command.getParam("m"));
                VoteUserSession session = voteUserSessionMap.get(ctx.channel().id());

                for (Topic topic : topics) {
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

                voteUserSessionMap.remove(ctx.channel().id());

                return "Your vote has been counted!";
            } catch (NumberFormatException exception) {
                return "Invalid number. Try again:\n";
            }
        }

        return "Incorrect command!";
    }

    private String handleDeleteVote(ParsedCommand command, ChannelHandlerContext ctx) {
        if (command.params().containsKey("t") && command.params().containsKey("v")) {
            String topicName = command.getParam("t");
            String username = getUsernameFromSession(ctx);
            String voteName = command.getParam("v");
            checkTopicExist(topicName);
            if (!checkVoteNameExists(topicName, voteName)) {
                return String.format("Vote with name '%s' not exists!", voteName);
            }
            Vote vote = findVoteByNameAndTopic(topicName, voteName);
            if (vote == null) {
                return String.format("Vote with name %s and topic name %s not found!", voteName, topicName);
            }

            for (Topic topic : topics) {
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

    private void validateUserLogIn(ChannelHandlerContext ctx) {
        if (!sessions.containsKey(ctx.channel().id())) {
            throw new UnauthorizedException("You are not authorized!");
        }
    }

    private void validateRepeatTopicName(String topicName) {
        topics.forEach(topic -> {
            if (topic.getName().equals(topicName)) {
                throw new TopicIsAlreadyExists("Топик с таким именем уже существует!");
            }
        });
    }

    private String collectAllTopicInResponse() {
        StringBuilder response = new StringBuilder("Topics:\n");

        for (Topic topic : topics) {
            response.append(String.format("<%s (votes in topic=%d)>\n",
                    topic.getName(), topic.getVotes().size()));
        }

        return response.toString();
    }

    private String findTopicByName(String name) {
        for (Topic topic : topics) {
            if (topic.getName().equals(name)) {
                return String.format("<%s (votes in topic=%d)>", name, topic.getVotes().size());
            }
        }

        return "Topic not found!";
    }

    private boolean checkVoteNameExists(String topicName, String nameVote) {
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

    private void saveVote(VoteSession session) {
        String topicName = session.getVote().getTopicName();

        for (Topic topic : topics) {
            if (topic.getName().equals(topicName)) {
                topic.getVotes().add(session.getVote());
            }
        }
    }

    private void saveTopic() {
        try {
            topicsList.save("src/main/resources/db/topics.json", new TopicList(topics));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String findTopicByNameAndNameVote(String topicName, String voteName) {
        StringBuilder response = new StringBuilder("INFO:\n");
        for (Topic topic : topics) {
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

    private void checkTopicExist(String topicName) {
        for (Topic topic : topics) {
            if (topic.getName().equals(topicName)) {
                return;
            }
        }

        throw new TopicNotExist("Topic with name '" + topicName + "' not exist!");
    }

    private Vote findVoteByNameAndTopic(String topicName, String voteName) {
        for (Topic topic : topics) {
            if (topic.getName().equals(topicName)) {
                for (Vote vote : topic.getVotes()) {
                    if (vote.getName().equals(voteName)) {
                        return vote;
                    }
                }
            }
        }

        return null;
    }

    private String getUsernameFromSession(ChannelHandlerContext ctx) {
        return sessions.get(ctx.channel().id());
    }
}
