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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunjian.datarelation.dto.ReportInfoDTO;
import com.yunjian.datarelation.dto.ReportMainInfoPageDTO;
import com.yunjian.datarelation.entity.ReportMainInfo;
import com.yunjian.datarelation.vo.ReportInfoVO;
import com.yunjian.datarelation.vo.ReportMainInfoVO;

import java.util.List;

/**
 * @author z7638
 * @description 针对表【report_main_info(报表主体信息表)】的数据库操作Service
 * @createDate 2024-07-03 11:27:38
 */
public interface ReportMainInfoService extends IService<ReportMainInfo> {

    /**
     * 保存报表信息
     *
     * @param reportInfoDTO 报表信息
     * @author fusheng
     * @date: 2024/7/3 上午11:48
     */
    Long saveInfo(ReportInfoDTO reportInfoDTO);

    void delete(Long reportId);

    ReportMainInfo info(Long reportId);

    ReportInfoVO getInfo(Long reportId);

    Page<ReportMainInfoVO> getPage(ReportMainInfoPageDTO reportMainInfoPageDTO);

    List<ReportMainInfoVO> listByAuth(ReportMainInfoPageDTO reportMainInfoPageDTO);

    Page<ReportMainInfoVO> listByAuthPage(ReportMainInfoPageDTO reportMainInfoPageDTO);

    Long saveAsInfo(ReportInfoDTO reportInfoDTO);

    void updateGroupAndName(ReportMainInfo reportInfoDTO);

}

