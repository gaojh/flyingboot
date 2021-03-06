package com.github.gaojh.server;

import com.github.gaojh.config.Environment;
import com.github.gaojh.server.handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 高建华
 * @date 2018/6/21 下午2:03
 */
@Slf4j
public class HttpServer {

    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap bootstrap;
    private Environment environment;

    public HttpServer(Environment environment) {
        this.environment = environment;
        int bossSize = Integer.parseInt(System.getProperty("server.boss.size", "2"));
        int workerSize = Integer.parseInt(System.getProperty("server.worker.size", "4"));
        if (Epoll.isAvailable()) {
            boss = new EpollEventLoopGroup(bossSize, new DefaultThreadFactory("NettyServerBoss", true));
            worker = new EpollEventLoopGroup(workerSize, new DefaultThreadFactory("NettyServerWorker", true));
        } else {
            boss = new NioEventLoopGroup(bossSize, new DefaultThreadFactory("NettyServerBoss", true));
            worker = new NioEventLoopGroup(workerSize, new DefaultThreadFactory("NettyServerWorker", true));
        }
        bootstrap = new ServerBootstrap();

        bootstrap.group(boss, worker);
        bootstrap.channel(worker instanceof EpollEventLoopGroup ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000);
        bootstrap.option(ChannelOption.SO_BACKLOG, NetUtil.SOMAXCONN);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, false);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.SO_RCVBUF, 24 * 1024);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 24 * 1024);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                        .addLast("decoder", new HttpRequestDecoder())
                        .addLast("support-compressor", new HttpContentCompressor())
                        .addLast("support-aggregator", new HttpObjectAggregator(1024 * 1024))
                        .addLast("encoder", new HttpResponseEncoder())
                        .addLast("chunk", new ChunkedWriteHandler())
                        .addLast("business-handler", new HttpServerHandler(environment));
            }
        });
    }

    public synchronized void start() throws InterruptedException {

        ChannelFuture channelFuture = bootstrap.bind(environment.getPort()).sync();
        log.info("启动成功！端口：{}", environment.getPort());
        if(environment.isEnableWebsocket()){
            log.info("WebSocket已开启！端口：{}", environment.getPort());
        }
        Channel channel = channelFuture.channel();
        channel.closeFuture().addListener(future -> log.info("停止服务成功，端口：{}", environment.getPort())).sync();
        // 监听服务器关闭监听
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * 优雅停机
     */
    private void shutdown() {
        if (boss != null && !boss.isShuttingDown() && !boss.isShutdown()) {
            boss.shutdownGracefully();
        }
        if (worker != null && !worker.isShuttingDown() && !worker.isShutdown()) {
            worker.shutdownGracefully();
        }
    }

}
