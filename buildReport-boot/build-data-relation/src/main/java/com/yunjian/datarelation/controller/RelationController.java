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

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjian.common.util.R;
import com.yunjian.datarelation.dto.BusinessGroupReqDTO;
import com.yunjian.datarelation.dto.BusinessRelationInfoDTO;
import com.yunjian.datarelation.dto.BusinessRelationInfoPageDTO;
import com.yunjian.datarelation.service.ReportBusinessRelationInfoService;
import com.yunjian.datarelation.vo.BusinessRelationInfoPageVO;
import com.yunjian.datarelation.vo.BusinessRelationInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 业务关系控制器
 * Created on 2024/6/18 14:31
 *
 * @author fusheng
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/relation")
public class RelationController {

    @Resource
    public ReportBusinessRelationInfoService reportBusinessRelationInfoService;


    /**
     * 分页查询
     *
     * @param businessRelationInfoDTO 查询条件
     * @author fusheng
     * @date 2024/6/18 15:01
     */
    @PostMapping("/page")
    public R<Page<BusinessRelationInfoPageVO>> page(@RequestBody BusinessRelationInfoPageDTO businessRelationInfoDTO) {
        return R.ok(reportBusinessRelationInfoService.getPage(businessRelationInfoDTO));
    }

    /**
     * 保存
     *
     * @param businessRelationInfoVO 业务关系信息
     * @author fusheng
     * @date 2024/6/18 15:01
     */
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody BusinessRelationInfoDTO businessRelationInfoVO) {
        reportBusinessRelationInfoService.checkAndSave(businessRelationInfoVO);
        return R.ok();
    }

    @PostMapping("/updateGroup")
    public R<Boolean> updateGroup(@RequestBody BusinessRelationInfoDTO businessRelationInfoVO) {
        reportBusinessRelationInfoService.updateGroup(businessRelationInfoVO);
        return R.ok();
    }

    /**
     * 删除
     *
     * @param relationId 业务关系ID
     * @author fusheng
     * @date 2024/6/18 15:02
     */
    @PostMapping("/delete/{relationId}")
    public R<Boolean> delete(@PathVariable Long relationId) {
        return R.ok(reportBusinessRelationInfoService.removeById(relationId));
    }

    /**
     * 详情
     *
     * @param relationId 业务关系ID
     * @author fusheng
     * @date 2024/6/19 16:40
     */
    @PostMapping("/info/{relationId}")
    public R<BusinessRelationInfoVO> info(@PathVariable Long relationId) {
        return R.ok(reportBusinessRelationInfoService.getInfo(relationId));
    }

    /**
     * 查询权限下所有业务关系
     *
     * @author fusheng
     * @date 2024/6/28 上午11:36
     */
    @GetMapping("/listByAuth")
    public R<List<BusinessRelationInfoVO>> listByAuth(BusinessGroupReqDTO businessGroupReqDTO) {
        return R.ok(reportBusinessRelationInfoService.listByAuth(businessGroupReqDTO));
    }

    /**
     * 在报表设计器中，创建数据集时弹出的选择器接口
     *
     * @return tree
     */
    @PostMapping("/treeByAuth")
    public R<List<Tree<Long>>> treeByAuth() {
        return R.ok(reportBusinessRelationInfoService.treeByAuth());
    }


}
