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
package io.netty.channel;

import java.net.SocketAddress;

/**
 * {@link ChannelHandler} which will get notified for IO-outbound-operations.
 * 对pipeline进行，从右到左添加处理器
 */
public interface ChannelOutboundHandler extends ChannelHandler {
    /**
     * 绑定操作
     */
    void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception;

    /**
     * 连接操作
     */
    void connect(
            ChannelHandlerContext ctx, SocketAddress remoteAddress,
            SocketAddress localAddress, ChannelPromise promise) throws Exception;

    /**
     * 断开连接
     */
    void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * 关闭连接
     */
    void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * Called once a deregister operation is made from the current registered {@link EventLoop}.
     *
     * @param ctx               the {@link ChannelHandlerContext} for which the close operation is made
     * @param promise           the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception        thrown if an error occurs
     */
    void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * 读
     */
    void read(ChannelHandlerContext ctx) throws Exception;

    /**
     * 写
     */
    void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;

    /**
     * 刷
     */
    void flush(ChannelHandlerContext ctx) throws Exception;
}
