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
import com.yunjian.datarelation.dto.ETLFunGroupDTO;
import com.yunjian.datarelation.dto.ReportEtlFunSymbolVO;
import com.yunjian.datarelation.service.ETLFunGroupService;
import com.yunjian.datarelation.service.ReportEtlFunSymbolService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yujian
 **/
@Slf4j
@RestController
@RequestMapping("/etl2")
@RequiredArgsConstructor
public class ETLController {

    private final ReportEtlFunSymbolService reportEtlFunSymbolService;
    private final ETLFunGroupService etlFunGroupService;

    @GetMapping("/func")
    public R<List<ETLFunGroupDTO>> getConvertFuncGroup() {
        return R.ok(etlFunGroupService.getConvertFuncGroup());
    }

    /**
     * 常用符号
     *
     * @return symbol
     */
    @GetMapping("/symbol")
    public R<List<ReportEtlFunSymbolVO>> symbol() {
        return R.ok(reportEtlFunSymbolService.symbollist());
    }

}
