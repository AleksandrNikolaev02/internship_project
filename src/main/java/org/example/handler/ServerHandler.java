package org.example.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.EventCommand;
import org.example.model.ParsedCommand;
import org.example.parser.CommandParser;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final CommandParser commandParser = new CommandParser();
    private final CommandHandler commandHandler = new CommandHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            String request = new String(bytes, StandardCharsets.UTF_8);

            log.info("Received: {}", request);

            ParsedCommand command;
            if (commandHandler.isVoteSessionOpen(ctx)) {
                command = new ParsedCommand(EventCommand.CREATE_VOTE,
                        Map.of("m", request));
            } else if (commandHandler.isVoteUserSessionOpen(ctx)) {
                command = new ParsedCommand(EventCommand.VOTE,
                        Map.of("m", request));
            } else{
                command = requestHandler(request);
            }

            String response = commandHandler.handleCommand(command, ctx);
            sendMessageToClient(ctx, response);
        } finally {
            in.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Исключение: {}", cause.getMessage());
        sendMessageToClient(ctx, cause.getMessage());
    }

    private ParsedCommand requestHandler(String request) {
        return commandParser.parse(request);
    }

    private void sendMessageToClient(ChannelHandlerContext ctx, String message) {
        ByteBuf out = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);

        ctx.writeAndFlush(out);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String username = commandHandler.deleteInactiveUser(ctx);

        if (username != null) {
            log.info("User {} disconnected!", username);
        }

        super.channelInactive(ctx);
    }
}
