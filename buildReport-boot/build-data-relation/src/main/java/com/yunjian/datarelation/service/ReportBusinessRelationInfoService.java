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

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunjian.datarelation.dto.BusinessGroupReqDTO;
import com.yunjian.datarelation.dto.BusinessRelationInfoDTO;
import com.yunjian.datarelation.dto.BusinessRelationInfoPageDTO;
import com.yunjian.datarelation.entity.ReportBusinessRelationInfo;
import com.yunjian.datarelation.vo.BusinessRelationInfoPageVO;
import com.yunjian.datarelation.vo.BusinessRelationInfoVO;

import java.util.List;

/**
 * @author luxuanhe
 * @description 针对表【report_business_relation_info(报表业务关系图)】的数据库操作Service
 * @createDate 2024-06-18 14:34:25
 */
public interface ReportBusinessRelationInfoService extends IService<ReportBusinessRelationInfo> {

    /**
     * 分页查询
     *
     * @param businessRelationInfoDTO 查询条件
     * @author fusheng
     * @date 2024/6/18 14:41
     */
    Page<BusinessRelationInfoPageVO> getPage(BusinessRelationInfoPageDTO businessRelationInfoDTO);

    /**
     * 保存
     *
     * @param businessRelationInfoVO 业务关系信息 {@link BusinessRelationInfoDTO}
     * @author fusheng
     * @date: 2024/7/2 上午9:20
     */
    void checkAndSave(BusinessRelationInfoDTO businessRelationInfoVO);

    /**
     * 获取业务关系信息
     *
     * @param relationId 业务关系id
     * @author fusheng
     * @date: 2024/7/2 上午9:20
     */
    BusinessRelationInfoVO getInfo(Long relationId);

    /**
     * 查询节点和节点关系
     *
     * @param reportBusinessRelationInfo 业务关系信息
     * @author fusheng
     * @date 2024/6/28 上午11:31
     */
    BusinessRelationInfoVO getNodeAndEdgeByRelationId(
            ReportBusinessRelationInfo reportBusinessRelationInfo);

    /**
     * 查询权限下所有业务关系
     *
     * @author fusheng
     * @date 2024/6/28 上午11:18
     */
    List<BusinessRelationInfoVO> listByAuth(BusinessGroupReqDTO businessGroupReqDTO);

    List<Tree<Long>> treeByAuth();

    void updateGroup(BusinessRelationInfoDTO businessRelationInfoVO);
}
