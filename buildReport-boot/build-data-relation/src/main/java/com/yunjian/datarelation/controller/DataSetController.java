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

import com.yunjian.common.util.R;
import com.yunjian.datarelation.anno.LogicDeleteAware;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.dto.DataSetPreviewDTO;
import com.yunjian.datarelation.dto.ReportInfoDTO;
import com.yunjian.datarelation.engine.BusinessEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yujian
 **/
@RestController
@RequestMapping("/dataset")
@RequiredArgsConstructor
public class DataSetController {

    private final BusinessEngine businessEngine;


    @PostMapping("/run/{relationId}")
    @LogicDeleteAware
    public R<List<AnyData>> run(@PathVariable Long relationId,
                                @RequestBody DataSetPreviewDTO data) {
        ReportInfoDTO reportInfoDTO = new ReportInfoDTO();
        reportInfoDTO.setPageNumber(0);
        reportInfoDTO.setPageSize(200);

        return R.ok(businessEngine.run(relationId, data.getData(), data.getDataFilters(), reportInfoDTO)
                .getData());
    }

}
