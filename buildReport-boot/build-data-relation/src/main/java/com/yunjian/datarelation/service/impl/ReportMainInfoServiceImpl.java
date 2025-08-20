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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.SqlUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.dto.DataSetDTO;
import com.yunjian.datarelation.dto.ReportInfoDTO;
import com.yunjian.datarelation.dto.ReportMainInfoPageDTO;
import com.yunjian.datarelation.entity.ReportDatasetRelation;
import com.yunjian.datarelation.entity.ReportGroup;
import com.yunjian.datarelation.entity.ReportMainInfo;
import com.yunjian.datarelation.mapper.ReportMainInfoMapper;
import com.yunjian.datarelation.service.ReportDataSetService;
import com.yunjian.datarelation.service.ReportGroupService;
import com.yunjian.datarelation.vo.ReportInfoVO;
import com.yunjian.datarelation.vo.ReportMainInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author z7638
 * @description 针对表【report_main_info(报表主体信息表)】的数据库操作Service实现
 * @createDate 2024-07-03 11:27:38
 */
@Service
public class ReportMainInfoServiceImpl extends ServiceImpl<ReportMainInfoMapper, ReportMainInfo>
        implements ReportMainInfoService {

    @Resource
    private ReportGroupService reportGroupService;
    @Resource
    private ReportDatasetRelationService reportDatasetRelationService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveInfo(ReportInfoDTO reportInfoDTO) {
        //先更新报表主体信息
        ReportMainInfo reportMainInfo;
        if (reportInfoDTO.getReportId() == null) {
            long count = count(Wrappers.<ReportMainInfo>lambdaQuery()
                    .eq(ReportMainInfo::getName, reportInfoDTO.getName()));
            if (count > 0) {
                throw new BusinessException("1", "报表名称已存在");
            }
            reportMainInfo = new ReportMainInfo();
        } else {
            reportMainInfo = getById(reportInfoDTO.getReportId());
            reportMainInfo.setReportId(reportInfoDTO.getReportId());
            if (!reportMainInfo.getName().equals(reportInfoDTO.getName())) {
                long count = count(Wrappers.<ReportMainInfo>lambdaQuery()
                        .eq(ReportMainInfo::getName, reportInfoDTO.getName()));
                if (count > 0) {
                    throw new BusinessException("1", "报表名称已存在");
                }
            }
        }


        reportMainInfo.setGroupId(reportInfoDTO.getGroupId());
        reportMainInfo.setName(reportInfoDTO.getName());
        reportMainInfo.setComponentJson(reportInfoDTO.getComponentJson());
        reportMainInfo.setFilterComponents(reportInfoDTO.getFilterComponents());
        saveOrUpdate(reportMainInfo);


        reportDatasetRelationService.remove(Wrappers.<ReportDatasetRelation>lambdaQuery().eq(ReportDatasetRelation::getReportId, reportMainInfo.getReportId()));

        if (CollUtil.isNotEmpty(reportInfoDTO.getDataSetId())) {
            List<ReportDatasetRelation> collect = reportInfoDTO.getDataSetId()
                    .stream()
                    .map(dataSetId -> {
                        ReportDatasetRelation reportDatasetRelation = new ReportDatasetRelation();
                        reportDatasetRelation.setReportId(reportMainInfo.getReportId());
                        reportDatasetRelation.setDataSetId(dataSetId);
                        return reportDatasetRelation;
                    }).collect(Collectors.toList());
            reportDatasetRelationService.saveBatch(collect);
        }


        return reportMainInfo.getReportId();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long reportId) {
        removeById(reportId);
        reportDatasetRelationService.removeByReportId(reportId);

    }

    @Override
    public ReportMainInfo info(Long reportId) {
        return null;
    }

    @Override
    public ReportInfoVO getInfo(Long reportId) {
        ReportMainInfo reportMainInfo = getById(reportId);
        if (ObjectUtil.isNull(reportMainInfo)) {
            throw new BusinessException("-1", "报表不存在");
        }
        ReportGroup reportGroup = reportGroupService.getById(reportMainInfo.getGroupId());
        if (ObjectUtil.isNull(reportGroup)) {
            throw new BusinessException("-1", "报表分组不存在");
        }

        ReportInfoVO reportInfoVO = new ReportInfoVO();
        BeanUtil.copyProperties(reportMainInfo, reportInfoVO);
        reportInfoVO.setBizGroupName(reportGroup.getBizGroupName());

        List<DataSetDTO> dataSetDTOList = reportDatasetRelationService.getDataSetByReportId(
                reportMainInfo.getReportId());

        reportInfoVO.setDataset(dataSetDTOList);

        return reportInfoVO;
    }

    @Override
    public Page<ReportMainInfoVO> getPage(ReportMainInfoPageDTO reportMainInfoPageDTO) {
        Page<ReportMainInfo> page = page(reportMainInfoPageDTO,
                Wrappers.<ReportMainInfo>lambdaQuery()
                        .like(StrUtil.isNotBlank(reportMainInfoPageDTO.getName()), ReportMainInfo::getName,
                                reportMainInfoPageDTO.getName())
                        .eq(ObjectUtil.isNotEmpty(reportMainInfoPageDTO.getGroupId()),
                                ReportMainInfo::getGroupId, reportMainInfoPageDTO.getGroupId())
                        .orderByDesc(ReportMainInfo::getCreateTime));
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        }
        List<ReportMainInfoVO> reportMainInfoVOList = new ArrayList<>();
        List<Long> groupIds = page.getRecords().stream().map(ReportMainInfo::getGroupId)
                .collect(Collectors.toList());
        List<ReportGroup> reportGroups = reportGroupService.listByIds(groupIds);
        Map<Long, String> groupNameMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(reportGroups)) {
            groupNameMap = reportGroups.stream()
                    .collect(Collectors.toMap(ReportGroup::getId, ReportGroup::getBizGroupName));
        }

        for (ReportMainInfo reportMainInfo : page.getRecords()) {
            ReportMainInfoVO reportMainInfoVO = new ReportMainInfoVO();
            BeanUtil.copyProperties(reportMainInfo, reportMainInfoVO);
            reportMainInfoVO.setBizGroupName(groupNameMap.get(reportMainInfo.getGroupId()));

            reportMainInfoVOList.add(reportMainInfoVO);
        }
        return new Page<ReportMainInfoVO>(page.getCurrent(), page.getSize(),
                page.getTotal()).setRecords(
                reportMainInfoVOList);
    }

    @Override
    @Transactional
    public Long saveAsInfo(ReportInfoDTO reportInfoDTO) {
        ReportMainInfo origin = getById(reportInfoDTO.getReportId());

        List<DataSetDTO> dataSetByReportId = reportDatasetRelationService.getDataSetByReportId(
                reportInfoDTO.getReportId());
        List<Long> dataSetIds = dataSetByReportId.stream().map(DataSetDTO::getId)
                .collect(Collectors.toList());

        ReportInfoDTO copy = new ReportInfoDTO();
        BeanUtil.copyProperties(origin, copy);

        copy.setReportId(null);
        copy.setGroupId(reportInfoDTO.getGroupId());
        copy.setName(reportInfoDTO.getName());
        copy.setDataSetId(dataSetIds);
        return saveInfo(copy);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateGroupAndName(ReportMainInfo reportInfoDTO) {
        ReportMainInfo reportMainInfo = getById(reportInfoDTO.getReportId());
        reportMainInfo.setGroupId(reportInfoDTO.getGroupId());
        if (!reportMainInfo.getName().equals(reportInfoDTO.getName())) {
            long count = count(Wrappers.<ReportMainInfo>lambdaQuery()
                    .eq(ReportMainInfo::getName, reportInfoDTO.getName()));
            if (count > 0) {
                throw new BusinessException("1", "报表名称已存在");
            }
            reportMainInfo.setName(reportInfoDTO.getName());
        }
        updateById(reportMainInfo);


    }

    @Override
    public Page<ReportMainInfoVO> listByAuthPage(ReportMainInfoPageDTO reportMainInfoPageDTO) {
        LambdaQueryWrapper<ReportMainInfo> reportWrapper = Wrappers.<ReportMainInfo>lambdaQuery();
        if (reportMainInfoPageDTO != null) {
            reportWrapper.like(StrUtil.isNotBlank(reportMainInfoPageDTO.getName()), ReportMainInfo::getName,
                    SqlUtil.buildLikeValue(reportMainInfoPageDTO.getName(), Condition.LikeType.Contains, false));
            reportWrapper.in(CollUtil.isNotEmpty(reportMainInfoPageDTO.getBizGroupIds()), ReportMainInfo::getGroupId,
                reportMainInfoPageDTO.getBizGroupIds());
        }
        Page<ReportMainInfo> reportMainInfoList = page(reportMainInfoPageDTO, reportWrapper);

        if (CollectionUtil.isEmpty(reportMainInfoList.getRecords())) {
            return new Page<>();
        }
        List<ReportMainInfoVO> reportMainInfoVOList = new ArrayList<>();

        Set<Long> groupIds = reportMainInfoList.getRecords().stream()
                .filter(reportMainInfo -> reportMainInfo.getGroupId() != null)
                .map(ReportMainInfo::getGroupId).collect(Collectors.toSet());

        Map<Long, String> groupMap = new HashMap<>();
        if (CollUtil.isNotEmpty(groupIds)) {
            List<ReportGroup> list = reportGroupService.list(
                    Wrappers.<ReportGroup>lambdaQuery().in(ReportGroup::getId, groupIds));
            groupMap = list.stream().collect(Collectors.toMap(ReportGroup::getId, ReportGroup::getBizGroupName));
        }

        for (ReportMainInfo reportMainInfo : reportMainInfoList.getRecords()) {
            ReportMainInfoVO reportMainInfoVO = new ReportMainInfoVO();
            BeanUtil.copyProperties(reportMainInfo, reportMainInfoVO);
            reportMainInfoVOList.add(reportMainInfoVO);
            String groupName = groupMap.get(reportMainInfo.getGroupId());
            if (groupName != null) {
                reportMainInfoVO.setBizGroupName(groupName);
            }
        }

        return new Page<ReportMainInfoVO>(reportMainInfoPageDTO.getCurrent(), reportMainInfoPageDTO.getSize(),
                reportMainInfoPageDTO.getTotal()).setRecords(
                reportMainInfoVOList);
    }

    @Override
    public List<ReportMainInfoVO> listByAuth(ReportMainInfoPageDTO reportMainInfoPageDTO) {
        LambdaQueryWrapper<ReportMainInfo> reportWrapper = Wrappers.lambdaQuery();
        List<ReportMainInfo> reportMainInfoList = list(reportWrapper);

        if (reportMainInfoPageDTO != null) {
            reportWrapper.like(StrUtil.isNotBlank(reportMainInfoPageDTO.getName()), ReportMainInfo::getName,
                    SqlUtil.buildLikeValue(reportMainInfoPageDTO.getName(), Condition.LikeType.Contains, false));
        }

        if (CollectionUtil.isEmpty(reportMainInfoList)) {
            return new ArrayList<>();
        }
        List<ReportMainInfoVO> reportMainInfoVOList = new ArrayList<>();

        for (ReportMainInfo reportMainInfo : reportMainInfoList) {
            ReportMainInfoVO reportMainInfoVO = new ReportMainInfoVO();
            BeanUtil.copyProperties(reportMainInfo, reportMainInfoVO);
            reportMainInfoVOList.add(reportMainInfoVO);
        }
        return reportMainInfoVOList;
    }
}




