package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.example.handler.ServerHandler;
import org.example.repository.DataRepository;

public class AppTest extends TestCase {
    private final String username = "alex";
    private final String testTopicName = "test";
    private final String testVoteName = "test_vote";
    private final EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ServerHandler(new DataRepository()));

    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testWithAuthorized() {
        String result = authorize();

        assertEquals("User " + username + " logged in.", result);
    }

    public void testWithoutAuthorized() {
        sendRequestToServer("view");

        String result = getResponseFromServer();

        assertEquals("You are not authorized!", result);
    }

    public void testEmptyView() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        sendRequestToServer("view");

        String result = getResponseFromServer();

        assertEquals("Topics:\n", result);
    }

    public void testCreateTopic() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        // create topic
        String result = createTopic();

        assertEquals(String.format("Topic '%s' created.", testTopicName), result);

        // view created topic
        sendRequestToServer("view");

        String resultView = getResponseFromServer();

        assertEquals(String.format("Topics:\n<%s (votes in topic=0)>\n", testTopicName), resultView);
    }

    public void testCreateVoteWithNotFoundTopic() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        sendRequestToServer(String.format("create vote -t=%s", testTopicName));

        String response = getResponseFromServer();

        assertEquals(String.format("Topic with name '%s' not exist!", testTopicName), response);
    }

    public void testCreateVoteWithTopic() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        //create topic
        createTopic();

        //start create vote
        createVote();
    }

    public void testViewCreatedVote() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        createTopic();
        createVote();

        sendRequestToServer(String.format("view -t=%s -v=%s", testTopicName, testVoteName));
        String result = getResponseFromServer();

        assertEquals(String.format("""
                INFO:
                Name topic: %s
                Variants:
                Option_1 : 0 Option_2 : 0\s""", testTopicName), result);
    }

    public void testVoteCommand() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        createTopic();
        createVote();

        sendRequestToServer(String.format("vote -t=%s -v=%s", testTopicName, testVoteName));
        String result = getResponseFromServer();
        assertEquals("""
                1) Option_1;
                2) Option_2;
                Enter your number:
                """, result);

        sendRequestToServer("1");
        result = getResponseFromServer();
        assertEquals("Your vote has been counted!", result);

        sendRequestToServer(String.format("view -t=%s -v=%s", testTopicName, testVoteName));
        result = getResponseFromServer();
        assertEquals(String.format("""
                INFO:
                Name topic: %s
                Variants:
                Option_1 : 1 Option_2 : 0\040""", testTopicName), result);
    }

    public void testDeleteVote() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        createTopic();
        createVote();

        sendRequestToServer(String.format("delete -t=%s -v=%s", testTopicName, testVoteName));
        String response = getResponseFromServer();

        assertEquals("Vote has been deleted!", response);
    }

    public void testDeleteVoteWithoutParameter() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        createTopic();
        createVote();

        sendRequestToServer(String.format("delete -t=%s", testTopicName));
        String response = getResponseFromServer();

        assertEquals("Please try again!", response);
    }

    public void testDeleteVoteNotAuthor() {
        String loginResult = authorize();

        assertEquals("User alex logged in.", loginResult);

        createTopic();
        createVote();

        sendRequestToServer("login -u=not_alex");
        getResponseFromServer();

        sendRequestToServer(String.format("delete -t=%s -v=%s", testTopicName, testVoteName));
        String response = getResponseFromServer();

        assertEquals("You are not creator of vote!", response);
    }

    private String createTopic() {
        sendRequestToServer(String.format("create topic -n=%s", testTopicName));
        return getResponseFromServer();
    }

    private void createVote() {
        sendRequestToServer(String.format("create vote -t=%s", testTopicName));
        String response = getResponseFromServer();
        assertEquals("Enter vote unique name:\n", response);

        //step 1
        sendRequestToServer(testVoteName);
        response = getResponseFromServer();
        assertEquals("Enter vote description:\n", response);

        // step 2
        sendRequestToServer("Test description!");
        response = getResponseFromServer();
        assertEquals("Enter number of answer options:\n", response);

        // step 3
        sendRequestToServer("2");
        response = getResponseFromServer();
        assertEquals("Enter option 1:\n", response);

        // step 4
        sendRequestToServer("Option_1");
        response = getResponseFromServer();
        assertEquals("Enter option 2:\n", response);

        // step 5
        sendRequestToServer("Option_2");
        response = getResponseFromServer();
        assertEquals("Vote created successfully!\n", response);
    }

    private void sendRequestToServer(String command) {
        ByteBuf inputView = Unpooled.copiedBuffer(command, CharsetUtil.UTF_8);
        embeddedChannel.writeInbound(inputView);
    }

    private String getResponseFromServer() {
        ByteBuf outputView = embeddedChannel.readOutbound();
        return outputView.toString(CharsetUtil.UTF_8);
    }

    private String authorize() {
        sendRequestToServer(String.format("login -u=%s", username));

        return getResponseFromServer();
    }
}
