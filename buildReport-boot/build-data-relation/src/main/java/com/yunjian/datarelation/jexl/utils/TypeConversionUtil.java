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
package com.yunjian.datarelation.jexl.utils;

import cn.hutool.core.convert.Convert;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * @description: 类型转换工具类
 * @version: 1.0.0
 * @author: caolg
 * @date: 2024/01/03
 **/
public class TypeConversionUtil extends Convert {

    /**
     * 该函数将输入文本转换成整数输出。  若输入不是整数格式则转换失败。 arg是要转换为long的值。 如果参数为null，则函数返回null
     *
     * @param arg
     * @return
     */
    public static Long str2long(String arg) {
        if (arg == null) {
            return null;
        }

        try {
            return Long.parseLong(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to convert '" + arg + "' to a long", e);
        }
    }

    /**
     * 该函数将输入文本转换成整数输出。 参数radix指定输出的进制。若输入不是整数格式则转换失败。 arg是要转换为long的值。 如果参数为null，则函数返回null
     *
     * @param arg   参数
     * @param radix 输出的进制
     * @return
     */
    public static Long str2long(String arg, int radix) {
        if (arg == null) {
            return null;
        }

        try {
            return Long.parseLong(arg, radix);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to convert '" + arg + "' to a long", e);
        }
    }


    public static Double str2double(String arg) {
        if (arg == null) {
            return null;
        }
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to convert '" + arg + "' to a double", e);
        }
    }

    public static Double str2double(String arg, String format) {
        if (arg == null) {
            return null;
        }
        try {
            NumberFormat numberFormat = new DecimalFormat(format);
            return numberFormat.parse(arg).doubleValue();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to convert '" + arg + "' to a double", e);
        }
    }


    /**
     * 该函数将输入的整数或数值转换为文本输出。
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }


}
