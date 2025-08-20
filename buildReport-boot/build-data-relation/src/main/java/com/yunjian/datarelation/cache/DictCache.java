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
package com.yunjian.datarelation.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.config.ReportProperties.DictCacheProperties;
import com.yunjian.datarelation.entity.ReportDictCondition;
import com.yunjian.datarelation.entity.ReportDictConf;
import com.yunjian.datarelation.event.DictChangeEvent;
import com.yunjian.datarelation.pool.DatasourcePoolManager;
import com.yunjian.datarelation.pool.Ds;
import com.yunjian.datarelation.service.impl.ReportDictConditionService;
import com.yunjian.datarelation.service.impl.ReportDictConfService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;

/**
 * 用来缓存用户配置的各种字典，用于业务线字段绑定字典来减少关联
 *
 * @author yujian
 **/
@RequiredArgsConstructor
@Slf4j
public class DictCache implements InitializingBean, ApplicationListener<DictChangeEvent> {

    private final ReportDictConfService reportDictConfService;
    private final ReportDictConditionService reportDictConditionService;
    private final DatasourcePoolManager datasourcePoolManager;
    private final DictCacheProperties dictCacheProperties;
    private static final ScheduledExecutorService job;
    /**
     * 全局字典缓存
     */
    private static final Map<Long, Dict> DICT_CACHE;

    static {
        DICT_CACHE = new ConcurrentHashMap<>();
        job = Executors.newScheduledThreadPool(1);
    }

    /**
     * 将值映射为显示值
     *
     * @param dictId 字典id
     * @param value  值
     * @return 显示值
     */
    public Object mapping(Long dictId, Object value) {
        Dict dict = DICT_CACHE.get(dictId);
        if (dict != null) {
            return dict.getValue(value);
        }
        return value;
    }

    public Dict getDict(Long dictId) {
        return DICT_CACHE.get(dictId);
    }

    /**
     * 删除字典项
     *
     * @param dictConfId dictConfId
     */
    public void removeDict(Long dictConfId) {
        log.info("【字典缓存】删除缓存用户配置字典项 {}", dictConfId);
        Dict dict = DICT_CACHE.get(dictConfId);
        dict.getKv().clear();
        DICT_CACHE.remove(dictConfId);
    }

    public void removeAllDict() {
        for (Dict dict : DICT_CACHE.values()) {
            dict.getKv().clear();
        }
        DICT_CACHE.clear();
    }

    /**
     * 添加字典
     *
     * @param dictConfId dictConfId
     */
    public void addDict(Long dictConfId) {
        log.info("【字典缓存】添加缓存用户配置字典项 {}", dictConfId);
        refresh();
    }

    /**
     * 刷新字典到缓存
     */
    public void refresh() {

        removeAllDict();
        List<ReportDictConf> dictConfList;
        List<ReportDictCondition> dictConditions;
        dictConfList = reportDictConfService.list();
        dictConditions = reportDictConditionService.list();


        Map<Long, List<ReportDictCondition>> conditionMap = dictConditions.stream()
                .collect(Collectors.groupingBy(ReportDictCondition::getDictConfId));
        for (ReportDictConf conf : dictConfList) {
            Optional<Ds> datasource =
                    datasourcePoolManager.getDatasource(conf.getDatasourceId());
            if (datasource.isPresent()) {
                Ds ds = datasource.get();
                StringBuilder sql = new StringBuilder(
                        StrUtil.format(" select {},{} from {} ", conf.getDictValueField(),
                                conf.getDictLabelField(), conf.getTableName()));
                List<ReportDictCondition> conditions = conditionMap.getOrDefault(conf.getId(), Collections.emptyList());
                if (CollUtil.isNotEmpty(conditions)) {
                    sql.append(" where 1=1 ");
                    for (ReportDictCondition condition : conditions) {
                        sql.append(StrUtil.format(" and {} = '{}' ", condition.getConditionKey(),
                                condition.getConditionValue()));
                    }
                }
                // 一个字典
                Map<String, Object> dictMap = ds.template.queryForList(sql.toString(), Collections.emptyMap()).stream()
                        .map(AnyData::new).collect(
                                Collectors.toMap(data -> String.valueOf(data.getObj(conf.getDictValueField())),
                                        data -> data.getObj(conf.getDictLabelField()), (o, n) -> "映射到多个字典项,请检查字典是否配置正确"));
                Optional<Ds> datasourceDs = datasourcePoolManager.getDatasource(conf.getDatasourceId());
                Ds ds1 = datasourceDs.orElse(null);
                DICT_CACHE.put(conf.getId(), new Dict(conf.getTableName(), conf.getDatasourceId(), ds1 == null ? "" : ds1.getOptions()
                        .getDatabase(), conf.getDictValueField(), conf.getDictLabelField(), conf.getDefaultLabel(), dictMap));
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("【字典缓存】开始缓存用户配置字典项..");
        refresh();
        int period =
                dictCacheProperties.getRefreshTime() == null ? 600 : dictCacheProperties.getRefreshTime();
        log.info("【字典缓存】job {} 秒刷新一次启动..", period);

        job.scheduleAtFixedRate(this::refresh, 60, period, TimeUnit.SECONDS);
    }

    @Override
    public void onApplicationEvent(DictChangeEvent event) {
        switch (event.getEventEnum()) {
            case REMOVE:
                removeDict(event.getDictConfId());
                break;
            case ADD:
                addDict(event.getDictConfId());
                break;
            default:
        }
    }

    /**
     * 字典缓存对象
     */
    @Data
    @AllArgsConstructor
    public static class Dict {
        private String tableName;
        private Long datasourceId;
        private String datasourceName;
        private String value;
        private String label;
        private String defaultLabel;
        private Map<String, Object> kv;

        private Object getValue(Object value) {
            Object v = kv.get(String.valueOf(value));
            if (v == null) {
                return defaultLabel == null ? "" : defaultLabel;
            } else {
                return v;
            }
        }
    }


}
