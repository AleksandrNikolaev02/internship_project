package org.example.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class ClientHandler extends ChannelInboundHandlerAdapter {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Connected to server. Type your message:");
        sendMessage(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf in = (ByteBuf) msg;
        try {
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            String response = new String(bytes, StandardCharsets.UTF_8);

            System.out.println("Received from server: " + response);

            sendMessage(ctx);
        } finally {
            in.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendMessage(ChannelHandlerContext ctx) throws IOException {
        while (true) {
            String request = reader.readLine();

            if (request == null) {
                System.out.println("Input stream closed.");
                return;
            }

            if (request.isBlank()) {
                System.out.println("Request cannot be empty! Try again:");
                continue;
            }

            if (request.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                System.exit(0);
            }

            ByteBuf buffer = Unpooled.copiedBuffer(request, StandardCharsets.UTF_8);
            ctx.writeAndFlush(buffer);
            break;
        }
    }
}
