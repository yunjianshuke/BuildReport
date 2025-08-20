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
package com.yunjian.datarelation.common;

import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.dto.TableDTO;

import java.util.List;

/**
 * 不同数据库统一规范,方便扩展新的数据库
 *
 * @author yujian
 */
public interface DataSourceAdepter {

    /**
     * 预览表的数据
     *
     * @param tableName 表名
     * @param limit     数量
     * @return data
     */
    List<AnyData> privew(String tableName, Integer limit);

    /**
     * 获取所有表
     *
     * @return tables
     */
    List<TableDTO> allTables(String tableName);

    /**
     * 获取表所有字段
     *
     * @param tableName 表名
     * @return fields
     */
    List<DbFieldDTO> tableFields(String tableName);

}
