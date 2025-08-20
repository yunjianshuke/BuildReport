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

import com.yunjian.common.group.SaveGroup;
import com.yunjian.common.group.UpdateGroup;
import com.yunjian.common.util.R;
import com.yunjian.datarelation.dto.ReportGroupDTO;
import com.yunjian.datarelation.service.impl.ReportBusinessGroupService;
import io.swagger.v3.oas.annotations.Operation;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yujian
 * @description
 * @create 2024-12-09 17:10
 **/
@RestController
@RequestMapping("/businessGroup")
public class ReportBusinessGroupController {

    @Resource
    private ReportBusinessGroupService reportGroupService;


    /**
     * 分页查询
     *
     * @param reportGroupDTO reportGroupDTO
     * @return R
     */
    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("/getPage")
    public R getPage(@Validated @RequestBody ReportGroupDTO reportGroupDTO) {
        return R.ok(reportGroupService.getPage(reportGroupDTO));
    }

    /**
     * 通过ReportGroupDTO查询业务分组表list
     *
     * @param reportGroupDTO
     * @return R
     */
    @PostMapping("/getList")
    public R getList(@Validated @RequestBody ReportGroupDTO reportGroupDTO) {
        return R.ok(this.reportGroupService.getList(reportGroupDTO));
    }

    /**
     * 通过ReportGroupDTO查询业务分组表单条
     *
     * @param reportGroupDTO
     * @return R
     */
    @PostMapping("/getOne")
    public R getOne(@RequestBody ReportGroupDTO reportGroupDTO) {
        return R.ok(this.reportGroupService.getOne(reportGroupDTO));
    }

    /**
     * 新增业务分组表
     *
     * @param reportGroupDTO
     * @return R
     */
    @PostMapping("/saveEntity")
    public R saveEntity(@Validated(SaveGroup.class) @RequestBody ReportGroupDTO reportGroupDTO) {
        return R.ok(this.reportGroupService.saveEntityWitchVO(reportGroupDTO));
    }

    /**
     * 修改业务分组表
     *
     * @param reportGroupDTO
     * @return R
     */
    @PostMapping("/update")
    public R update(@Validated(UpdateGroup.class) @RequestBody ReportGroupDTO reportGroupDTO) {
        return R.ok(this.reportGroupService.update(reportGroupDTO) > 0);
    }

    /**
     * 通过ids批量删除业务分组表
     *
     * @param ids ids
     * @return R
     */
    @PostMapping("/deleteByIds")
    public R deleteByIds(@RequestParam Long[] ids) {
        return R.ok(this.reportGroupService.deleteByIdsOutTables(ids));
    }

    /**
     * 获取树形结构列表
     *
     * @param ReportGroupDTO dto
     * @return R
     */
    @PostMapping("/tree")
    public R tree(@Validated @RequestBody ReportGroupDTO ReportGroupDTO) {
        return R.ok(this.reportGroupService.getTree(ReportGroupDTO));
    }

}
