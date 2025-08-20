/*
 * Copyright 2025-2030 大连云建数科科技有限公司.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunjian.reportbiz.util;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE心跳管理器单例，统一管理所有活跃的SseEmitter并发送心跳
 */
public class SseHeartbeatManager {
    private static volatile SseHeartbeatManager instance;

    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService scheduler;

    private SseHeartbeatManager() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "sse-global-heartbeat-thread");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(this::sendHeartbeats, 0, 1, TimeUnit.SECONDS);
    }

    public static SseHeartbeatManager getInstance() {
        if (instance == null) {
            synchronized (SseHeartbeatManager.class) {
                if (instance == null) {
                    instance = new SseHeartbeatManager();
                }
            }
        }
        return instance;
    }

    public void registerEmitter(SseEmitter emitter) {
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
    }

    private void sendHeartbeats() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        emitters.clear();
    }
}
