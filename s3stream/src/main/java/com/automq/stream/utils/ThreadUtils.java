/*
 * Copyright 2025, AutoMQ HK Limited.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.automq.stream.utils;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.util.concurrent.FastThreadLocalThread;

/**
 * Utilities for working with threads.
 */
public class ThreadUtils {
    /**
     * Create a new ThreadFactory.
     *
     * @param pattern The pattern to use.  If this contains %d, it will be
     *                replaced with a thread number.  It should not contain more
     *                than one %d.
     * @param daemon  True if we want daemon threads.
     * @return The new ThreadFactory.
     */
    public static ThreadFactory createThreadFactory(final String pattern,
        final boolean daemon) {
        return new ThreadFactory() {
            private final AtomicLong threadEpoch = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                String threadName;
                if (pattern.contains("%d")) {
                    threadName = String.format(pattern, threadEpoch.addAndGet(1));
                } else {
                    threadName = pattern;
                }
                Thread thread = new Thread(r, threadName);
                thread.setDaemon(daemon);
                return thread;
            }
        };
    }

    public static ThreadFactory createFastThreadLocalThreadFactory(String pattern, final boolean daemon) {
        return new ThreadFactory() {
            private final AtomicLong threadEpoch = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                String threadName;
                if (pattern.contains("%d")) {
                    threadName = String.format(pattern, threadEpoch.addAndGet(1));
                } else {
                    threadName = pattern;
                }
                FastThreadLocalThread thread = new FastThreadLocalThread(r, threadName);
                thread.setDaemon(daemon);
                return thread;
            }
        };
    }

    public static Runnable wrapRunnable(Runnable runnable, Logger logger) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                logger.error("[FATAL] Uncaught exception in executor thread {}", Thread.currentThread().getName(), throwable);
            }
        };
    }

    /**
     * A wrapper of {@link #shutdownExecutor} without logging.
     */
    public static void shutdownExecutor(ExecutorService executorService, long timeout, TimeUnit timeUnit) {
        shutdownExecutor(executorService, timeout, timeUnit, NOPLogger.NOP_LOGGER);
    }

    /**
     * Shuts down an executor service in two phases, first by calling shutdown to reject incoming tasks,
     * and then calling shutdownNow, if necessary, to cancel any lingering tasks.
     * After the timeout/on interrupt, the service is forcefully closed.
     */
    public static void shutdownExecutor(ExecutorService executorService, long timeout, TimeUnit timeUnit,
        Logger logger) {
        if (null == executorService) {
            return;
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, timeUnit)) {
                executorService.shutdownNow();
                logger.error("Executor {} did not terminate in time, forcefully shutting down", executorService);
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
