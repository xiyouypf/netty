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

/**
 * 对pipeline进行，从左到右添加处理器
 */
public interface ChannelInboundHandler extends ChannelHandler {

    /**
     * 当通道注册时，回调此方法
     */
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * 当通道取消注册时，回调此方法
     */
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * 通道激活时，回调此方法
     */
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * 通道处于未激活时，回调此方法
     */
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    /**
     * 当通道变为可读取时，回调此方法
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    /**
     * 通道读取完成时，回调此方法
     */
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called if an user event was triggered.
     */
    void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception;

    /**
     * Gets called once the writable state of a {@link Channel} changed. You can check the state with
     * {@link Channel#isWritable()}.
     */
    void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called if a {@link Throwable} was thrown.
     */
    @Override
    @SuppressWarnings("deprecation")
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}
