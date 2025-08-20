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
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.datarelation.dto.BusinessDatasetDTO;
import com.yunjian.datarelation.dto.DataSetDTO;
import com.yunjian.datarelation.engine.pojo.DataFilter;
import com.yunjian.datarelation.entity.ReportDataSetV2;
import com.yunjian.datarelation.entity.ReportDatasetRelation;
import com.yunjian.datarelation.mapper.ReportDatasetRelationMapper;
import com.yunjian.datarelation.service.ReportDataSetService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author yujian
 **/
@Service
public class ReportDatasetRelationService extends
        ServiceImpl<ReportDatasetRelationMapper, ReportDatasetRelation> {
    @Autowired
    private ReportDataSetService reportDataSetService;

    public List<DataSetDTO> getDataSetByReportId(Long reportId) {

        List<ReportDatasetRelation> reportDatasetRelations = list(Wrappers.<ReportDatasetRelation>lambdaQuery()
                .eq(ReportDatasetRelation::getReportId, reportId));

        List<Long> dsId = reportDatasetRelations.stream().map(ReportDatasetRelation::getDataSetId)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(dsId)) {
            return Collections.emptyList();
        }

        List<ReportDataSetV2> reportDataSetV2s = reportDataSetService.listByIds(dsId);
        List<DataSetDTO> collect = new ArrayList<>();
        for (ReportDataSetV2 dataSet : reportDataSetV2s) {
            DataSetDTO dataSetDTO = new DataSetDTO();
            dataSetDTO.setId(dataSet.getId());
            dataSetDTO.setType(dataSet.getType());
            dataSetDTO.setDatasetName(dataSet.getName());
            dataSetDTO.setBusinessType(dataSet.getBusinessType());
            dataSetDTO.setDataFilter(
                    StringUtils.isEmpty(dataSet.getDataFilter()) ? null :
                            JSON.parseArray(dataSet.getDataFilter()).toJavaList(DataFilter.class));
            dataSetDTO.setRelationId(String.valueOf(dataSet.getRelationId()));
            dataSetDTO.setTables(
                    JSON.toJavaObject(JSON.parseArray(dataSet.getFieldJsonArray()), List.class));
            collect.add(dataSetDTO);
        }
        return collect;

    }

    @Transactional(rollbackFor = Throwable.class)
    public void removeByReportId(Long reportId) {
        remove(Wrappers.<ReportDatasetRelation>lambdaQuery()
                .eq(ReportDatasetRelation::getReportId, reportId));
    }

    public List<DataSetDTO> getDataSetByDataSetId(List<Long> dataSetId) {

        if (CollUtil.isEmpty(dataSetId)) {
            return Collections.emptyList();
        }

        List<ReportDataSetV2> reportDataSetV2s = reportDataSetService.listByIds(dataSetId);
        List<DataSetDTO> collect = new ArrayList<>();
        for (ReportDataSetV2 dataSet : reportDataSetV2s) {
            DataSetDTO dataSetDTO = new DataSetDTO();
            dataSetDTO.setId(dataSet.getId());
            dataSetDTO.setType(dataSet.getType());
            dataSetDTO.setDatasetName(dataSet.getName());
            dataSetDTO.setBusinessType(dataSet.getBusinessType());
            dataSetDTO.setDataSourceId(dataSet.getDataSourceId());
            dataSetDTO.setDataFilter(
                    StringUtils.isEmpty(dataSet.getDataFilter()) ? null :
                            JSONUtil.toList(dataSet.getDataFilter(), DataFilter.class));
            dataSetDTO.setRelationId(String.valueOf(dataSet.getRelationId()));
            List<BusinessDatasetDTO> list = JSONUtil.toList(dataSet.getFieldJsonArray(),
                    BusinessDatasetDTO.class);
            dataSetDTO.setTables(list);
            collect.add(dataSetDTO);
        }
        return collect;
    }
}
