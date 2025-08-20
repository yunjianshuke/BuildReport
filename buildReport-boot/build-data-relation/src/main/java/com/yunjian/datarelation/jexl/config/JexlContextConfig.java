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
package com.yunjian.datarelation.jexl.config;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.yunjian.datarelation.jexl.utils.DataProcessUtil;
import com.yunjian.datarelation.jexl.utils.NumberUtil;
import com.yunjian.datarelation.jexl.utils.TypeConversionUtil;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;

/**
 * @description: JexlContext配置类，配置自定义工具类，加载到JexlContext中
 * @version: 1.0.0
 * @author: caolg
 * @date: 2023/12/28
 **/
public class JexlContextConfig {

    private JexlContextConfig() {
    }

    private static final Map<String, Object> CONTEXT_MAP = new HashMap<>(4);

    static {
        CONTEXT_MAP.put("strUtil", CharSequenceUtil.class);
        CONTEXT_MAP.put("dateUtil", DateUtil.class);
        CONTEXT_MAP.put("tcUtil", TypeConversionUtil.class);
        CONTEXT_MAP.put("dpUtil", DataProcessUtil.class);
        CONTEXT_MAP.put("numUtil", NumberUtil.class);
    }

    public static JexlContext getJexlContext() {
        JexlContext context = new MapContext();
        for (Map.Entry<String, Object> entry : CONTEXT_MAP.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
        return context;
    }


}
