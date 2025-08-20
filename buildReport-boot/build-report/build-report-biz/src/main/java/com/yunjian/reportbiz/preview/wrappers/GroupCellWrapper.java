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
package com.yunjian.reportbiz.preview.wrappers;

import com.yunjian.datarelation.common.AnyData;
import com.yunjian.reportbiz.preview.CellData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yujian
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupCellWrapper extends CellWrapper {


    private List<CellData> groupData;
    private Map<CellData, List<CellData>> dataMergeCountMap;
    private Map<AnyData, Integer> dataCountMap;
    private AtomicInteger indexOffSet;
    private boolean isId;


    private int max;

}
