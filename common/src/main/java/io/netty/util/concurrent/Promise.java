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
package io.netty.util.concurrent;

/**
 * Special {@link Future} which is writable.
 * 继承自Future接口
 */
public interface Promise<V> extends Future<V> {

    /**
     * 将此Future标记为成功，并通知所有监听器
     */
    Promise<V> setSuccess(V result);

    /**
     * 将此Future标记为成功，并通知所有监听器，返回是否成功
     */
    boolean trySuccess(V result);

    /**
     * 将此Future标记为失败，并通知所有监听器
     */
    Promise<V> setFailure(Throwable cause);

    /**
     * 将此Future标记为失败，并通知所有监听器，返回是否成功
     */
    boolean tryFailure(Throwable cause);

    /**
     * 将此Future标记为可能被取消
     */
    boolean setUncancellable();

    /**
     * 添加一个监听器
     */
    @Override
    Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    /**
     * 添加一组监听器
     */
    @Override
    Promise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    /**
     * 移除一个监听器
     */
    @Override
    Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);

    /**
     * 移除一组监听器
     */
    @Override
    Promise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    /**
     * 等待完成
     */
    @Override
    Promise<V> await() throws InterruptedException;

    /**
     * 不可中断等待完成
     */
    @Override
    Promise<V> awaitUninterruptibly();

    /**
     * 异步转同步等待
     */
    @Override
    Promise<V> sync() throws InterruptedException;

    /**
     * 异步转同步等待，不可中断返回
     */
    @Override
    Promise<V> syncUninterruptibly();
}
