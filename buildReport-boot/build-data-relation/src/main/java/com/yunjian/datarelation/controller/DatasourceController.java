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
package com.yunjian.datarelation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjian.common.util.R;
import com.yunjian.datarelation.anno.LogicDeleteAware;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.common.BaseOptions.CreateGroup;
import com.yunjian.datarelation.dto.DatasourceDTO;
import com.yunjian.datarelation.dto.DatasourcePageDTO;
import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.dto.TableDTO;
import com.yunjian.datarelation.service.DatasourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yujian
 **/
@RestController
@RequestMapping("/datasource")
@RequiredArgsConstructor
public class DatasourceController {

    private final DatasourceService datasourceService;

    /**
     * 获取数据源分页
     *
     * @param pageDTO page
     * @return data
     */
    @PostMapping("/page")
    public R<Page<DatasourceDTO>> getPage(@RequestBody DatasourcePageDTO pageDTO) {
        return R.ok(datasourceService.getPage(pageDTO));
    }


    /**
     * 获取数据源分页
     *
     * @param pageDTO page
     * @return data
     */
    @PostMapping("/list")
    public R<List<DatasourceDTO>> getlist(@RequestBody DatasourcePageDTO pageDTO) {
        return R.ok(datasourceService.getList(pageDTO, false));
    }

    /**
     * 创建数据源
     *
     * @param dto dto
     * @return ok
     */
    @PostMapping("/create")
    public R<Void> create(@Validated(CreateGroup.class) @RequestBody DatasourceDTO dto) {
        datasourceService.createDatasource(dto);
        return R.ok();
    }

    /**
     * 数据源详情
     *
     * @return 详情
     */
    @GetMapping("/detail/{id}")
    public R<DatasourceDTO> detail(@PathVariable Long id) {
        return R.ok(datasourceService.detail(id));
    }

    /**
     * 更新数据源
     *
     * @param dto dto
     * @return ok
     */
    @PostMapping("/update/{id}")
    public R<Void> update(@PathVariable Long id,
                          @Validated(CreateGroup.class) @RequestBody DatasourceDTO dto) {
        datasourceService.updateDatasource(id, dto);
        return R.ok();
    }

    /**
     * 删除数据源
     *
     * @param id id
     * @return ok
     */
    @PostMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        datasourceService.deleteDatasource(id);
        return R.ok();
    }

    /**
     * 获取所有表
     *
     * @param datasourceId 数据源id
     * @return tables
     */
    @GetMapping("/getTables/{datasourceId}/{tableName}")
    public R<List<TableDTO>> getTables(@PathVariable Long datasourceId, @PathVariable(value = "tableName", required = false) String tableName) {
        return R.ok(datasourceService.getTablesByDatasourceId(datasourceId, tableName));
    }

    @GetMapping("/getTables/{datasourceId}")
    public R<List<TableDTO>> getTables(@PathVariable Long datasourceId) {
        return R.ok(datasourceService.getTablesByDatasourceId(datasourceId, ""));
    }

    /**
     * 根据业务关系id获取 缩小范围的筛选器所需表
     *
     * @param relationId 业务关系id
     * @return 缩小范围的表
     */
    @GetMapping("/getTablesByRelation/{relationId}")
    public R<List<TableDTO>> getTablesByRelation(@PathVariable Long relationId) {
        return R.ok(datasourceService.getTablesByRelationId(relationId));
    }


    /**
     * 所有字段
     *
     * @param datasourceId 数据源id
     * @param tableName    表名
     * @return 字段
     */
    @GetMapping("/getTableFields/{datasourceId}/{tableName}")
    public R<List<DbFieldDTO>> getTableFields(@PathVariable Long datasourceId,
                                              @PathVariable String tableName) {
        return R.ok(datasourceService.getTableFieldsByDatasourceId(datasourceId, tableName));
    }

    @LogicDeleteAware
    @GetMapping("/preview/{datasourceId}/{tableName}")
    public R<List<AnyData>> preview(@PathVariable Long datasourceId,
                                    @PathVariable String tableName) {
        return R.ok(datasourceService.preview(datasourceId, tableName));
    }

}
