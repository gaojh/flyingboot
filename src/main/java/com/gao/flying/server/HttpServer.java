package com.gao.flying.server;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.gao.flying.config.ApplicationConfig;
import com.gao.flying.server.handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.NetUtil;

/**
 * @author 高建华
 * @date 2018/6/21 下午2:03
 */
public class HttpServer {

    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap bootstrap;

    private static Log log = LogFactory.get();


    public HttpServer() {
        int bossSize = Integer.parseInt(System.getProperty("server.boss.size", "2"));
        int workerSize = Integer.parseInt(System.getProperty("server.worker.size", "4"));
        boss = new NioEventLoopGroup(bossSize, new NamedThreadFactory("NettyServerBoss", true));
        worker = new NioEventLoopGroup(workerSize, new NamedThreadFactory("NettyServerWorker", true));
        bootstrap = new ServerBootstrap();

        bootstrap.group(boss, worker);
        bootstrap.channel(NioServerSocketChannel.class);
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
                        .addLast("business-handler", new HttpServerHandler());
            }
        });
    }

    public synchronized void start() throws InterruptedException {

        ChannelFuture channelFuture = bootstrap.bind(ApplicationConfig.PORT).sync();
        log.info("启动成功，端口：{}", ApplicationConfig.PORT);
        Channel channel = channelFuture.channel();
        channel.closeFuture().addListener(future -> log.info("停止服务成功，端口：{}", ApplicationConfig.PORT)).sync();
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
