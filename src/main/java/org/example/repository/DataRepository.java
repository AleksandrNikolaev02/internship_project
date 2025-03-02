package org.example.repository;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import lombok.Getter;
import lombok.Setter;
import org.example.model.Topic;
import org.example.model.TopicList;
import org.example.model.Vote;
import org.example.model.VoteSession;
import org.example.model.VoteUserSession;
import org.example.util.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class DataRepository {
    private final FileStorage<TopicList> topicsList = new FileStorage<>(TopicList.class);
    private final Map<ChannelId, String> sessions = new ConcurrentHashMap<>();
    private final Map<ChannelId, VoteSession> voteSessionMap = new ConcurrentHashMap<>();
    private final Map<ChannelId, VoteUserSession> voteUserSessionMap = new ConcurrentHashMap<>();
    private List<Topic> topics = new ArrayList<>();

    public String getUsernameFromSession(ChannelHandlerContext ctx) {
        return sessions.get(ctx.channel().id());
    }

    public void saveVoteFromVoteSession(VoteSession session) {
        String topicName = session.getVote().getTopicName();

        for (Topic topic : topics) {
            if (topic.getName().equals(topicName)) {
                topic.getVotes().add(session.getVote());
            }
        }
    }

    public Optional<Vote> findVoteByNameAndTopic(String topicName, String voteName) {
        for (Topic topic : topics) {
            if (topic.getName().equals(topicName)) {
                for (Vote vote : topic.getVotes()) {
                    if (vote.getName().equals(voteName)) {
                        return Optional.of(vote);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
