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

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.datarelation.dto.ETLFunGroupDTO;
import com.yunjian.datarelation.entity.ETLFun;
import com.yunjian.datarelation.entity.ETLFunGroup;
import com.yunjian.datarelation.mapper.ETLFunGroupMapper;
import com.yunjian.datarelation.mapper.ETLFunMapper;
import com.yunjian.datarelation.service.ETLFunGroupService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 函数分组
 * Created on 2024/1/3 09:39
 *
 * @author fusheng
 * @version 1.0
 */
@Service
public class ETLFunGroupServiceImpl extends ServiceImpl<ETLFunGroupMapper, ETLFunGroup> implements
        ETLFunGroupService {

    @Resource
    private ETLFunMapper etlFunMapper;


    @Override
    public List<ETLFunGroupDTO> getConvertFuncGroup() {
        List<ETLFunGroupDTO> etlFunGroups = baseMapper.getETLFunGroupList();
        if (etlFunGroups == null || etlFunGroups.isEmpty()) {
            return ListUtil.empty();
        }
        List<ETLFun> etlFuns = etlFunMapper.selectList(
                new LambdaQueryWrapper<ETLFun>().eq(ETLFun::getFunEnable, 1).orderByAsc(ETLFun::getSort));
        if (etlFuns != null && !etlFuns.isEmpty()) {
            Map<Long, List<ETLFun>> etlFunMap = etlFuns.stream()
                    .collect(Collectors.groupingBy(ETLFun::getGroupId));
            for (ETLFunGroupDTO etlFunGroup : etlFunGroups) {
                List<ETLFun> etlFunList = etlFunMap.get(etlFunGroup.getId());
                etlFunGroup.setFunList(etlFunList);
            }
        }
        return etlFunGroups;
    }
}
