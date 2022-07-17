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
package io.netty.example.http.helloworld;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public final class HttpHelloWorldServer {

    //通过系统变量检测是否开启了SSL
    static final boolean SSL = System.getProperty("ssl") != null;
    //通过系统变量获取端口号
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
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
            //创建服务端启动器
            ServerBootstrap b = new ServerBootstrap();
            //设置Server通道的SO_BACKLOG为1024
            b.option(ChannelOption.SO_BACKLOG, 1024);
            //将上面两个事件循环组对象放入server启动器中
            b.group(bossGroup, workerGroup)
             //设置服务端的ServerSocketChannel为NIOServerSocketChannel
             .channel(NioServerSocketChannel.class)
             //设置处理ServerSocketChannel接收到的客户端事件处理器，也即打日志处理器
             .handler(new LoggingHandler(LogLevel.INFO))
             //设置接收到socket客户端请求的处理器
             .childHandler(new HttpHelloWorldServerInitializer(sslCtx));

            //绑定端口，并开始接收传入的连接
            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            //等待直到服务器套接字关闭
            ch.closeFuture().sync();
        } finally {
            //优雅的关闭主/从事件循环组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
