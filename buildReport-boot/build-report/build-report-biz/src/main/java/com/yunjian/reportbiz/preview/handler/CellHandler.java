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
package com.yunjian.reportbiz.preview.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yujian
 **/
public interface CellHandler {

    /**
     * 单元格处理方法
     */
    void handle();

    /**
     * 格式化
     *
     * @param text      待格式化
     * @param formatter 表达式
     * @return 格式化后
     */
    default String format(String text, String formatter) {
        try {
            LocalDateTime parse = LocalDateTime.parse(text);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatter);
            return dateTimeFormatter.format(parse);
        } catch (Exception e) {
            return text;
        }
    }

}
