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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.datarelation.dto.ReportEtlFunSymbolVO;
import com.yunjian.datarelation.entity.ReportEtlFunSymbol;
import com.yunjian.datarelation.mapper.ReportEtlFunSymbolMapper;
import com.yunjian.datarelation.service.ReportEtlFunSymbolService;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @description: 函数常用符号
 * @version: 1.0.0
 * @author: caolg
 * @date: 2024/01/05
 **/
@Slf4j
@Service
public class ReportEtlFunSymbolServiceImpl extends
        ServiceImpl<ReportEtlFunSymbolMapper, ReportEtlFunSymbol> implements ReportEtlFunSymbolService {

    @Resource
    ReportEtlFunSymbolMapper reportEtlFunSymbolMapper;


    @Override
    public List<ReportEtlFunSymbolVO> symbollist() {
        LambdaQueryWrapper<ReportEtlFunSymbol> queryWrapper = Wrappers.<ReportEtlFunSymbol>lambdaQuery();
        queryWrapper.eq(ReportEtlFunSymbol::getDelFlag, "0");
        queryWrapper.eq(ReportEtlFunSymbol::getSymbolEnable, "1");
        queryWrapper.orderByAsc(ReportEtlFunSymbol::getSort);
        return entityToVo(this.reportEtlFunSymbolMapper.selectList(queryWrapper));
    }

    private List<ReportEtlFunSymbolVO> entityToVo(List<ReportEtlFunSymbol> list) {
        return list.stream().map(map -> {
            ReportEtlFunSymbolVO reportEtlFunSymbolVO = new ReportEtlFunSymbolVO();
            BeanUtil.copyProperties(map, reportEtlFunSymbolVO);
            return reportEtlFunSymbolVO;
        }).collect(Collectors.toList());
    }
}
