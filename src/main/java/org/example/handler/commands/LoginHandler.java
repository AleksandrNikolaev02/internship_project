package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.example.model.ParsedCommand;
import org.example.repository.DataRepository;

@AllArgsConstructor
public class LoginHandler implements Handler {
    private DataRepository dataRepository;

    @Override
    public String execute(ParsedCommand command, ChannelHandlerContext ctx) {
        String username = command.getParam("u");
        if (username == null) {
            return "Username is required!";
        }

        dataRepository.getSessions().put(ctx.channel().id(), username);
        return "User " + username + " logged in.";
    }
}
