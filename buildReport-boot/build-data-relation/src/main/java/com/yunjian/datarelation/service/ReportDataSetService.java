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

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunjian.datarelation.dto.DataSetDTO;
import com.yunjian.datarelation.entity.ReportDataSetV2;

import java.util.List;
import java.util.Map;

/**
 * @author z7638
 * @description 针对表【report_data_set(数据集表)】的数据库操作Service
 * @createDate 2024-07-03 11:27:20
 */
public interface ReportDataSetService extends IService<ReportDataSetV2> {


    void saveDataSet(DataSetDTO dataSetRequestDTO);

    Map<Long, List<ReportDataSetV2>> getRelationMap();
}
