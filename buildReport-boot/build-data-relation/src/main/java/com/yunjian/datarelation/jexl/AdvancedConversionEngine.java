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
package com.yunjian.datarelation.jexl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONNull;
import com.yunjian.datarelation.jexl.config.JexlContextConfig;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.internal.Engine;
import org.springframework.stereotype.Component;


/**
 * @description: 高级转换引擎
 * @author: caolg
 * @date: 2023/12/28
 **/
@Slf4j
@Component
public class AdvancedConversionEngine {

    private static final JexlEngine ENGINE = new Engine();
    // 出参后缀标识
    private static final String OUT = "_OUT";
    // 入参后缀标识
    private static final String IN = "_IN";

    private static final String PAH = "%";
    private static final String PS = "#";


    /**
     * 转换方法
     *
     * @param param  参数，key变量名； value变量值
     * @param expStr 代码块 或 表达式
     * @return
     */
    public String convert(Map<String, Object> param, String expStr) {
        if (StrUtil.isEmpty(expStr)) {
            throw new IllegalArgumentException("Expression string cannot be null or empty");
        }

        try {

            JexlContext context = JexlContextConfig.getJexlContext();
            for (Map.Entry<String, Object> entry : param.entrySet()) {

                if (NumberUtil.isNumber(entry.getValue() + "")) {
                    context.set(entry.getKey(), new BigDecimal(entry.getValue() + ""));
                } else {
                    context.set(entry.getKey(), entry.getValue());
                }
            }

            JexlScript script = ENGINE.createScript(expStr);
            Object execute = script.execute(context);
            return execute.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("无法计算该表达式={},参数={}", expStr, param);
            return "";
        }
    }

    public Set<List<String>> getVariables(String expStr) {
        JexlScript script = ENGINE.createScript(expStr);
        return script.getVariables();
    }


    public static void main(String[] args) {
        AdvancedConversionEngine advancedConversionEngine = new AdvancedConversionEngine();
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("A1", 1);
        objectObjectHashMap.put("A2", 2);


        String exp = "a = A1+A2; a = a + 2; if (a >= 5) {return a }else{return 1}; ";

        JexlScript script = ENGINE.createScript(exp);
        for (List<String> variable : script.getVariables()) {
            System.out.println(variable);
        }

        String convert = advancedConversionEngine.convert(objectObjectHashMap, exp);
        System.out.println(convert);

    }

    /**
     * 转换方法
     *
     * @param expStr     代码块 或 表达式
     * @param inputData  输入参数
     * @param outputData 输出参数
     * @return
     */
    public Object convert(String expStr, DataRecord inputData, DataRecord outputData) throws Exception {
        try {

            if (StrUtil.isEmpty(expStr)) {
                throw new IllegalArgumentException("Expression string cannot be null or empty");
            }

            JexlContext context = JexlContextConfig.getJexlContext();
            // 设置输入字段参数
            for (Map.Entry<String, Object> entry : inputData.entrySet()) {
                context.set(entry.getKey() + IN, entry.getValue() == JSONNull.NULL ? "" : entry.getValue());
            }

            // 设置输出字段参数
            for (Map.Entry<String, Object> entry : outputData.entrySet()) {
                context.set(entry.getKey() + OUT,
                        entry.getValue() == JSONNull.NULL ? "" : entry.getValue());
            }

            // 构造正则表达式
            String regex = PAH + "[^" + PAH + "]+" + PAH + "|" + PS + "[^" + PS + "]+" + PS;
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(expStr);

            // 查找匹配结果
            while (matcher.find()) {
                if (matcher.group().startsWith(PAH)) {
                    expStr = expStr.replace(matcher.group(), matcher.group().replaceAll(PAH, "") + IN);
                } else if (matcher.group().startsWith(PS)) {
                    expStr = expStr.replace(matcher.group(), matcher.group().replaceAll(PS, "") + OUT);
                }
            }
            JexlScript script = ENGINE.createScript(expStr);
            return script.execute(context);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("unsolvable function/method '")) {
                message = message.substring(message.indexOf("'") + 1, message.lastIndexOf("'"));
                log.error("函数{}使用有误，请参考说明", message);
                throw new Exception("函数" + message + "使用有误，请参考说明");
            }
            log.error("函数使用有误，请检查函数表达式:", e);
            throw new Exception("函数使用有误，请检查函数表达式");
        }
    }
}
