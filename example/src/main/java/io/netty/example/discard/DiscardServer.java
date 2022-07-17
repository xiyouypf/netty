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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Discards any incoming data.
 */
public final class DiscardServer {

    //通过系统变量检测是否开启了SSL
    static final boolean SSL = System.getProperty("ssl") != null;
    //通过系统变量获取端口号
    static final int PORT = Integer.parseInt(System.getProperty("port", "8009"));

    public static void main(String[] args) throws Exception {
        //如果设置了SSL，那就配置上
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        //创建主（boss）事件循环组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //创建从（worker）事件循环组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建一个服务端启动器
            ServerBootstrap b = new ServerBootstrap();
            //将上面两个事件循环组对象放入服务端启动器中
            b.group(bossGroup, workerGroup)
             //设置服务端的ServerSocketChannel为NioServerSocketChannel
             .channel(NioServerSocketChannel.class)
             //设置处理ServerSocketChannel接收到的客户端事件处理器，也即打日志处理器
             .handler(new LoggingHandler(LogLevel.INFO))
             //设置接收到socket客户端请求的处理器
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 //将SocketChannel对象放入通道流水线对象中处理
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc()));
                     }
                     //向流水线末尾添加一个自定义的数据处理器
                     p.addLast(new DiscardServerHandler());
                 }
             });

            //绑定端口，并开始接收传入的连接
            ChannelFuture f = b.bind(PORT).sync();

            //等待直到服务器套接字关闭
            f.channel().closeFuture().sync();
        } finally {
            //优雅的关闭主/从服务器
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
