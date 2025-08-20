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
package com.yunjian.reportbiz.preview.wrappers;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yujian
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class StaticCellWrapper extends CellWrapper {

    public final static String[] FUNCTION = new String[]{"=SUM", "=AVG", "=MAX", "=MIN", "=COUNT"};


    int funIndex = -1;
    boolean isExpression = false;

    List<List<Integer>> cellXy;

    public List<String> getFun() {
        Object text = getOrigin().getText();
        for (int index = 0; index < FUNCTION.length; index++) {
            String fun = FUNCTION[index];
            String value = (String) text;
            if (value != null && value.startsWith(fun)) {
                funIndex = index;
                int i = value.indexOf(StringPool.LEFT_BRACKET);
                int j = value.lastIndexOf(StringPool.RIGHT_BRACKET);
                String substring = value.substring(i + 1, j);
                if (substring.indexOf(StringPool.COMMA) > 0) {
                    return StrUtil.split(substring, StringPool.COMMA);
                } else {
                    return Collections.singletonList(substring);
                }
            }
        }
        return Collections.emptyList();

    }

}
