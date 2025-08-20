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
package com.yunjian.reportbiz.preview;

import java.util.List;

/**
 * @author yujian
 */
public interface FunCellProcess {

    /**
     * 处理单个聚合
     *
     * @param values 数据
     * @return 返回结果
     */
    Object processSingle(List<Object> values);

    /**
     * 处理多个个聚合
     *
     * @param values 数据
     * @return 需要复制多少条数据
     */
    List<Object> processMultiple(List<List<Object>> values);

}
