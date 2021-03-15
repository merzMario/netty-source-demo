package com.example.nettysourcedemo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServerRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        /**
         * NioEventLoopGroup本身是一个eventLoopExecutor,里面包含一个childGroup=EventExecutor[]
         * 数组中是NioEventLoop,每一个NioEventLoop中都包含一个selector,还包含一个executor
         * 这个executor可以用来执行任务,在使用executor.execute执行任务时,会使用ThreadPerTaskExecutor.execute()方法执行
         * 这个方法又会调用DefaultThreadFactory创建一个线程来启动任务的执行
         * 重点关注NioEventLoopGroup构造器中执行的父类构造器
         */
        NioEventLoopGroup boss = new NioEventLoopGroup(new DefaultThreadFactory("boss"));
        NioEventLoopGroup worker = new NioEventLoopGroup(new DefaultThreadFactory("worker"));
        NioEventLoopGroup business = new NioEventLoopGroup(new DefaultThreadFactory("business"));
//        SimpleHandler simpleHandler = new SimpleHandler();
//        RealHandler realHandler = new RealHandler();
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss,worker)
                //调用ReflectiveChannelFactory的构造器，创建NioServerSocketChannel
                .channel(NioServerSocketChannel.class)
                //设置boss线程的属性
                .option(ChannelOption.SO_BACKLOG,1024)
                //设置worker线程的属性，参考Linux tcp参数设置
                .childOption(ChannelOption.TCP_NODELAY,true)
                //ChannelInitializer也是一个ChannelInboundHandler，主要作用是往pipeline中添加handler，添加完handler之后就会从pipeline中移除
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //使用单独的业务线程池执行业务handler
//                        pipeline.addLast(business,"stringDecoder",new StringDecoder());
//                        pipeline.addLast(business,"stringEncoder",new StringEncoder());
                        pipeline.addLast(new IdleStateHandler(0,0,10));
                        pipeline.addLast(new HttpRequestDecoder());
                        pipeline.addLast(new HttpObjectAggregator(65536,true));
                        pipeline.addLast(new HttpResponseEncoder());
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(business,"simpleHandler",new SimpleHandler());
                        pipeline.addLast(business,"realHandler",new RealHandler());
                    }
                });
        ChannelFuture future = b.bind(9080).sync();
        ChannelFuture future1 = b.bind(9081).sync();
        log.info("netty server started");
        future.channel().closeFuture().sync();
        future1.channel().closeFuture().sync();
        boss.shutdownGracefully();
        worker.shutdownGracefully();
        business.shutdownGracefully();
    }
}
