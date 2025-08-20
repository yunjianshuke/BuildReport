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
import com.yunjian.datarelation.dto.ReportDictDTO;
import com.yunjian.datarelation.dto.ReportDictDTO.UpdateGroup;
import com.yunjian.datarelation.service.impl.ReportDictConfService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yujian
 **/
@RestController
@RequestMapping("/reportDict")
public class ReportDictController {

    @Resource
    private ReportDictConfService reportDictConfService;


    /**
     * 保存/更新字典
     *
     * @return 保存结果
     */
    @PostMapping("/save")
    public R<Void> saveEtl(@Validated(UpdateGroup.class) @RequestBody ReportDictDTO requestDTO) {
        reportDictConfService.saveDictConf(requestDTO);
        return R.ok();
    }

    /**
     * 删除字典配置
     *
     * @param confId conf
     * @return ok
     */
    @PostMapping("/remove/{confId}")
    public R<Void> removeConf(@PathVariable String confId) {
        reportDictConfService.removeConf(confId);
        return R.ok();
    }

    /**
     * 详情
     *
     * @param confId confId
     * @return 详情
     */
    @GetMapping("/detail/{confId}")
    public R<ReportDictDTO> detail(@PathVariable String confId) {
        return R.ok(reportDictConfService.detailById(confId));
    }

    /**
     * 分页查询
     *
     * @param reportDictDTO reportGroupDTO
     * @return R
     */
    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("/getPage")
    public R<Page<ReportDictDTO>> getPage(@Validated @RequestBody ReportDictDTO reportDictDTO) {
        return R.ok(reportDictConfService.getPage(reportDictDTO));
    }

    /**
     * 获取下拉字典项内容
     *
     * @return 全部字典
     */
    @GetMapping("/getList")
    public R<List<ReportDictDTO>> getList() {
        return R.ok(reportDictConfService.getList());
    }

}
