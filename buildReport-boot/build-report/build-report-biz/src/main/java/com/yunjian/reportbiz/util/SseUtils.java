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

import com.yunjian.common.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class SseUtils {

    public static SseEmitter runAsyncWithHeartbeat(SupplierWithException<String> task, long timeoutMs) {
        int charsPerChunk = 512 * 1024;
        SseEmitter emitter = new SseEmitter(timeoutMs);

        Thread thread = new Thread(() -> {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return task.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            try {
                while (true) {
                    if (future.isDone()) {
                        String result = future.get();
                        if (result != null && !result.isEmpty()) {
                            int length = result.length();
                            int chunkId = 0;

                            for (int start = 0; start < length; start += charsPerChunk) {
                                int end = Math.min(start + charsPerChunk, length);
                                String chunk = result.substring(start, end);

                                emitter.send(SseEmitter.event()
                                    .data(chunk)
                                    .id(String.valueOf(chunkId++)));
                            }
                        }
                        emitter.send("finished");
                        emitter.complete();
                        break;
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                handException(emitter, e);
            }
        });

        thread.setDaemon(true);
        thread.start();

        SseHeartbeatManager.getInstance().registerEmitter(emitter);

        return emitter;
    }

    private static void handException(SseEmitter emitter, Exception e) {
        log.error("处理异常，", e);
        try {
            emitter.send(SseEmitter.event().name("error").data(R.failed(e.getMessage())));
            emitter.send("finished");
        } catch (Exception ex) {
            log.error("发送error信息异常，", ex);
        } finally {
            emitter.complete();
        }
    }

    // 函数式接口：允许抛出 checked exception
    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
