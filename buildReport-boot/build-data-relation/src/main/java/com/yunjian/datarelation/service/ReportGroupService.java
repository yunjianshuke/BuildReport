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
import com.yunjian.datarelation.dto.ReportGroupDTO;
import com.yunjian.datarelation.entity.ReportGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunjian.datarelation.vo.ReportGroupVO;

import java.util.List;

/**
 * @author z7638
 * @description 针对表【report_group(业务分组表)】的数据库操作Service
 * @createDate 2024-07-05 14:19:15
 */
public interface ReportGroupService extends IService<ReportGroup> {

    List<Tree<Long>> getTree(ReportGroupDTO dto);

    int deleteByIdsOutTables(Long[] ids);

    Page<ReportGroupVO> getPage(ReportGroupDTO ReportGroup);

    public List<ReportGroupVO> getList(ReportGroupDTO ReportGroup);

    public ReportGroupVO getOne(ReportGroupDTO dto);

    public ReportGroupVO saveEntityWitchVO(ReportGroupDTO dto);

    public int update(ReportGroupDTO dto);
}
