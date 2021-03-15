package com.example.nettysourcedemo;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sk
 */
@Slf4j
@ChannelHandler.Sharable
public class SimpleHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        MyMessage message = new MyMessage();
        message.setCode(1);
        message.setMessage("aaa");
        message.setMsg(msg);
        ctx.fireChannelRead(message);
    }
}
