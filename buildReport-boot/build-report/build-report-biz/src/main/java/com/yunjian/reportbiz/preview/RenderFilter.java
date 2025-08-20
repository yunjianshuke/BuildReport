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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Functions;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.common.PreviewData;
import com.yunjian.datarelation.dto.BusinessDatasetDTO;
import com.yunjian.datarelation.dto.ConditionDTO;
import com.yunjian.datarelation.dto.DataSetDTO;
import com.yunjian.datarelation.dto.ReportInfoDTO;
import com.yunjian.datarelation.engine.pojo.DataFilter;
import com.yunjian.datarelation.engine.pojo.SqlField;
import com.yunjian.datarelation.entity.ReportMainInfo;
import com.yunjian.datarelation.enums.CellComparatorEnum;
import com.yunjian.datarelation.service.ReportDataSetService;
import com.yunjian.datarelation.service.impl.ReportDatasetRelationService;
import com.yunjian.datarelation.service.impl.ReportMainInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.function.Function4;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class RenderFilter {

    private final ReportDataSetService reportDataSetService;
    private final ReportDatasetRelationService reportDatasetRelationService;
    private final ReportMainInfoService reportMainInfoService;

    public void fillOriginData(ReportInfoDTO reportInfoDTO) {
        Long reportId = reportInfoDTO.getReportId();
        ReportMainInfo info = reportMainInfoService.getById(reportId);
        if (ObjectUtil.isNull(info)) {
            throw new BusinessException("-1", "报表不存在");
        }
        BeanUtil.copyProperties(info, reportInfoDTO, "filterComponents");
        // 如果reportInfoDTO 没有传入filterComponents 则使用数据库中的filterComponents
        if (StrUtil.isBlank(reportInfoDTO.getFilterComponents())) {
            reportInfoDTO.setFilterComponents(info.getFilterComponents());
        }
        //如果是下钻，则需要将下钻的过滤条件携带
        Map<String, List<ConditionDTO>> drillDownMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(reportInfoDTO.getCondition())) {
            drillDownMap = reportInfoDTO.getCondition().stream()
                    .collect(Collectors.groupingBy(ConditionDTO::getTargetDatasetName));
        }
        List<DataSetDTO> relationByReportId = reportDatasetRelationService.getDataSetByReportId(
                reportId);
        List<Long> dataSetIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(relationByReportId)) {
            List<DataSetDTO> dataSetDTOList = new ArrayList<>();
            for (DataSetDTO dataSet : relationByReportId) {
                dataSetIds.add(dataSet.getId());
                // 如果是下钻带来的数据集，需要将数据集的过滤条件带过去
                if (drillDownMap.containsKey(dataSet.getDatasetName())
                        && ObjectUtil.isNotEmpty(dataSet.getDataFilter())) {
                    List<DataFilter> dataFilterList = dataSet.getDataFilter();
                    List<ConditionDTO> conditionDTOS = drillDownMap.get(dataSet.getDatasetName());
                    for (ConditionDTO conditionDTO : conditionDTOS) {
                        for (DataFilter dataFilter : dataFilterList) {
                            if (StrUtil.equals(dataFilter.getKey(), conditionDTO.getTargetField())) {
                                dataFilter.setDefaultValue(conditionDTO.getSourceValue());
                            }
                        }
                    }
                }
                dataSetDTOList.add(dataSet);
            }
            reportInfoDTO.setDataset(dataSetDTOList);
            reportInfoDTO.setDataSetId(dataSetIds);
        }
    }

    //如果dataFilterComponent数据有值 则替换掉默认值
    public void fillDataSetFilter(ReportInfoDTO reportInfo, List<DataSetDTO> dataset) {
        Map<String, Object> dataFilterComponentMap = new HashMap<>();
        if (StringUtils.isNotBlank(reportInfo.getFilterComponents())) {
            JSONArray dataFilterComponentArray = JSONUtil.parseArray(reportInfo.getFilterComponents());
            for (Object item : dataFilterComponentArray) {
                JSONObject jsonItem = (JSONObject) item;
                JSONArray fields = jsonItem.getJSONArray("fields");

                if (ObjectUtil.isAllNotEmpty(jsonItem.get("component"),
                        jsonItem.getByPath("component.value"))) {

                    for (Object field : fields) {
                        JSONObject fieldObject = (JSONObject) field;

                        boolean shouldSkipDateTimeBetweenField = shouldSkipDateTimeBetweenField(fieldObject, jsonItem);
                        if (shouldSkipDateTimeBetweenField) {
                            continue;
                        }

                        dataFilterComponentMap.put(
                                fieldObject.getStr("datasetName") + "." + fieldObject.getStr("name"),
                                jsonItem.getByPath("component.value"));
                    }


                }
            }
        }
        Map<Long, DataSetDTO> collect = Collections.emptyMap();
        if (reportInfo.getDataset() != null) {
            collect = reportInfo.getDataset().stream()
                    .collect(Collectors.toMap(DataSetDTO::getId, Functions.identity()));
        }


        for (DataSetDTO dataSetDTO : dataset) {
            // 下钻传值bug在这修复
            DataSetDTO dataSetDTO1 = collect.get(dataSetDTO.getId());
            if (dataSetDTO1 != null) {
                dataSetDTO.setDataFilter(dataSetDTO1.getDataFilter());
            }

            if (dataSetDTO.getBusinessType() == 1) {
                continue;
            }


            List<DataFilter> dataFilters = dataSetDTO.getDataFilter();
            for (DataFilter dataFilter : dataFilters) {
                if (dataFilterComponentMap.containsKey(
                        dataSetDTO.getDatasetName() + "." + dataFilter.getName())) {
                    dataFilter.setDefaultValue(
                            dataFilterComponentMap.get(dataSetDTO.getDatasetName() + "." + dataFilter.getName()));
                }
            }
        }
    }

    private static boolean shouldSkipDateTimeBetweenField(JSONObject fieldObject, JSONObject jsonItem) {
        try {
            if (Objects.isNull(fieldObject)) {
                return false;
            }
            String type = fieldObject.getStr("type");
            String condition = fieldObject.getStr("condition");

            Set<String> dateTimeTypes = CollUtil.newHashSet("DATETIME", "DATE", "TIME");
            if (dateTimeTypes.contains(type) && CellComparatorEnum.BETWEEN.name().equals(condition)) {
                Object componentValue = jsonItem.getByPath("component.value");
                if (Objects.nonNull(componentValue) && componentValue instanceof JSONArray) {
                    JSONArray componentValueArr = (JSONArray)jsonItem.getByPath("component.value");
                    if (componentValueArr == null || componentValueArr.size() != 2) {
                        return false;
                    }
                    String v1 = (String)componentValueArr.get(0);
                    String v2 = (String)componentValueArr.get(1);
                    return StrUtil.isBlank(v1) && StrUtil.isBlank(v2);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return false;
    }

    public JSONArray dataFilter(ReportInfoDTO reportInfo, List<DataSetDTO> dataset,
                                Function4<Long, BusinessDatasetDTO, ReportInfoDTO, List<DataFilter>, List<AnyData>> getDatasetBySingleTableFun,
                                Function4<Long, List<BusinessDatasetDTO>, ReportInfoDTO, List<DataFilter>, PreviewData> getDatasetFun) {
        JSONArray dataFilter = new JSONArray(reportInfo.getFilterComponents());

        for (Object item : dataFilter) {

            JSONObject jsonItem = (JSONObject) item;
            JSONObject byPath = jsonItem.getByPath("component.options", JSONObject.class);
            // 只有这几种类型，并且type为数据集的组件才需要查询数据
            if (!Arrays.asList("select", "radio", "checkbox")
                    .contains(jsonItem.getByPath("component.type", String.class)) || !"dataset".equals(
                    byPath.getStr("type"))) {
                continue;
            }
            //根据dataset名称获取数据集信息
            DataSetDTO datasetMatch = dataset.stream()
                    .filter(datasetItem -> String.valueOf(datasetItem.getId()).equals(byPath.getStr("dataset")))
                    .findAny().orElse(null);
            if (ObjectUtil.isEmpty(datasetMatch)) {
                log.warn("数据源未匹配到，{}", byPath.getStr("dataset"));
                continue;
            }

            String labelAliasName = byPath.getStr("labelAliasName");
            String valueAliasName = byPath.getStr("valueAliasName");
            String labelName = byPath.getStr("labelName");
            String valueName = byPath.getStr("valueName");
            String label = byPath.getStr("label");
            String value = byPath.getStr("value");
            String labelCellId = byPath.getStr("labelCellId");
            String valueCellId = byPath.getStr("valueCellId");

            //判断是自定义表还是业务关系中的表
            if (datasetMatch.getType() == 1) {
                //自定义表
                //循环dataFilter 添加到对应字段中

                List<DataFilter> filterList = datasetMatch.getDataFilter();
                List<AnyData> singleTable = getDatasetBySingleTableFun.apply(
                        datasetMatch.getDataSourceId(), datasetMatch.getTables().get(0),
                        reportInfo, filterList);
                if (CollectionUtil.isNotEmpty(singleTable)) {
                    //过滤anydata,对label和value进行去重
                    Map<Object, List<Object>> map = new HashMap<>();
                    List<AnyData> resultAndData = singleTable.stream()
                            .filter(anyDataItem -> distinctLabelAndValue(anyDataItem, labelName, valueName, map))
                            .collect(Collectors.toList());
                    if (CollectionUtil.isNotEmpty(resultAndData)) {
                        JSONArray dataArray = new JSONArray();
                        resultAndData.forEach(data -> {
                            JSONObject dataJson = new JSONObject();
                            dataJson.put("label", data.get(labelName));
                            dataJson.put("value", data.get(valueName));
                            dataArray.add(dataJson);
                        });
                        jsonItem.putByPath("component.options.data", dataArray);
                    }
                }

            } else {

                //getDataset 所需参数，relationId, selectedFieldList, reportInfoDTO, dataFilters
                List<SqlField> sqlFieldList = new ArrayList<>();
                List<BusinessDatasetDTO> tables = new ArrayList<>();

                //如果label和value的值不一樣，则说明是新的table
                BusinessDatasetDTO businessDatasetDTO = new BusinessDatasetDTO();
                businessDatasetDTO.setTableName(labelAliasName);
                businessDatasetDTO.setCellId(labelCellId);
                SqlField sqlField = new SqlField();
                sqlField.setName(labelName);
                sqlField.setAlias(label);
                sqlFieldList.add(sqlField);
                businessDatasetDTO.setSqlFieldList(sqlFieldList);
                tables.add(businessDatasetDTO);
                if (labelAliasName.equals(valueAliasName)) {
                    if (!label.equals(value)) {
                        SqlField valueSqlField = new SqlField();
                        valueSqlField.setAlias(value);
                        valueSqlField.setName(valueName);
                        businessDatasetDTO.setTableName(valueAliasName);
                        sqlFieldList.add(valueSqlField);
                    }

                } else {
                    List<SqlField> sqlFields = new ArrayList<>();
                    BusinessDatasetDTO valueBusinessDatasetDTO = new BusinessDatasetDTO();
                    valueBusinessDatasetDTO.setTableName(labelAliasName);
                    valueBusinessDatasetDTO.setCellId(valueCellId);
                    SqlField valueSqlField = new SqlField();
                    valueSqlField.setAlias(value);
                    valueSqlField.setName(valueName);
                    sqlFields.add(valueSqlField);
                    valueBusinessDatasetDTO.setSqlFieldList(sqlFields);
                    tables.add(valueBusinessDatasetDTO);
                }
                ReportInfoDTO reportInfoDTO = new ReportInfoDTO();
                reportInfoDTO.setPageSize(Integer.MAX_VALUE);
                reportInfoDTO.setPageNumber(0);
                PreviewData anyData = getDatasetFun.apply(byPath.getLong("relationId"), tables,
                        reportInfoDTO,
                        JSONUtil.toList(reportInfo.getFilterComponents(), DataFilter.class));
                if (anyData != null && CollectionUtil.isNotEmpty(anyData.getData())) {
                    //过滤anydata,对label和value进行去重
                    Map<Object, List<Object>> map = new HashMap<>();
                    List<AnyData> resultAndData = anyData.getData().stream()
                            .filter(anyDataItem -> distinctLabelAndValue(anyDataItem, label, value, map))
                            .collect(Collectors.toList());
                    if (CollectionUtil.isNotEmpty(resultAndData)) {
                        JSONArray dataArray = new JSONArray();
                        resultAndData.forEach(data -> {
                            JSONObject dataJson = new JSONObject();
                            dataJson.put("label", data.get(label));
                            dataJson.put("value", data.get(value));
                            dataArray.add(dataJson);
                        });
                        jsonItem.putByPath("component.options.data", dataArray);
                    }
                }
            }
        }
        return dataFilter;
    }

    private static boolean distinctLabelAndValue(AnyData anyDataItem, String label, String value,
                                                 Map<Object, List<Object>> map) {
        Object itemLabel = anyDataItem.get(label);
        Object itemValue = anyDataItem.get(value);
        if (ObjectUtil.isAllEmpty(itemLabel, itemValue)) {
            return false;
        }
        if (map.containsKey(itemLabel)) {
            if (map.get(itemLabel).contains(itemValue)) {
                return false;
            } else {
                map.get(itemLabel).add(itemValue);
                return true;
            }
        } else {
            List<Object> itemArray = new ArrayList<>();
            itemArray.add(itemValue);
            map.put(itemLabel, itemArray);
            return true;
        }
    }
}
