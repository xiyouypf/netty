/*
 * Copyright 2013 The Netty Project
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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpHelloWorldServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    //常量字节数组
    private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

    //当通道读取完成时，回调此方法
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //将CTX中的内容刷出
        ctx.flush();
    }

    //当通道内容可读取时，回调此方法
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        //判断消息体是否为HttpRequest对象
        if (msg instanceof HttpRequest) {
            //类型转换
            HttpRequest req = (HttpRequest) msg;

            //判断请求是否为keep-alive的头部
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            //创建http响应对象
            FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), OK,
                                                                    Unpooled.wrappedBuffer(CONTENT));
            //设置HTTP头部信息
            response.headers()
                    //设置MIME类型
                    .set(CONTENT_TYPE, TEXT_PLAIN)
                    //设置Cntent-Length头部
                    .setInt(CONTENT_LENGTH, response.content().readableBytes());

            //如果使用了keepalive保活机制，那么设置Connection头部
            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
            } else {
                //告诉客户端我们要断开连接，也即设置Connection头部为Close
                response.headers().set(CONNECTION, CLOSE);
            }

            //向CTX中写入响应信息
            ChannelFuture f = ctx.write(response);

            //如果没有使用KeepAlive操作
            if (!keepAlive) {
                //添加一个监听器，当操作完成时，关闭客户端连接
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    //发生异常时，回调此方法
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
