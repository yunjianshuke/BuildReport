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
package com.yunjian.common.util;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Bruce
 */
public class RegexUtils {

    /**
     * 校验防止sql注入
     *
     * @param
     */
    public static void verifyPageFileld(List<OrderItem> orderItem) {
        Optional.ofNullable(orderItem).ifPresent(oi -> {
            oi.forEach(o -> {
                boolean rightfulString = RegexUtils.isRightfulString(o.getColumn());
                if (!rightfulString) {
                    throw new IllegalArgumentException("参数中含有非法的列名:" + o.getColumn());
                }
            });
        });
    }


    /**
     * 判断是否为合法字符(a-zA-Z0-9-_)
     *
     * @param text
     * @return
     */
    public static boolean isRightfulString(String text) {
        return match(text, "^[A-Za-z0-9_-]+$");
    }

    /**
     * 正则表达式匹配
     *
     * @param text 待匹配的文本
     * @param reg  正则表达式
     * @return
     */
    private static boolean match(String text, String reg) {
        if (StrUtil.isBlank(text) || StrUtil.isBlank(reg)) {
            return false;
        }
        return Pattern.compile(reg).matcher(text).matches();
    }


}
