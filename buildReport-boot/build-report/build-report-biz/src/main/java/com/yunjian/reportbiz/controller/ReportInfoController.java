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
package com.yunjian.reportbiz.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.SqlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunjian.common.util.R;
import com.yunjian.datarelation.anno.LogicDeleteAware;
import com.yunjian.datarelation.config.ReportProperties;
import com.yunjian.datarelation.dto.DataSetDTO;
import com.yunjian.datarelation.dto.ReportDataSetDTO;
import com.yunjian.datarelation.dto.ReportInfoDTO;
import com.yunjian.datarelation.dto.ReportMainInfoPageDTO;
import com.yunjian.datarelation.entity.ReportDataSetV2;
import com.yunjian.datarelation.entity.ReportMainInfo;
import com.yunjian.datarelation.service.ReportDataSetService;
import com.yunjian.datarelation.service.impl.ReportMainInfoService;
import com.yunjian.datarelation.vo.ReportInfoVO;
import com.yunjian.datarelation.vo.ReportMainInfoVO;
import com.yunjian.reportbiz.preview.RenderFilter;
import com.yunjian.reportbiz.preview.ReportPreviewEngine;
import com.yunjian.reportbiz.util.SseUtils;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * 报表信息
 * Created on 2024/7/3 上午11:39
 *
 * @author fusheng
 * @version 1.0
 */
@RestController
@RequestMapping("/reportInfo")
@Slf4j
public class ReportInfoController {

    @Resource
    private ReportMainInfoService reportMainInfoService;
    @Resource
    private ReportPreviewEngine reportPreviewEngine;
    @Resource
    private ReportDataSetService reportDataSetService;
    @Resource
    RenderFilter renderFilter;
    @Autowired
    ReportProperties reportProperties;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 保存报表信息
     *
     * @param reportInfoDTO 报表信息
     * @author fusheng
     * @date: 2024/7/3 上午11:44
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody ReportInfoDTO reportInfoDTO) {
        return R.ok(reportMainInfoService.saveInfo(reportInfoDTO));
    }

    /**
     * 保存数据集
     */
    @PostMapping("/saveDataSet")
    public R<Long> saveDataSet(@RequestBody DataSetDTO dataSetDTO) {
        reportDataSetService.saveDataSet(dataSetDTO);
        return R.ok();
    }

    @PostMapping("/deleteDataSet/{dataSetId}")
    public R<Long> saveDataSet(@PathVariable("dataSetId") Long dataSetId) {
        reportDataSetService.removeById(dataSetId);
        return R.ok();
    }

    /**
     * 获取数据集
     */
    @PostMapping("/getDataSetPage")
    public R<Page<ReportDataSetV2>> getDateSet(@RequestBody ReportDataSetDTO reportDataSet) {
        return R.ok(reportDataSetService.page(reportDataSet, Wrappers.<ReportDataSetV2>lambdaQuery()
                .isNotNull(reportDataSet.getSelect() != null && reportDataSet.getSelect() == 1 && reportDataSet.getBusinessType() == 1, ReportDataSetV2::getFieldJsonArray)
                .eq(ReportDataSetV2::getRelationId, reportDataSet.getRelationId())
                .eq(ObjectUtil.isNotEmpty(reportDataSet.getBusinessType())
                        , ReportDataSetV2::getBusinessType, reportDataSet.getBusinessType())));
    }

    /**
     * 获取数据集
     */
    @PostMapping("/getDateSet")
    public R<List<ReportDataSetV2>> getDateSetList(@RequestBody ReportDataSetDTO reportDataSet) {
        Long reportId = reportDataSet.getReportId();
        Set<String> datasetNames = new HashSet<>();
        if (Objects.nonNull(reportId)) {
            ReportMainInfo reportMainInfo = reportMainInfoService.getById(reportId);
            String componentJson = reportMainInfo.getComponentJson();
            datasetNames = extractDatasets(componentJson);
        }
        return R.ok(reportDataSetService.list(Wrappers.<ReportDataSetV2>lambdaQuery()
                .in(CollUtil.isNotEmpty(datasetNames), ReportDataSetV2::getName, datasetNames)
                .eq(ObjectUtil.isNotEmpty(reportDataSet.getBusinessType())
                        , ReportDataSetV2::getBusinessType, reportDataSet.getBusinessType())));
    }

    /**
     * 从ComponentJson中提取去重的dataset名称
     * @param componentJson 组件JSON字符串
     * @return 去重后的dataset名称集合
     */
    public Set<String> extractDatasets(String componentJson) {
        Set<String> datasets = new HashSet<>();
        JSONObject jsonObject = JSONUtil.parseObj(componentJson);
        JSONObject rows = jsonObject.getJSONObject("rows");
        if (rows == null) {
            return datasets;
        }
        for (Object rowObj : rows.values()) {
            if (!(rowObj instanceof JSONObject)) {
                continue;
            }
            JSONObject row = (JSONObject) rowObj;
            JSONObject cells = row.getJSONObject("cells");
            if (cells == null) {
                continue;
            }
            for (Object cellObj : cells.values()) {
                if (!(cellObj instanceof JSONObject)) {
                    continue;
                }
                JSONObject cell = (JSONObject) cellObj;
                String dataset = cell.getStr("dataset");
                if (StrUtil.isNotBlank(dataset)) {
                    datasets.add(dataset);
                }
            }
        }
        return datasets;
    }

    /**
     * 报表另存为
     *
     * @param reportInfoDTO 报表
     * @author yujian
     * @date: 2024/11/4 下午1:58
     */
    @PostMapping("/saveAs")
    public R<Long> saveAs(@RequestBody ReportInfoDTO reportInfoDTO) {
        return R.ok(reportMainInfoService.saveAsInfo(reportInfoDTO));
    }

    /**
     * 保存报表分组
     *
     * @param reportInfoDTO 业务分组 {@link ReportMainInfo}
     * @author fusheng
     * @date: 2024/7/8 下午2:46
     */
    @PostMapping("/saveGroup")
    public R<Boolean> saveGroup(@RequestBody ReportMainInfo reportInfoDTO) {

        reportMainInfoService.updateGroupAndName(reportInfoDTO);

        return R.ok();
    }

    /**
     * 预览报表
     *
     * @param reportInfoDTO 报表信息
     * @date: 2024/7/8 下午2:48
     */
    @LogicDeleteAware
    @PostMapping("/preview")
    public R<JSONObject> preview(@RequestBody ReportInfoDTO reportInfoDTO) {
        reportInfoDTO.setPageSize(reportProperties.getExport().getDesignLimitSize());
        return R.ok(reportPreviewEngine.preview(reportInfoDTO));
    }

    @LogicDeleteAware
    @PostMapping(value = "/preview/sync", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> previewSync(@RequestBody ReportInfoDTO reportInfoDTO) {
        reportInfoDTO.setPageSize(reportProperties.getExport().getDesignLimitSize());
        return ResponseEntity.ok(
                SseUtils.runAsyncWithHeartbeat(() -> {
                    return objectMapper.writeValueAsString(R.ok(reportPreviewEngine.previewSync(reportInfoDTO)));
                }, 300_000L)
        );
    }

    @LogicDeleteAware
    @PostMapping("/download/{type}")
    public void download(@PathVariable("type") String type,
                            @RequestBody ReportInfoDTO reportInfoDTO, HttpServletRequest request) {
        String taskId = UUID.randomUUID().toString();
        log.info("开始导出 {} 名称={}, taskId={}", type, reportInfoDTO.getName(), taskId);
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

        final AsyncContext asyncContext = request.startAsync();
        // 设置请求属性，用于标识该异步请求
        reportPreviewEngine.addExportTask(asyncContext, type, reportInfoDTO);
    }

    /**
     * 列表简单预览
     *
     * @param reportId 报表id {@link Long}
     * @author fusheng
     * @date 2024/7/11 上午10:00
     */
    @LogicDeleteAware
    @PostMapping("/preview/{reportId}")
    public R<JSONObject> preview(@PathVariable Long reportId,
                                 @RequestBody ReportInfoDTO reportInfoDTO) {
        reportInfoDTO.setReportId(reportId);
        renderFilter.fillOriginData(reportInfoDTO);
        return R.ok(reportPreviewEngine.preview(reportInfoDTO));
    }

    /**
     * 同步预览（实际使用报表场景）
     */
    @LogicDeleteAware
    @PostMapping(value = "/preview/actual/sync/{reportId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> previewDesignSync(@PathVariable Long reportId,
                                                        @RequestBody ReportInfoDTO reportInfoDTO) {
        reportInfoDTO.setPageSize(reportProperties.getExport().getActualLimitSize());
        return ResponseEntity.ok(
                SseUtils.runAsyncWithHeartbeat(() -> {
                    reportInfoDTO.setReportId(reportId);
                    renderFilter.fillOriginData(reportInfoDTO);
                    return objectMapper.writeValueAsString(R.ok(reportPreviewEngine.previewSync(reportInfoDTO)));
                }, 300_000L)
        );
    }

    /**
     * 同步预览
     */
    @LogicDeleteAware
    @PostMapping(value = "/preview/sync/{reportId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> previewSync(@PathVariable Long reportId,
                                                  @RequestBody ReportInfoDTO reportInfoDTO) {
        reportInfoDTO.setPageSize(reportProperties.getExport().getDesignLimitSize());
        return ResponseEntity.ok(
                SseUtils.runAsyncWithHeartbeat(() -> {
                    reportInfoDTO.setReportId(reportId);
                    renderFilter.fillOriginData(reportInfoDTO);
                    return objectMapper.writeValueAsString(R.ok(reportPreviewEngine.previewSync(reportInfoDTO)));
                }, 300_000L)
        );
    }


    /**
     * 获取报表信息
     *
     * @param reportId 报表id
     * @author fusheng
     * @date: 2024/7/3 下午3:23
     */
    @PostMapping("/info/{reportId}")
    public R<ReportInfoVO> info(@PathVariable Long reportId) {
        return R.ok(reportMainInfoService.getInfo(reportId));
    }

    /**
     * 分页查询报表信息
     *
     * @param reportMainInfoPageDTO 报表信息
     * @author fusheng
     * @date: 2024/7/3 上午11:45
     */
    @PostMapping("/page")
    public R<Page<ReportMainInfoVO>> page(@RequestBody ReportMainInfoPageDTO reportMainInfoPageDTO) {
        return R.ok(reportMainInfoService.getPage(
                reportMainInfoPageDTO));
    }

    /**
     * 根据权限获取报表列表
     *
     * @author fusheng
     * @date 2024/8/20 下午3:06
     */
    @PostMapping("/listByAuth")
    public R<List<ReportMainInfoVO>> listByAuth() {
        return R.ok(reportMainInfoService.listByAuth(null));
    }

    /**
     * 根据权限获取报表列表（给dream端使用）
     *
     * @author yujian
     * @date 2024/11/6 下午2:06
     */
    @PostMapping("/auth/page")
    public R<Page<ReportMainInfoVO>> listByAuth(@RequestBody ReportMainInfoPageDTO reportMainInfoPageDTO) {
        return R.ok(reportMainInfoService.listByAuthPage(reportMainInfoPageDTO));
    }


    /**
     * 删除报表信息
     *
     * @param reportId 报表id
     * @author fusheng
     * @date: 2024/7/5 下午4:35
     */
    @PostMapping("/delete/{reportId}")
    public R<Void> delete(@PathVariable Long reportId) {
        reportMainInfoService.delete(reportId);
        return R.ok();
    }

}
