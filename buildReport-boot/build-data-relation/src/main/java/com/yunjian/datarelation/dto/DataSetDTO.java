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
package com.yunjian.datarelation.dto;

import com.yunjian.datarelation.engine.pojo.DataFilter;
import lombok.Data;

import java.util.List;

/**
 * @author yujian
 **/
@Data
public class DataSetDTO {
    private Long id;
    private String datasetName;
    private Integer type;
    private String relationId;
    private Long dataSourceId;
    private Integer businessType;
    private List<BusinessDatasetDTO> tables;
    private List<DataFilter> dataFilter;


    public boolean isUpdate() {
        return id != null;
    }

}
