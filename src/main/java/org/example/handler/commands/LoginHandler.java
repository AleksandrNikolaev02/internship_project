package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import lombok.AllArgsConstructor;
import org.example.exceptions.RepeatUsernameException;
import org.example.model.ParsedCommand;
import org.example.repository.DataRepository;

import java.util.Map;

@AllArgsConstructor
public class LoginHandler implements Handler {
    private DataRepository dataRepository;

    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        String username = command.getParam("u");
        if (username == null) {
            return "Username is required!";
        }

        checkRepeatUsername(username);
        dataRepository.getSessions().put(ctx.channel().id(), username);
        return "User " + username + " logged in.";
    }

    private void checkRepeatUsername(String username) {
        var sessions = dataRepository.getSessions();

        for (Map.Entry<ChannelId, String> entry : sessions.entrySet()) {
            if (entry.getValue().equals(username)) {
                throw new RepeatUsernameException("This username is already taken!");
            }
        }
    }
}
