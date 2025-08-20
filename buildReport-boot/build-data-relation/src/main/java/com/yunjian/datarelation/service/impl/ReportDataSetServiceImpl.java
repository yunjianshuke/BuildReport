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
package com.yunjian.datarelation.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.dto.DataSetDTO;
import com.yunjian.datarelation.engine.pojo.DataFilter;
import com.yunjian.datarelation.entity.ReportDataSetV2;
import com.yunjian.datarelation.mapper.ReportDataSetMapper;
import com.yunjian.datarelation.service.ReportDataSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author z7638
 * @description 针对表【report_data_set(数据集表)】的数据库操作Service实现
 * @createDate 2024-07-03 11:27:20
 */
@Service
@Slf4j
public class ReportDataSetServiceImpl extends ServiceImpl<ReportDataSetMapper, ReportDataSetV2>
        implements ReportDataSetService {

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveDataSet(DataSetDTO dataSetDto) {
        ReportDataSetV2 reportDataSetV2;

        if (dataSetDto.isUpdate()) {
            reportDataSetV2 = getById(dataSetDto.getId());
            if (!reportDataSetV2.getName().equals(dataSetDto.getDatasetName())) {
                long count = count(Wrappers.<ReportDataSetV2>lambdaQuery()
                        .eq(ReportDataSetV2::getRelationId, dataSetDto.getRelationId())
                        .eq(ReportDataSetV2::getName, dataSetDto.getDatasetName()));

                if (count > 0) {
                    throw new BusinessException("1", "数据集名称已存在");
                }
            }
        } else {
            long count = count(Wrappers.<ReportDataSetV2>lambdaQuery()
                    .eq(ReportDataSetV2::getRelationId, dataSetDto.getRelationId())
                    .eq(ReportDataSetV2::getName, dataSetDto.getDatasetName()));

            if (count > 0) {
                throw new BusinessException("1", "数据集名称已存在");
            }
            reportDataSetV2 = new ReportDataSetV2();

        }
        reportDataSetV2.setName(dataSetDto.getDatasetName());
        reportDataSetV2.setType(dataSetDto.getType());
        reportDataSetV2.setBusinessType(dataSetDto.getBusinessType());
        reportDataSetV2.setRelationId(Long.valueOf(dataSetDto.getRelationId()));
        reportDataSetV2.setFieldJsonArray(JSONUtil.toJsonStr(dataSetDto.getTables()));
        if (dataSetDto.getBusinessType() == 0) {
            List<String> names = dataSetDto.getDataFilter().stream().map(DataFilter::getUniqueKey)
                    .collect(Collectors.toList());
            Map<String, ReportDataSetV2> nameMap;
            if (CollUtil.isNotEmpty(names)) {
                nameMap = list(
                        Wrappers.<ReportDataSetV2>lambdaQuery().in(ReportDataSetV2::getName, names))
                        .stream().collect(Collectors.toMap(ReportDataSetV2::getName, x -> x));

            } else {
                nameMap = Collections.emptyMap();
            }

            // 在处理设计器的情况下
            List<ReportDataSetV2> collect = dataSetDto.getDataFilter().stream()
                    .filter(DataFilter::isAutoSave)
                    .filter(x -> !nameMap.containsKey(x.getUniqueKey()))
                    .map(x -> {
                        ReportDataSetV2 auto = new ReportDataSetV2();
                        auto.setDataSourceId(dataSetDto.getDataSourceId());
                        auto.setBusinessType(1);
                        auto.setName(x.getUniqueKey());
                        auto.setRelationId(Long.valueOf(dataSetDto.getRelationId()));
                        auto.setDataFilter("");
                        auto.setFieldJsonArray(null);
                        auto.setType(1);
                        return auto;
                    })
                    .collect(Collectors.toList());
            log.info("自动保存筛选数据集数量 {}", collect.size());
            saveBatch(collect);

        }


        reportDataSetV2.setDataFilter(JSONUtil.toJsonStr(dataSetDto.getDataFilter()));
        reportDataSetV2.setDataSourceId(dataSetDto.getDataSourceId());

        if (dataSetDto.isUpdate()) {
            updateById(reportDataSetV2);
        } else {
            save(reportDataSetV2);
        }

    }

    @Override
    public Map<Long, List<ReportDataSetV2>> getRelationMap() {
        List<ReportDataSetV2> reportDataSetV2s = list();

        return reportDataSetV2s.stream().collect(Collectors.groupingBy(ReportDataSetV2::getRelationId));
    }

}




