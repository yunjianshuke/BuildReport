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
package com.yunjian.datarelation.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Condition.LikeType;
import cn.hutool.db.sql.SqlUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.dto.BusinessGroupReqDTO;
import com.yunjian.datarelation.dto.BusinessRelationInfoDTO;
import com.yunjian.datarelation.dto.BusinessRelationInfoPageDTO;
import com.yunjian.datarelation.entity.*;
import com.yunjian.datarelation.mapper.DatasourceMapper;
import com.yunjian.datarelation.mapper.ReportBusinessRelationInfoMapper;
import com.yunjian.datarelation.service.*;
import com.yunjian.datarelation.utils.PageUtils;
import com.yunjian.datarelation.vo.BusinessRelationInfoPageVO;
import com.yunjian.datarelation.vo.BusinessRelationInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author luxuanhe
 * @description 针对表【report_business_relation_info(报表业务关系图)】的数据库操作Service实现
 * @createDate 2024-06-18 14:34:25
 */
@Slf4j
@Service
public class ReportBusinessRelationInfoServiceImpl extends
        ServiceImpl<ReportBusinessRelationInfoMapper, ReportBusinessRelationInfo> implements
        ReportBusinessRelationInfoService {

    @Resource
    private ReportBusinessNodeService reportBusinessNodeService;

    @Resource
    private ReportBusinessNodeRelationService reportBusinessNodeRelationService;

    @Resource
    private DatasourceMapper datasourceMapper;
    @Resource
    private ReportDataSetService reportDataSetService;
    @Resource
    private ReportBusinessGroupService reportBusinessGroupService;
    /*@Resource
    private RemoteBuildUserService remoteBuildUserService;*/


    @Override
    public Page<BusinessRelationInfoPageVO> getPage(
            BusinessRelationInfoPageDTO businessRelationInfoDTO) {
        Page<ReportBusinessRelationInfo> page = new Page<>(businessRelationInfoDTO.getCurrent(),
                businessRelationInfoDTO.getSize());
        Page<ReportBusinessRelationInfo> selectPage = this.baseMapper.selectPage(page,
                new LambdaQueryWrapper<ReportBusinessRelationInfo>()
                        .eq(StrUtil.isNotBlank(businessRelationInfoDTO.getGroupId()), ReportBusinessRelationInfo::getBizGroupId, businessRelationInfoDTO.getGroupId())
                        .like(StrUtil.isNotBlank(businessRelationInfoDTO.getName()),
                                ReportBusinessRelationInfo::getRelationName,
                                SqlUtil.buildLikeValue(businessRelationInfoDTO.getName(), LikeType.Contains, false))
                        .orderByDesc(ReportBusinessRelationInfo::getUpdateTime)
        );
        Set<Long> groupId = selectPage.getRecords().stream().map(
                        ReportBusinessRelationInfo::getBizGroupId)
                .collect(Collectors.toSet());
        Map<Long, String> groupMap;
        if (CollUtil.isNotEmpty(groupId)) {
            List<ReportBusinessGroup> reportBusinessGroups = reportBusinessGroupService.listByIds(
                    groupId);
            groupMap = reportBusinessGroups.stream().collect(
                    Collectors.toMap(ReportBusinessGroup::getId, ReportBusinessGroup::getBizGroupName));
        } else {
            groupMap = Collections.emptyMap();
        }

        Page<BusinessRelationInfoPageVO> pageVO = new Page<>();
        BeanUtils.copyProperties(selectPage, pageVO);


        return PageUtils.mapRecords(selectPage, record -> {
            BusinessRelationInfoPageVO businessRelationInfoPageVO = new BusinessRelationInfoPageVO();
            BeanUtils.copyProperties(record, businessRelationInfoPageVO);
//      businessRelationInfoPageVO.setCreateByName(userMap.getOrDefault(Long.valueOf(record.getCreateBy()),""));
            businessRelationInfoPageVO.setBizGroupName(groupMap.getOrDefault(record.getBizGroupId(), ""));
            return businessRelationInfoPageVO;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndSave(BusinessRelationInfoDTO businessRelationInfoVO) {
        log.info("业务关系信息：{}", businessRelationInfoVO);
        // 校验业务关系名称是否存在

        List<ReportBusinessRelationInfo> reportBusinessRelationInfoList = baseMapper.selectList(
                Wrappers.<ReportBusinessRelationInfo>lambdaQuery()
                        .eq(ReportBusinessRelationInfo::getRelationName,
                                businessRelationInfoVO.getRelationName())
                        .ne(businessRelationInfoVO.getRelationId() != null,
                                ReportBusinessRelationInfo::getRelationId, businessRelationInfoVO.getRelationId()));
        if (CollectionUtil.isNotEmpty(reportBusinessRelationInfoList)) {
            log.info("业务关系名称已存在");
            throw new BusinessException("0", "业务关系名称已存在");
        }
        boolean save = businessRelationInfoVO.getRelationId() == null;

        // 保存业务关系信息
        saveOrUpdate(businessRelationInfoVO);

        // 保存节点和节点关系信息
        reportBusinessNodeService.remove(Wrappers.<ReportBusinessNode>lambdaQuery()
                .eq(ReportBusinessNode::getRelationId, businessRelationInfoVO.getRelationId()));
        if (CollectionUtil.isNotEmpty(businessRelationInfoVO.getNodeList())) {
            businessRelationInfoVO.getNodeList().forEach(node -> {
                node.setRelationId(businessRelationInfoVO.getRelationId());
            });
            reportBusinessNodeService.saveBatch(businessRelationInfoVO.getNodeList());
        }
        reportBusinessNodeRelationService.remove(Wrappers.<ReportBusinessNodeRelation>lambdaQuery()
                .eq(ReportBusinessNodeRelation::getRelationId, businessRelationInfoVO.getRelationId()));
        if (CollectionUtil.isNotEmpty(businessRelationInfoVO.getEdgeList())) {
            businessRelationInfoVO.getEdgeList().forEach(edge -> {
                edge.setRelationId(businessRelationInfoVO.getRelationId());
            });
            reportBusinessNodeRelationService.saveBatch(businessRelationInfoVO.getEdgeList());
        }
    }

    @Override
    public BusinessRelationInfoVO getInfo(Long relationId) {
        // 查询业务关系信息
        ReportBusinessRelationInfo reportBusinessRelationInfo = getById(relationId);
        // 查询节点信息
        if (reportBusinessRelationInfo != null) {
            return getNodeAndEdgeByRelationId(reportBusinessRelationInfo);
        }
        return new BusinessRelationInfoVO();
    }


    /**
     * 查询节点和节点关系
     *
     * @param reportBusinessRelationInfo 业务关系信息
     * @author fusheng
     * @date 2024/6/28 上午11:31
     */
    @Override
    public BusinessRelationInfoVO getNodeAndEdgeByRelationId(
            ReportBusinessRelationInfo reportBusinessRelationInfo) {
        Long relationId = reportBusinessRelationInfo.getRelationId();
        BusinessRelationInfoVO businessRelationInfoVO = new BusinessRelationInfoVO();

        BeanUtils.copyProperties(reportBusinessRelationInfo, businessRelationInfoVO);
        // 查询节点信息
        List<ReportBusinessNode> reportBusinessNodeList = reportBusinessNodeService.list(
                Wrappers.<ReportBusinessNode>lambdaQuery()
                        .eq(ReportBusinessNode::getRelationId, relationId));
        if (CollectionUtil.isNotEmpty(reportBusinessNodeList)) {
            List<Long> datasourceIdList = reportBusinessNodeList.stream()
                    .map(ReportBusinessNode::getDatasourceId).distinct().collect(Collectors.toList());
            List<Datasource> datasources = datasourceMapper.selectList(
                    Wrappers.<Datasource>lambdaQuery().in(Datasource::getId, datasourceIdList));
            if (CollectionUtil.isNotEmpty(datasources)) {
                Map<Long, String> datasourceMap = datasources.stream()
                        .collect(Collectors.toMap(Datasource::getId, Datasource::getName));

                reportBusinessNodeList.forEach(reportBusinessNode -> {
                    reportBusinessNode.setDatasourceName(
                            datasourceMap.getOrDefault(reportBusinessNode.getDatasourceId(), ""));

                });
            }
        }
        businessRelationInfoVO.setNodeList(reportBusinessNodeList);
        // 查询节点关系信息
        List<ReportBusinessNodeRelation> reportBusinessNodeRelationList = reportBusinessNodeRelationService.list(
                Wrappers.<ReportBusinessNodeRelation>lambdaQuery()
                        .eq(ReportBusinessNodeRelation::getRelationId, relationId));
        businessRelationInfoVO.setEdgeList(reportBusinessNodeRelationList);
        return businessRelationInfoVO;
    }

    @Override
    public List<BusinessRelationInfoVO> listByAuth(BusinessGroupReqDTO businessGroupReqDTO) {

        List<ReportBusinessRelationInfo> list = list(Wrappers.<ReportBusinessRelationInfo>lambdaQuery()
                .eq(ObjectUtil.isNotNull(businessGroupReqDTO.getBizGroupId()),
                        ReportBusinessRelationInfo::getBizGroupId,
                        businessGroupReqDTO.getBizGroupId()));
        //查询node节点和edge
        List<BusinessRelationInfoVO> businessRelationInfoVOList = new ArrayList<>();
        list.forEach(reportBusinessRelationInfo -> {
            businessRelationInfoVOList.add(getNodeAndEdgeByRelationId(reportBusinessRelationInfo));
        });
        return businessRelationInfoVOList;

    }

    @Override
    public void updateGroup(BusinessRelationInfoDTO businessRelationInfoVO) {
        ReportBusinessRelationInfo reportBusinessRelationInfo = getById(businessRelationInfoVO.getRelationId());
        if (reportBusinessRelationInfo != null) {
            reportBusinessRelationInfo.setBizGroupId(businessRelationInfoVO.getBizGroupId());
            updateById(reportBusinessRelationInfo);
        }
    }

    @Override
    public List<Tree<Long>> treeByAuth() {
        // 每个业务线下所的数据集
        Map<Long, List<ReportDataSetV2>> relationIdList = reportDataSetService.getRelationMap();

        // 每个业务线的分组
        List<ReportBusinessRelationInfo> list = list(Wrappers.<ReportBusinessRelationInfo>lambdaQuery()
                .in(ReportBusinessRelationInfo::getRelationId, relationIdList.keySet()));
        Map<Long, List<ReportBusinessRelationInfo>> groupMap = list.stream()
                .collect(Collectors.groupingBy(ReportBusinessRelationInfo::getBizGroupId));
        // 所有分组
        List<ReportBusinessGroup> group = reportBusinessGroupService.list();

        List<TreeNode<Long>> collect = group.stream().map(businessGroup -> {
                    JSONObject jsonObject = new JSONObject();
                    List<ReportBusinessRelationInfo> reportBusinessRelationInfos = groupMap.getOrDefault(businessGroup.getId(), Collections.emptyList());
                    for (ReportBusinessRelationInfo reportBusinessRelationInfo : reportBusinessRelationInfos) {
                        List<ReportDataSetV2> datasets = relationIdList.getOrDefault(
                            reportBusinessRelationInfo.getRelationId(), Collections.emptyList())
                            .stream()
                            .filter(dataSet -> dataSet.getType() == 0)
                            .collect(Collectors.toList());
                        jsonObject.putOnce(reportBusinessRelationInfo.getRelationName(), datasets);
                    }

                    TreeNode<Long> node = getNodeFunction().apply(businessGroup);
                    Map<String, Object> extra = node.getExtra();
                    extra.put("data", jsonObject);
                    return node;
                })
                .collect(Collectors.toList());


        return TreeUtil.build(collect, 0L);

    }

    private Function<ReportBusinessGroup, TreeNode<Long>> getNodeFunction() {
        return dbGroup -> {
            TreeNode<Long> node = new TreeNode<>();

            node.setId(dbGroup.getId());
            node.setName(dbGroup.getBizGroupName());
            node.setParentId(Objects.isNull(dbGroup.getParentId()) ? 0L : dbGroup.getParentId());
            node.setWeight(dbGroup.getSeq());

            Map<String, Object> extra = new HashMap<>();
            extra.put("bizGroupName", dbGroup.getBizGroupName());
            extra.put("bizGroupCode", dbGroup.getBizGroupCode());

            node.setExtra(extra);
            return node;
        };
    }
}




