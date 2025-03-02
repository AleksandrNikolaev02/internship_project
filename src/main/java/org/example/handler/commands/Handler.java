package org.example.handler.commands;

import io.netty.channel.ChannelHandlerContext;
import org.example.model.ParsedCommand;

public interface Handler {
    String execute(ParsedCommand command, ChannelHandlerContext ctx);
}
