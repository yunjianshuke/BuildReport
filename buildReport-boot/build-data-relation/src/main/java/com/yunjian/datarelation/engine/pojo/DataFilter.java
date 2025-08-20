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
package com.yunjian.datarelation.engine.pojo;

import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.util.Arrays;
import java.util.List;

/**
 * @author yujian
 **/
@lombok.Data
public class DataFilter {

    private static List<String> listGroup = Arrays.asList("IN", "NOT_IN");

    private String datasetName;
    private String cellId;
    private String key;
    private String name;
    private String type;
    private String alias;
    private String condition;
    private Object defaultValue;
    private Component component;

    public String getUniqueKey() {
        if (null == key) {
            return datasetName + StringPool.UNDERSCORE + "empty" + StringPool.UNDERSCORE + alias;
        }
        String[] split = key.split(StringPool.UNDERSCORE);
        if (split.length >= 2) {
            return datasetName + StringPool.UNDERSCORE + split[0] + StringPool.UNDERSCORE + alias;
        }
        return key;
    }


    public boolean isAutoSave() {

        if (listGroup.indexOf(condition) > 0) {
            return true;
        } else if ("VARCHAR".equals(type)) {
            return true;
        }
        return false;
    }


    @lombok.Data
    public static class Component {

        private String type;
        private String label;
        private Object value;
        private Options options;


    }

    @lombok.Data
    public static class Options {

        private List<Data> data;
        private String type;
        private String dataset;
        private String label;
        private String value;
        private Boolean multiple;
    }

    @lombok.Data
    public static class Data {

        private String label;
        private String value;
    }

}
