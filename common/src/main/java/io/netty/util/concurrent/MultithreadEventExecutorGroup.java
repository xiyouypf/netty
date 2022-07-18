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
package io.netty.util.concurrent;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base class for {@link EventExecutorGroup} implementations that handles their tasks with multiple threads at
 * the same time.
 */
public abstract class MultithreadEventExecutorGroup extends AbstractEventExecutorGroup {

    //事件执行器组
    private final EventExecutor[] children;
    private final Set<EventExecutor> readonlyChildren;
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
    //选择对象
    private final EventExecutorChooserFactory.EventExecutorChooser chooser;

    /**
     * Create a new instance.
     *
     * @param nThreads          the number of threads that will be used by this instance.
     * @param threadFactory     the ThreadFactory to use, or {@code null} if the default should be used.
     * @param args              arguments which will passed to each {@link #newChild(Executor, Object...)} call
     */
    protected MultithreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        this(nThreads, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
    }

    /**
     * 参数一：内部线程数量
     * 参数二：执行器
     * args[0]：选择器提供器，通过这个可以获取到JDK层面的selector实例
     * args[1]：选择器工作策略 ，DefaultSelectStrategy
     * args[2]：线程池拒绝策略
     */
    protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
        //参数一：内部线程数量
        //参数二：执行器
        //参数三：事件执行器选择器工厂
        //args[0]：选择器提供器，通过这个可以获取到JDK层面的selector实例
        //args[1]：选择器工作策略 ，DefaultSelectStrategy
        //args[2]：线程池拒绝策略
        this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
    }

    /**
     * 参数一：内部线程数量
     * 参数二：执行器
     * 参数三：事件执行器选择器工厂
     * args[0]：选择器提供器，通过这个可以获取到JDK层面的selector实例
     * args[1]：选择器工作策略 ，DefaultSelectStrategy
     * args[2]：线程池拒绝策略
     */
    protected MultithreadEventExecutorGroup(int nThreads,
                                            Executor executor,
                                            EventExecutorChooserFactory chooserFactory,
                                            Object... args) {
        //校验线程数不能小于等于0
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }

        //入股执行器为空，那么创建ThreadPerTaskExecutor对象
        if (executor == null) {
            // 真正生产出来执行任务的线程的作用，execute（）
            executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
        }

        //创建事件执行器数组，数组大小为nThreads
        children = new EventExecutor[nThreads];

        for (int i = 0; i < nThreads; i ++) {
            boolean success = false;
            try {
                //创建NioEventLoop实例
                //executor：ThreadPerTaskExecutor实例，这个实例包含着一个ThreadFactory实例，通过内部线程工厂可以制造线程
                children[i] = newChild(executor, args);
                success = true;
            } catch (Exception e) {
                // TODO: Think about if this is a good exception type
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                //如果没有创建成功
                if (!success) {
                    //遍历children数组的所有EventExecutor对象
                    for (int j = 0; j < i; j ++) {
                        //调用EventExecutor的shutdownGracefully方法关闭
                        children[j].shutdownGracefully();
                    }
                    //遍历等待所有的EventExecutor关闭
                    for (int j = 0; j < i; j ++) {
                        EventExecutor e = children[j];
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interrupted) {
                            // Let the caller handle the interruption.
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }

        //根据children实例的数量，创建一个chooser选择对象
        //后面，外部资源想要 获取或注册。。到NioEventLoop，都是通过chooser来分配NioEventLoop的
        chooser = chooserFactory.newChooser(children);

        //创建一个结束监听器对象
        final FutureListener<Object> terminationListener = new FutureListener<Object>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (terminatedChildren.incrementAndGet() == children.length) {
                    terminationFuture.setSuccess(null);
                }
            }
        };

        //遍历所有的EventExecutor对象，向他们添加终结监听器
        for (EventExecutor e: children) {
            //为每一个NioEventLoop添加监听
            e.terminationFuture().addListener(terminationListener);
        }

        Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
        Collections.addAll(childrenSet, children);
        //将该集合变为一个只读的Children集合
        readonlyChildren = Collections.unmodifiableSet(childrenSet);
    }

    /**
     * 构建出来一个线程工厂，用来生产具体线程实例的工厂
     */
    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultThreadFactory(getClass());
    }

    @Override
    public EventExecutor next() {
        return chooser.next();
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return readonlyChildren.iterator();
    }

    /**
     * Return the number of {@link EventExecutor} this implementation uses. This number is the maps
     * 1:1 to the threads it use.
     */
    public final int executorCount() {
        return children.length;
    }

    /**
     * Create a new EventExecutor which will later then accessible via the {@link #next()}  method. This method will be
     * called for each thread that will serve this {@link MultithreadEventExecutorGroup}.
     *
     */
    protected abstract EventExecutor newChild(Executor executor, Object... args) throws Exception;

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        for (EventExecutor l: children) {
            l.shutdownGracefully(quietPeriod, timeout, unit);
        }
        return terminationFuture();
    }

    @Override
    public Future<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    @Deprecated
    public void shutdown() {
        for (EventExecutor l: children) {
            l.shutdown();
        }
    }

    @Override
    public boolean isShuttingDown() {
        for (EventExecutor l: children) {
            if (!l.isShuttingDown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isShutdown() {
        for (EventExecutor l: children) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        for (EventExecutor l: children) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        loop: for (EventExecutor l: children) {
            for (;;) {
                long timeLeft = deadline - System.nanoTime();
                if (timeLeft <= 0) {
                    break loop;
                }
                if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
                    break;
                }
            }
        }
        return isTerminated();
    }
}
