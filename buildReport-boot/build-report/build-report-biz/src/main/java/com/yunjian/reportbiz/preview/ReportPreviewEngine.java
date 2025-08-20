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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.common.PreviewData;
import com.yunjian.datarelation.config.ReportProperties;
import com.yunjian.datarelation.dto.*;
import com.yunjian.datarelation.engine.BusinessEngine;
import com.yunjian.datarelation.engine.pojo.DataFilter;
import com.yunjian.datarelation.engine.pojo.SqlField;
import com.yunjian.datarelation.enums.DataSetBusinessType;
import com.yunjian.datarelation.service.impl.ReportDatasetRelationService;
import com.yunjian.datarelation.service.impl.ReportMainInfoService;
import com.yunjian.datarelation.utils.InterruptedUtils;
import com.yunjian.reportbiz.preview.export.Export;
import com.yunjian.reportbiz.preview.export.pdf.FontFileFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import reactor.function.Function4;

import javax.annotation.PostConstruct;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
@Component
@Slf4j
public class ReportPreviewEngine {

    /**
     * 业务关系处理引擎
     */
    @Autowired
    private BusinessEngine businessEngine;
    @Autowired
    private ReportMainInfoService reportManagerService;
    @Autowired
    private Map<String, Export> exportMap;
    @Autowired
    RenderFilter renderFilter;
    @Autowired
    ReportProperties reportProperties;
    @Autowired
    ReportDatasetRelationService reportDatasetRelationService;
    final ScheduledExecutorService exportScheduled;

    @Autowired
    StringRedisTemplate redisTemplate;

    public ReportPreviewEngine() {
        exportScheduled = Executors.newScheduledThreadPool(10, new ThreadFactory() {
            AtomicInteger incr = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("ReportExport-" + incr.incrementAndGet());
                return t;
            }
        });
    }


    public void export(AtomicBoolean flag, String type, ReportInfoDTO reportInfoDTO,
                       OutputStream outputStream)
            throws IOException, InterruptedException {
        Export export = exportMap.get(type.toLowerCase());
        if (export == null) {
            throw new BusinessException("-1", "不支持该类型的导出");
        }

        // 在开始IO操作前检查中断标志
        if (flag.get()) {
            throw new InterruptedException("导出任务被取消");
        }

        // 获取数据阶段
        if (reportInfoDTO.getReportId() != null) {
            renderFilter.fillOriginData(reportInfoDTO);
        }
        reportInfoDTO.setExport(true);
        JSONObject previewJson = previewSync(reportInfoDTO);

        // 再次检查中断标志
        if (flag.get()) {
            throw new InterruptedException("导出任务被取消");
        }

        // 准备导出数据
        JSONObject rows = previewJson.getJSONObject("tableInfo").getJSONObject("rows");
        JSONArray styles = previewJson.getJSONObject("tableInfo").getJSONArray("styles");
        JSONObject cols = previewJson.getJSONObject("tableInfo").getJSONObject("cols");
        List<ReportCellDTO> reportCells = new ArrayList<>();
        ReportRender.cellRender(rows, (row, column, cell) -> reportCells.add(cell));
        List<ReportCellDTO> sortedCells = reportCells.stream()
                .sorted(Comparator.comparing(ReportCellDTO::getRow).thenComparing(ReportCellDTO::getColumn))
                .collect(Collectors.toList());

        // 标记开始IO操作，此后将不再响应中断
        flag.set(true);

        // 执行导出
        export.export(flag, sortedCells, styles, cols, outputStream);
    }

    @PostConstruct
    public void initFont() {
        FontFileFactory.init();
    }


    public JSONObject preview(ReportInfoDTO reportInfo) {
        for (Object item : new JSONArray(reportInfo.getFilterComponents())) {
            JSONObject jsonItem = (JSONObject) item;
            JSONObject byPath = jsonItem.getByPath("component.options", JSONObject.class);
            String dataset = byPath.getStr("dataset");
            if (StrUtil.isNotBlank(dataset)) {
                reportInfo.getDataSetId().add(Long.valueOf(dataset));
            }
        }
        Map<String, List<String>> data = new HashMap<>();
        // 过滤设计器实际拖拽了哪些字段
        ReportRender.cellRender(JSONUtil.parseObj(reportInfo.getComponentJson()).getJSONObject("rows"),
                (row, column, cell) -> {
                    String dataset = cell.getDataset();
                    String key = cell.getKey();
                    data.computeIfAbsent(dataset, k -> new ArrayList<>())
                            .add(key);
                });


        // 1.数据集解析
        List<DataSetDTO> dataset = reportDatasetRelationService.getDataSetByDataSetId(reportInfo.getDataSetId());
        List<DataSetDTO> newDataSet = new ArrayList<>();
        for (DataSetDTO dataSetDTO : dataset) {
            if (Objects.nonNull(dataSetDTO.getBusinessType()) && DataSetBusinessType.getEnumByCode(dataSetDTO.getBusinessType()) == DataSetBusinessType.FILTER) {
                newDataSet.add(dataSetDTO);
                continue;
            }
            if (data.containsKey(dataSetDTO.getDatasetName())) {
                List<String> keys = data.get(dataSetDTO.getDatasetName());

                for (BusinessDatasetDTO table : dataSetDTO.getTables()) {
                    List<SqlField> fields = new ArrayList<>();
                    for (SqlField sqlField : table.getSqlFieldList()) {
                        String key = table.getAliasName() + "_" + sqlField.getName();
                        if (keys.contains(key)) {
                            fields.add(sqlField);
                        }
                    }
                    table.setSqlFieldList(fields);
                }
                newDataSet.add(dataSetDTO);
            }

        }

        dataset = newDataSet;


        // 2.如果dataFilterComponent数据有值 则替换掉默认值
        renderFilter.fillDataSetFilter(reportInfo, dataset);
        // 4. 数据集查询
        Map<String, PreviewData> datasetNameMap = new HashMap<>();
        for (DataSetDTO dataSetDTO : dataset) {
            InterruptedUtils.checkInterrupted();
            if (dataSetDTO.getTables().size() == 1 && dataSetDTO.getBusinessType() == 1) {
                // 单表
                List<AnyData> value = getDatasetBySingleTable(
                        dataSetDTO.getDataSourceId(),
                        CollUtil.getFirst(dataSetDTO.getTables()), reportInfo, dataSetDTO.getDataFilter());
                datasetNameMap.put(dataSetDTO.getDatasetName(), new PreviewData(value, value.size()));
            } else {
                PreviewData value = getDataset(Long.valueOf(dataSetDTO.getRelationId()),
                        dataSetDTO.getTables(), reportInfo, dataSetDTO.getDataFilter());
                datasetNameMap.put(dataSetDTO.getDatasetName(), value);
            }
        }
        InterruptedUtils.checkInterrupted();
        // 5.报表结构解析
        JSONObject render = new ReportRender(reportInfo.getComponentJson(),
                reportInfo.getFilterComponents(), reportInfo.getPageSize(), datasetNameMap).render();
        InterruptedUtils.checkInterrupted();
        // 6. 报表查询条件，数据集解析
        JSONArray dataFilter = renderFilter.dataFilter(reportInfo, dataset, getDatasetBySingleTableFun,
                getDatasetFun);
        render.set("filterComponents", JSONUtil.toJsonStr(dataFilter));

        return render;

    }

    public JSONObject previewSync(ReportInfoDTO reportInfo) {
        for (Object item : new JSONArray(reportInfo.getFilterComponents())) {
            JSONObject jsonItem = (JSONObject) item;
            JSONObject byPath = jsonItem.getByPath("component.options", JSONObject.class);
            String dataset = byPath.getStr("dataset");
            if (StrUtil.isNotBlank(dataset)) {
                reportInfo.getDataSetId().add(Long.valueOf(dataset));
            }
        }
        Map<String, List<String>> data = new HashMap<>();
        // 过滤设计器实际拖拽了哪些字段
        ReportRender.cellRender(JSONUtil.parseObj(reportInfo.getComponentJson()).getJSONObject("rows"),
                (row, column, cell) -> {
                    String dataset = cell.getDataset();
                    String key = cell.getKey();
                    data.computeIfAbsent(dataset, k -> new ArrayList<>())
                            .add(key);
                });


        // 1.数据集解析
        List<DataSetDTO> dataset = reportDatasetRelationService.getDataSetByDataSetId(reportInfo.getDataSetId());
        List<DataSetDTO> newDataSet = new ArrayList<>();
        for (DataSetDTO dataSetDTO : dataset) {
            if (Objects.nonNull(dataSetDTO.getBusinessType()) && DataSetBusinessType.getEnumByCode(dataSetDTO.getBusinessType()) == DataSetBusinessType.FILTER) {
                newDataSet.add(dataSetDTO);
                continue;
            }
            if (data.containsKey(dataSetDTO.getDatasetName())) {
                List<String> keys = data.get(dataSetDTO.getDatasetName());

                for (BusinessDatasetDTO table : dataSetDTO.getTables()) {
                    List<SqlField> fields = new ArrayList<>();
                    for (SqlField sqlField : table.getSqlFieldList()) {
                        String key = table.getAliasName() + "_" + sqlField.getName();
                        if (keys.contains(key)) {
                            fields.add(sqlField);
                        }
                    }
                    table.setSqlFieldList(fields);
                }
                newDataSet.add(dataSetDTO);
            }

        }

        dataset = newDataSet;


        // 2.如果dataFilterComponent数据有值 则替换掉默认值
        renderFilter.fillDataSetFilter(reportInfo, dataset);
        // 4. 数据集查询
        Map<String, PreviewData> datasetNameMap = new HashMap<>();
        for (DataSetDTO dataSetDTO : dataset) {
            InterruptedUtils.checkInterrupted();
            if (dataSetDTO.getTables().size() == 1 && dataSetDTO.getBusinessType() == 1) {
                // 单表
                List<AnyData> value = getDatasetBySingleTable(
                        dataSetDTO.getDataSourceId(),
                        CollUtil.getFirst(dataSetDTO.getTables()), reportInfo, dataSetDTO.getDataFilter());
                datasetNameMap.put(dataSetDTO.getDatasetName(), new PreviewData(value, value.size()));
            } else {
                PreviewData value = getDataset(Long.valueOf(dataSetDTO.getRelationId()),
                        dataSetDTO.getTables(), reportInfo, dataSetDTO.getDataFilter());
                datasetNameMap.put(dataSetDTO.getDatasetName(), value);
            }
        }
        InterruptedUtils.checkInterrupted();
        // 5.报表结构解析
        JSONObject render = new ReportRender(reportInfo.getComponentJson(),
                reportInfo.getFilterComponents(), reportInfo.getPageSize(), datasetNameMap).render();
        InterruptedUtils.checkInterrupted();
        // 6. 报表查询条件，数据集解析
        JSONArray dataFilter = renderFilter.dataFilter(reportInfo, dataset, getDatasetBySingleTableFun,
                getDatasetFun);
        render.set("filterComponents", JSONUtil.toJsonStr(dataFilter));
//    PreviewData max = datasetNameMap.values().stream()
//        .max(Comparator.comparing(PreviewData::getCount))
//        .orElse(null);
//    render.set("total",max == null ? 0 :  max.getCount());

        return render;

    }


    public PreviewData getDataset(Long relationId, List<BusinessDatasetDTO> selectedFieldList,
                                  ReportInfoDTO reportInfoDTO, List<DataFilter> dataFilters) {
        return businessEngine.run(relationId, selectedFieldList, dataFilters, reportInfoDTO);
    }

    public List<AnyData> getDatasetBySingleTable(Long sourceId, BusinessDatasetDTO selectedField,
                                                 ReportInfoDTO reportInfoDTO, List<DataFilter> dataFilters) {
        return businessEngine.runSingleTable(sourceId, selectedField, reportInfoDTO, dataFilters);
    }

    Function4<Long, BusinessDatasetDTO, ReportInfoDTO, List<DataFilter>, List<AnyData>>
            getDatasetBySingleTableFun = this::getDatasetBySingleTable;

    Function4<Long, List<BusinessDatasetDTO>, ReportInfoDTO, List<DataFilter>, PreviewData>
            getDatasetFun = this::getDataset;

    public void addExportTask(AsyncContext asyncContext, String type, ReportInfoDTO reportInfoDTO) {
        asyncContext.setTimeout(0);
        final String taskId = IdUtil.fastUUID();

        AtomicBoolean ioStarted = new AtomicBoolean(false);
        Future<?> submit = exportScheduled.submit(
                new ExportTask(asyncContext, type, reportInfoDTO, ioStarted, taskId));

        long timeout = Math.max(reportProperties.getExport().getExportTimeOut(), 10L);

        exportScheduled.schedule(() -> {
            if (!submit.isDone() && !ioStarted.get()) {
                log.info("超时尝试中断导出线程，taskId: {}", taskId);
                HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                submit.cancel(true);
                try {
                    if (!response.isCommitted()) {
                        response.reset();
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
                        response.getWriter().write("{\"msg\":\"导出超时\",\"code\":408}");
                    }
                } catch (Exception e) {
                    log.error("处理超时响应失败 taskId={}", taskId, e);
                } finally {
                    asyncContext.complete();
                }
            }
        }, timeout, TimeUnit.SECONDS);

    }

    @Data
    @AllArgsConstructor
    public static class FileDownloadInfo {
        private String fileName;
        private String fileUrl;
    }

    @RequiredArgsConstructor
    @Data
    class ExportTask implements Runnable {

        final AsyncContext asyncContext;
        final String type;
        final ReportInfoDTO reportInfoDTO;
        final AtomicBoolean flag;
        final String taskId;

        @Override
        public void run() {
            StopWatch stopWatch = new StopWatch(taskId);
            stopWatch.start();
            HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();

            try {
                // 检查响应是否已经开始
                if (response.isCommitted()) {
                    log.warn("响应已经开始，取消导出任务 taskId={}", taskId);
                    return;
                }

                // 先设置响应头
                setupResponse(response, type);

                // 执行导出
                export(flag, type, reportInfoDTO, response.getOutputStream());

                // 完成导出
                stopWatch.stop();
                log.info("导出完成 {} taskId={} 耗时: {}ms", type, taskId, stopWatch.getTotalTimeMillis());

            } catch (Throwable e) {
                // 捕获所有异常，包括Error
                log.error("导出过程发生错误 taskId={}", taskId, e);
                handleExportError(response, e);
            } finally {
                // 确保异步上下文被完成
                try {
                    if (!asyncContext.getResponse().isCommitted()) {
                        asyncContext.complete();
                    }
                } catch (Exception e) {
                    log.error("完成异步上下文失败 taskId={}", taskId, e);
                }
            }
        }

        private void setupResponse(HttpServletResponse response, String type) throws IOException {
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream; charset=UTF-8");
            final String downFileName = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
                    + getFileSuffix(type);
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(downFileName, "UTF-8"));
        }

        private void handleExportError(HttpServletResponse response, Throwable e) {
            try {
                if (!response.isCommitted()) {
                    response.reset();
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json;charset=UTF-8");

                    String errorMsg;
                    if (e instanceof InterruptedException) {
                        response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
                        errorMsg = "{\"msg\":\"导出任务被取消\",\"code\":408}";
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        errorMsg = "{\"msg\":\"导出失败\",\"code\":500}";
                    }

                    response.getWriter().write(errorMsg);
                }
            } catch (IOException ex) {
                log.error("写入错误响应失败 taskId={}", taskId, ex);
            }
        }
    }

    public static String getFileSuffix(String type) {
        if ("excel".equals(type)) {
            return ".xlsx";
        }
        if ("word".equals(type)) {
            return ".docx";
        }
        if ("pdf".equals(type)) {
            return ".pdf";
        }
        return "unknow";
    }
}
