package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.example.enums.EventCommand;
import org.example.exceptions.UnauthorizedException;
import org.example.handler.commands.CreateTopicHandler;
import org.example.handler.commands.CreateVoteHandler;
import org.example.handler.commands.DeleteVoteHandler;
import org.example.handler.commands.Handler;
import org.example.handler.commands.LoginHandler;
import org.example.handler.commands.ViewHandler;
import org.example.handler.commands.VoteHandler;
import org.example.model.ParsedCommand;
import org.example.repository.DataRepository;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
    private final Map<EventCommand, Handler> handlers = new HashMap<>();
    private final DataRepository dataRepository;

    public CommandHandler(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        handlers.put(EventCommand.LOGIN, new LoginHandler(dataRepository));
        handlers.put(EventCommand.CREATE_TOPIC, new CreateTopicHandler(dataRepository));
        handlers.put(EventCommand.VIEW, new ViewHandler(dataRepository));
        handlers.put(EventCommand.CREATE_VOTE, new CreateVoteHandler(dataRepository));
        handlers.put(EventCommand.VOTE, new VoteHandler(dataRepository));
        handlers.put(EventCommand.DELETE, new DeleteVoteHandler(dataRepository));
    }

    public String handleCommand(ParsedCommand parsedCommand, ChannelHandlerContext ctx) {
        Handler handler = handlers.get(parsedCommand.command());
        if (parsedCommand.command() == EventCommand.LOGIN) {
            return handler.execute(parsedCommand, ctx);
        }

        validateUserLogIn(ctx);

        if (handler == null) {
            return "Bad command!";
        }

        return handler.execute(parsedCommand, ctx);
    }

    public boolean isVoteSessionOpen(ChannelHandlerContext ctx) {
        return dataRepository.getVoteSessionMap().containsKey(ctx.channel().id());
    }

    public boolean isVoteUserSessionOpen(ChannelHandlerContext ctx) {
        return dataRepository.getVoteUserSessionMap().containsKey(ctx.channel().id());
    }

    public String deleteInactiveUser(ChannelHandlerContext ctx) {
        ChannelId id = ctx.channel().id();
        dataRepository.getVoteUserSessionMap().remove(id);
        dataRepository.getVoteSessionMap().remove(id);
        return dataRepository.getSessions().remove(id);
    }

    private void validateUserLogIn(ChannelHandlerContext ctx) {
        if (!dataRepository.getSessions().containsKey(ctx.channel().id())) {
            throw new UnauthorizedException("You are not authorized!");
        }
    }
}
