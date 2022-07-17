/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a client-side channel.
 */
public class DiscardClientHandler extends SimpleChannelInboundHandler<Object> {

    //内容字节缓冲区
    private ByteBuf content;
    private ChannelHandlerContext ctx;

    //当通道激活时，回调此方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;

        //初始化消息
        content = ctx
                //分配缓冲区
                .alloc()
                //设置直接内存缓冲区大小为DiscardClient.SIZE，256
                .directBuffer(DiscardClient.SIZE)
                //向缓冲区中国呢写入Obyte，大小为DiscardClient.SIZE，256
                .writeZero(DiscardClient.SIZE);

        //发送初始化后的数据
        generateTraffic();
    }

    //当通道变为不活跃时，回调此方法
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //释放分配的缓冲区对象
        content.release();
    }

    //当读取到服务端发送的数据时，回调此方法
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Server is supposed to send nothing, but if it sends something, discard it.
    }

    //当发生了异常，回调此方法
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //打印异常信息
        cause.printStackTrace();
        //关闭连接
        ctx.close();
    }

    //计时器
    long counter;

    //生成传输数据
    private void generateTraffic() {
        //将数据写入socket中并且刷出到服务器
        ctx.writeAndFlush(content.retainedDuplicate())
                //添加监听器对象
                .addListener(trafficGenerator);
    }

    //监听器对象，用于监听ChannelHandlerCntext上下文操作
    private final ChannelFutureListener trafficGenerator = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
            if (future.isSuccess()) {
                generateTraffic();
            } else {
                //异常完成，打印异常信息
                future.cause().printStackTrace();
                //关闭通道
                future.channel().close();
            }
        }
    };
}
