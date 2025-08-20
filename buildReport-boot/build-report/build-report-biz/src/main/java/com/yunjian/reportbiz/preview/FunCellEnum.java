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

import cn.hutool.core.util.ObjectUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
public enum FunCellEnum implements FunCellProcess {
    /**
     * 求和
     */
    SUM("=SUM") {
        @Override
        public Object processSingle(List<Object> values) {
            values = values == null ? Collections.emptyList() : values;
            return values.stream()
                    .map(x -> {
                        try {
                            return new BigDecimal(String.valueOf(x));
                        } catch (Exception e) {
                            return null;
                        }
                    }).filter(ObjectUtil::isNotNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public List<Object> processMultiple(List<List<Object>> values) {
            return null;
        }
    },
    /**
     * 求平均值
     */
    AVG("=AVG") {
        @Override
        public Object processSingle(List<Object> values) {
            values = values == null ? Collections.emptyList() : values;
            List<BigDecimal> collect = values.stream()
                    .map(x -> {
                        try {
                            return new BigDecimal(String.valueOf(x));
                        } catch (Exception e) {
                            return null;
                        }
                    }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());

            if (collect.isEmpty()) {
                return 0;
            }

            return collect.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(collect.size()), 2, BigDecimal.ROUND_HALF_DOWN);
        }

        @Override
        public List<Object> processMultiple(List<List<Object>> values) {
            return null;
        }
    },
    /**
     * 求最大值
     */
    MAX("=MAX") {
        @Override
        public Object processSingle(List<Object> values) {
            values = values == null ? Collections.emptyList() : values;
            return values.stream()
                    .map(x -> {
                        try {
                            return new BigDecimal(String.valueOf(x));
                        } catch (Exception e) {
                            return null;
                        }
                    }).filter(ObjectUtil::isNotNull)
                    .max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        }

        @Override
        public List<Object> processMultiple(List<List<Object>> values) {
            return null;
        }
    },
    /**
     * 求最小值
     */
    MIN("=MIN") {
        @Override
        public Object processSingle(List<Object> values) {
            values = values == null ? Collections.emptyList() : values;
            return values.stream()
                    .map(x -> {
                        try {
                            return new BigDecimal(String.valueOf(x));
                        } catch (Exception e) {
                            return null;
                        }
                    }).filter(ObjectUtil::isNotNull)
                    .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        }

        @Override
        public List<Object> processMultiple(List<List<Object>> values) {
            return null;
        }
    },
    /**
     * 求数量
     */
    COUNT("=COUNT") {
        @Override
        public Object processSingle(List<Object> values) {
            values = values == null ? Collections.emptyList() : values;
            return values.size();
        }

        @Override
        public List<Object> processMultiple(List<List<Object>> values) {
            return null;
        }
    };


    private final String fun;

    FunCellEnum(String fun) {
        this.fun = fun;
    }


    public static FunCellEnum of(String fun) {
        for (FunCellEnum value : values()) {
            if (value.fun.equals(fun)) {
                return value;
            }
        }
        return null;
    }


}
