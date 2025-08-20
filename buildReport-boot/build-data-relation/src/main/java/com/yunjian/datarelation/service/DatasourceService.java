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
package com.yunjian.datarelation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.dto.DatasourceDTO;
import com.yunjian.datarelation.dto.DatasourcePageDTO;
import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.dto.TableDTO;
import com.yunjian.datarelation.entity.Datasource;

import java.util.List;

/**
 * @author yujian
 */
public interface DatasourceService extends IService<Datasource> {

    /**
     * 分页查询
     *
     * @param pageDTO 分页参数
     * @return dto
     */
    Page<DatasourceDTO> getPage(DatasourcePageDTO pageDTO);

    /**
     * 创建数据源
     *
     * @param dto dto
     */
    void createDatasource(DatasourceDTO dto);

    /**
     * 更新数据源
     *
     * @param dto dto
     * @param id  id
     */
    void updateDatasource(Long id, DatasourceDTO dto);

    /**
     * 数据源详情
     *
     * @param id id
     * @return dto
     */
    DatasourceDTO detail(Long id);

    /**
     * 删除数据源
     *
     * @param id id
     */
    void deleteDatasource(Long id);

    /**
     * 获取所有表
     *
     * @param datasourceId 数据源id
     * @return tables
     */
    List<TableDTO> getTablesByDatasourceId(Long datasourceId, String tableName);

    /**
     * 获取表字段
     *
     * @param datasourceId 数据源id
     * @param tableName    表名称
     * @return fields
     */
    List<DbFieldDTO> getTableFieldsByDatasourceId(Long datasourceId, String tableName);

    /**
     * 不分页的列表
     *
     * @param pageDTO dto
     * @return list
     */
    List<DatasourceDTO> getList(DatasourcePageDTO pageDTO, boolean back);

    /**
     * 预览数据源中的数据默认就100条
     *
     * @param datasourceId 数据源id
     * @param tableName    表名
     * @return 预览的数据
     */
    List<AnyData> preview(Long datasourceId, String tableName);

    List<TableDTO> getTablesByRelationId(Long relationId);
}
