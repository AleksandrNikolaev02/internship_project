package org.example.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.initializer.ServerInitializer;
import org.example.listener.ConsoleCommandListener;

public class Server {
    private final Integer port;

    public Server(Integer port) {
        this.port = port;
    }

    public static void main(String[] args ) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new Server(port).run();
    }

    public void run() throws Exception {
        Thread consoleListenerThread = new Thread(new ConsoleCommandListener());
        consoleListenerThread.start();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Server started on port " + port);

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
