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
package com.yunjian.datarelation.pool;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yujian
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogicDelete {

    /**
     * 逻辑删除字段 例如 del_flag
     */
    private String logicFieldName;
    /**
     * 值
     */
    private String value;

    public boolean hasLogicDelete() {
        return StrUtil.isAllNotBlank(logicFieldName, value);
    }
}
