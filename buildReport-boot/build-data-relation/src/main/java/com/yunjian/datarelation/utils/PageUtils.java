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
package com.yunjian.datarelation.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yujian
 **/
public class PageUtils {

    private PageUtils() {
    }

    ;

    public static <T, R> Page<R> mapRecords(IPage<T> page, Function<T, R> map) {
        return mapRecords(page, map, null);
    }

    public static <T, R> Page<R> mapRecords(IPage<T> page, Function<T, R> map, Comparator<R> sorted) {
        if (page == null || map == null) {
            throw new IllegalArgumentException("MapRecords ArgumentException");
        }
        Page<R> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        Stream<R> rStream = page.getRecords().stream().map(map);
        if (sorted != null) {
            resultPage.setRecords(rStream.sorted(sorted).collect(Collectors.toList()));
        } else {
            resultPage.setRecords(rStream.collect(Collectors.toList()));
        }
        return resultPage;
    }

    public static <T, R> Page<R> mapAfterFilterRecords(IPage<T> page, Predicate<R> filter, Function<T, R> map,
                                                       Comparator<R> sorted) {
        if (filter == null) {
            return mapRecords(page, map, sorted);
        }
        if (page == null || map == null) {
            throw new IllegalArgumentException("MapRecords And AfterFilter ArgumentException");
        }
        Page<R> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        Stream<R> rStream = page.getRecords().stream().map(map);
        if (sorted != null) {
            resultPage.setRecords(rStream.sorted(sorted).filter(filter).collect(Collectors.toList()));
        } else {
            resultPage.setRecords(rStream.filter(filter).collect(Collectors.toList()));
        }
        return resultPage;
    }
}
