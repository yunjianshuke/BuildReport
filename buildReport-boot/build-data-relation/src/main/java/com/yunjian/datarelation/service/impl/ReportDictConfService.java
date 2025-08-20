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
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Condition.LikeType;
import cn.hutool.db.sql.SqlUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.dto.ReportDictDTO;
import com.yunjian.datarelation.entity.ReportDictCondition;
import com.yunjian.datarelation.entity.ReportDictConf;
import com.yunjian.datarelation.enums.DictEventEnum;
import com.yunjian.datarelation.event.DictChangeEvent;
import com.yunjian.datarelation.mapper.ReportDictConfMapper;
import com.yunjian.datarelation.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
@Service
@RequiredArgsConstructor
public class ReportDictConfService extends ServiceImpl<ReportDictConfMapper, ReportDictConf> {

    private final ReportDictConditionService reportDictConditionService;
    private final ApplicationEventPublisher publisher;

    @Transactional(rollbackFor = Throwable.class)
    public void saveDictConf(ReportDictDTO requestDTO) {
        ReportDictConf conf;
        if (requestDTO.isUpdate()) {
            conf = getById(requestDTO.getId());
            reportDictConditionService.remove(Wrappers.<ReportDictCondition>lambdaQuery().eq(ReportDictCondition::getDictConfId, requestDTO.getId()));
        } else {
            conf = new ReportDictConf();
        }
        if (conf == null) {
            throw new BusinessException("1", "数据不存在");
        }
        BeanUtils.copyProperties(requestDTO, conf, "id");
        if (requestDTO.isUpdate()) {
            updateById(conf);
        } else {
            save(conf);
        }

        List<ReportDictCondition> conditions = requestDTO.getConditionList().stream()
                .map(conditionDTO -> {
                    ReportDictCondition condition = new ReportDictCondition();
                    BeanUtils.copyProperties(conditionDTO, condition);
                    condition.setDictConfId(conf.getId());
                    return condition;
                }).collect(Collectors.toList());

        reportDictConditionService.saveBatch(conditions);

        publisher.publishEvent(new DictChangeEvent(this, conf.getId(), DictEventEnum.ADD));


    }

    /**
     * 字典配置详情
     *
     * @param confId confId
     * @return 详情
     */
    public ReportDictDTO detailById(String confId) {
        ReportDictConf conf = Optional.ofNullable(getById(confId))
                .orElseThrow(() -> new BusinessException("1", "字典不存在或已被删除"));

        List<ReportDictCondition> conditions = reportDictConditionService.list(
                Wrappers.<ReportDictCondition>lambdaQuery().eq(ReportDictCondition::getDictConfId, confId));

        ReportDictDTO dto = new ReportDictDTO();
        BeanUtils.copyProperties(conf, dto);
        dto.setConditions(conditions);
        return dto;
    }

    public Page<ReportDictDTO> getPage(ReportDictDTO reportDictDTO) {
        Page<ReportDictConf> page = page(reportDictDTO, Wrappers.<ReportDictConf>lambdaQuery()
                .like(StrUtil.isNotBlank(reportDictDTO.getDictName()), ReportDictConf::getDictName,
                        SqlUtil.buildLikeValue(reportDictDTO.getDictName(), LikeType.Contains, false)));

        if (CollUtil.isEmpty(page.getRecords())) {
            return new Page<>();
        }

        List<Long> confId = page.getRecords().stream().map(ReportDictConf::getId)
                .collect(Collectors.toList());

        List<ReportDictCondition> conditions = reportDictConditionService.list(
                Wrappers.<ReportDictCondition>lambdaQuery().in(ReportDictCondition::getDictConfId, confId));

        Map<Long, List<ReportDictCondition>> conditionMap = conditions.stream()
                .collect(Collectors.groupingBy(ReportDictCondition::getDictConfId));


        return PageUtils.mapRecords(page, conf -> {
            ReportDictDTO dto = new ReportDictDTO();
            BeanUtils.copyProperties(conf, dto);
            dto.setConditions(conditionMap.get(conf.getId()));
            return dto;
        });
    }

    @Transactional(rollbackFor = Throwable.class)
    public void removeConf(String confId) {
        ReportDictConf conf = getById(confId);
        if (conf != null) {
            removeById(conf);
            reportDictConditionService.remove(Wrappers.<ReportDictCondition>lambdaQuery().eq(ReportDictCondition::getDictConfId, confId));
            publisher.publishEvent(new DictChangeEvent(this, conf.getId(), DictEventEnum.REMOVE));
        }
    }

    public List<ReportDictDTO> getList() {

        List<ReportDictConf> data = list();

        return data.stream().map(conf -> {
            ReportDictDTO dto = new ReportDictDTO();
            BeanUtils.copyProperties(conf, dto);
            return dto;
        }).collect(Collectors.toList());

    }
}
