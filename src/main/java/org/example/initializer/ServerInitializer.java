package org.example.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import org.example.handler.ServerHandler;
import org.example.repository.DataRepository;

@AllArgsConstructor
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private DataRepository dataRepository;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new ServerHandler(dataRepository));
    }
}
