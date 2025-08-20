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
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.common.exception.ErrorCodeEnum;
import com.yunjian.common.util.RegexUtils;
import com.yunjian.datarelation.dto.ReportGroupDTO;
import com.yunjian.datarelation.entity.ReportGroup;
import com.yunjian.datarelation.entity.ReportMainInfo;
import com.yunjian.datarelation.mapper.ReportGroupMapper;
import com.yunjian.datarelation.service.ReportGroupService;
import com.yunjian.datarelation.vo.ReportGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author z7638
 * @description 针对表【report_group(业务分组表)】的数据库操作Service实现
 * @createDate 2024-07-05 14:19:15
 */
@Service
public class ReportGroupServiceImpl extends ServiceImpl<ReportGroupMapper, ReportGroup>
        implements ReportGroupService {

    @Resource
    ReportGroupMapper reportGroupMapper;

    /**
     * 分页查询
     *
     * @param ReportGroupDTO page  业务分组表
     * @return
     */
    @Override
    public Page<ReportGroupVO> getPage(ReportGroupDTO ReportGroupDTO) {
        Page page = this.getPageOfDtoToPage(ReportGroupDTO);
        // step1：封装查询条件查询结果集
        LambdaQueryWrapper<ReportGroup> queryWrapper = Wrappers.<ReportGroup>lambdaQuery();
        queryWrapper.eq(ReportGroup::getDelFlag, "0");
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getId()), ReportGroup::getId,
                ReportGroupDTO.getId());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getBizGroupName()),
                ReportGroup::getBizGroupName,
                ReportGroupDTO.getBizGroupName());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getSeq()), ReportGroup::getSeq,
                ReportGroupDTO.getSeq());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getParentId()), ReportGroup::getParentId,
                ReportGroupDTO.getParentId());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getProjectId()), ReportGroup::getProjectId,
                ReportGroupDTO.getProjectId());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getDelFlag()), ReportGroup::getDelFlag,
                ReportGroupDTO.getDelFlag());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getCreateTime()), ReportGroup::getCreateTime,
                ReportGroupDTO.getCreateTime());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getCreateBy()), ReportGroup::getCreateBy,
                ReportGroupDTO.getCreateBy());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getUpdateTime()), ReportGroup::getUpdateTime,
                ReportGroupDTO.getUpdateTime());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getUpdateBy()), ReportGroup::getUpdateBy,
                ReportGroupDTO.getUpdateBy());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getVersion()), ReportGroup::getVersion,
                ReportGroupDTO.getVersion());

        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getBizGroupCode()),
                ReportGroup::getBizGroupCode,
                ReportGroupDTO.getBizGroupCode());

        // step2：处理多字段排序并校验是否是非法参数
        List<OrderItem> orderItemList = page.getOrders();
        RegexUtils.verifyPageFileld(orderItemList);
        if (CollUtil.isNotEmpty(orderItemList)) {
            Map<String, Object> ReportGroupMap = BeanUtil.beanToMap(new ReportGroup());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < orderItemList.size(); i++) {
                if (ReportGroupMap.containsKey(orderItemList.get(i).getColumn())) {
                    sb.append(StrUtil.toUnderlineCase(orderItemList.get(i).getColumn())).append(" ").append(
                            orderItemList.get(i).isAsc() ? "asc" : "desc").append(",");
                }
            }
            if (StrUtil.isNotBlank(sb.toString())) {
                queryWrapper.last("order by ".concat(StrUtil.removeSuffix(sb.toString(), ",")));
            }
            page.setOrders(new ArrayList<>());
        }

        // step3: 返回查询结果
        Page pageEntity = page(page, queryWrapper);

        return getPageOfEntityToVo(pageEntity);
    }

    private Page getPageOfDtoToPage(ReportGroupDTO dto) {
        return BeanUtil.copyProperties(dto, Page.class);
    }

    /**
     * 设计时含有虚拟列处理，生成代码时默认实现改方法
     *
     * @param page 业务分组表
     * @return
     */
    //	@Override
    private Page<ReportGroupVO> getPageOfEntityToVo(Page page) {
        return (Page) page.convert(e -> {
            ReportGroupVO ReportGroupVO = new ReportGroupVO();
            BeanUtil.copyProperties(e, ReportGroupVO);
            return ReportGroupVO;
        });
    }

    /**
     * 查询集合
     *
     * @param ReportGroupDTO 业务分组表
     * @return
     */
    @Override
    public List<ReportGroupVO> getList(ReportGroupDTO ReportGroupDTO) {
        // step1：封装查询条件查询结果集
        LambdaQueryWrapper<ReportGroup> queryWrapper = Wrappers.<ReportGroup>lambdaQuery();
        queryWrapper.eq(ReportGroup::getDelFlag, "0");
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getId()), ReportGroup::getId,
                ReportGroupDTO.getId());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getBizGroupName()),
                ReportGroup::getBizGroupName,
                ReportGroupDTO.getBizGroupName());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getSeq()), ReportGroup::getSeq,
                ReportGroupDTO.getSeq());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getParentId()), ReportGroup::getParentId,
                ReportGroupDTO.getParentId());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getProjectId()), ReportGroup::getProjectId,
                ReportGroupDTO.getProjectId());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getDelFlag()), ReportGroup::getDelFlag,
                ReportGroupDTO.getDelFlag());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getCreateTime()), ReportGroup::getCreateTime,
                ReportGroupDTO.getCreateTime());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getCreateBy()), ReportGroup::getCreateBy,
                ReportGroupDTO.getCreateBy());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getUpdateTime()), ReportGroup::getUpdateTime,
                ReportGroupDTO.getUpdateTime());
        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getUpdateBy()), ReportGroup::getUpdateBy,
                ReportGroupDTO.getUpdateBy());
        queryWrapper.eq(Objects.nonNull(ReportGroupDTO.getVersion()), ReportGroup::getVersion,
                ReportGroupDTO.getVersion());

        queryWrapper.eq(StrUtil.isNotBlank(ReportGroupDTO.getBizGroupCode()),
                ReportGroup::getBizGroupCode,
                ReportGroupDTO.getBizGroupCode());

        // step2：处理多字段排序并校验是否是非法参数
        List<OrderItem> orderItemList = ReportGroupDTO.getOrders();
        RegexUtils.verifyPageFileld(orderItemList);
        if (CollUtil.isNotEmpty(orderItemList)) {
            Map<String, Object> ReportGroupMap = BeanUtil.beanToMap(new ReportGroup());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < orderItemList.size(); i++) {
                if (ReportGroupMap.containsKey(orderItemList.get(i).getColumn())) {
                    sb.append(StrUtil.toUnderlineCase(orderItemList.get(i).getColumn())).append(" ").append(
                            orderItemList.get(i).isAsc() ? "asc" : "desc").append(",");
                }
            }
            if (StrUtil.isNotBlank(sb.toString())) {
                queryWrapper.last("order by ".concat(StrUtil.removeSuffix(sb.toString(), ",")));
            }
            ReportGroupDTO.setOrders(new ArrayList<>());
        }

        // step3: 返回查询结果
        List<ReportGroup> ReportGroups = list(queryWrapper);
        return getListOfEntityToVo(ReportGroups);
    }

    /**
     * 查询list entityTOVO
     *
     * @param list 业务分组表
     * @return
     */
    //	@Override
    private List<ReportGroupVO> getListOfEntityToVo(List<ReportGroup> list) {
        List<ReportGroupVO> ReportGroupVOList = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            list.stream().forEach(o -> {
                ReportGroupVO ReportGroupVO = new ReportGroupVO();
                BeanUtil.copyProperties(o, ReportGroupVO);
                ReportGroupVOList.add(ReportGroupVO);
            });
        }
        return ReportGroupVOList;
    }

    /**
     * 查询ONE entityTOVO
     *
     * @param ReportGroup 业务分组表
     * @return
     */
    //	@Override
    public ReportGroupVO getOneOfEntityToVo(ReportGroup ReportGroup) {
        ReportGroupVO ReportGroupVO = new ReportGroupVO();
        BeanUtil.copyProperties(ReportGroup, ReportGroupVO);
        return ReportGroupVO;
    }

    /**
     * 查询一条
     *
     * @param dto 业务分组表
     * @return
     */
    @Override
    public ReportGroupVO getOne(ReportGroupDTO dto) {
        // step1: 封装查询条件
        LambdaQueryWrapper<ReportGroup> queryWrapper = Wrappers.<ReportGroup>lambdaQuery();
        if (Objects.isNull(dto.getId()) || dto.getId() == 0) {
            throw new BusinessException(ErrorCodeEnum.UNIQUE_PARAM_ERROR);
        }
        queryWrapper.eq(Objects.nonNull(dto.getId()), ReportGroup::getId, dto.getId());
        queryWrapper.eq(ReportGroup::getDelFlag, "0");
        // step2: 查询结果转换成vo返回
        ReportGroup entityResult = this.reportGroupMapper.selectOne(queryWrapper);
        return getOneOfEntityToVo(entityResult);
    }

    /**
     * 新增dto转entity
     *
     * @param ReportGroupDTO 业务分组表
     * @return
     */
    //	@Override
    protected ReportGroup saveEntityOfDtoToEntity(ReportGroupDTO ReportGroupDTO) {
        ReportGroup ReportGroup = new ReportGroup();
        BeanUtil.copyProperties(ReportGroupDTO, ReportGroup);
        return ReportGroup;
    }

    /**
     * 新增
     *
     * @param dto 业务分组表
     * @return
     */
    @Override
    public ReportGroupVO saveEntityWitchVO(ReportGroupDTO dto) {
        ReportGroup reportGroup = reportGroupMapper.selectOne(new LambdaQueryWrapper<ReportGroup>()
                .eq(ReportGroup::getBizGroupCode, dto.getBizGroupCode())
                .eq(ReportGroup::getDelFlag, "0")
        );
        if (Objects.nonNull(reportGroup)) {
            throw new BusinessException(ErrorCodeEnum.OPERATION_ERROR.errCode, "分组编码已存在");
        }
        ReportGroup ReportGroup = saveEntityOfDtoToEntity(dto);
        if (Objects.isNull(ReportGroup) || StrUtil.isBlank(ReportGroup.getBizGroupCode())
                || StrUtil.isBlank(
                ReportGroup.getBizGroupName())) {
            throw new BusinessException(ErrorCodeEnum.OPERATION_ERROR);
        }
        int result = this.reportGroupMapper.insert(ReportGroup);
        if (result > 0) {
            ReportGroupVO vo = new ReportGroupVO();
            BeanUtil.copyProperties(ReportGroup, vo);
            return vo;
        }
        return null;
    }

    /**
     * 修改dto转entity
     *
     * @param ReportGroupDTO 业务分组表
     * @return
     */
    //	@Override
    protected ReportGroup updateOfDtoToEntity(ReportGroupDTO ReportGroupDTO) {
        ReportGroup ReportGroup = new ReportGroup();
        BeanUtil.copyProperties(ReportGroupDTO, ReportGroup);
        return ReportGroup;
    }

    /**
     * 修改
     *
     * @param dto 业务分组表
     * @return
     */
    @Override
    public int update(ReportGroupDTO dto) {
        ReportGroup ReportGroup = updateOfDtoToEntity(dto);
        if (Objects.isNull(ReportGroup.getId()) || ReportGroup.getId() == 0) {
            throw new BusinessException(ErrorCodeEnum.UPDATE_PARAM_ERROR);
        }
        ReportGroup ReportGroupOld = this.reportGroupMapper.selectById(ReportGroup.getId());
        if (Objects.isNull(ReportGroupOld)) {
            throw new BusinessException(ErrorCodeEnum.UPDATE_DATA_ERROR);
        }
        return this.reportGroupMapper.updateById(ReportGroup);
    }

    /**
     * 删除
     *
     * @param ids 业务分组表
     * @return
     */
    //	@Override
    protected int executeDeleteByIds(Long[] ids) {
        if (Objects.isNull(ids) || ids.length == 0) {
            throw new BusinessException(ErrorCodeEnum.DELETE_PARAM_ERROR);
        }
        return this.reportGroupMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public List<Tree<Long>> getTree(ReportGroupDTO dto) {
        List<ReportGroupVO> list = getList(dto);
        List<TreeNode<Long>> collect = list.stream().map(getNodeFunction())
                .collect(Collectors.toList());
        return TreeUtil.build(collect, 0L);
    }

    @Autowired
    @Lazy
    private ReportMainInfoService reportManagerService;


    @Override
    public int deleteByIdsOutTables(Long[] ids) {

        if (Objects.isNull(ids) || ids.length == 0) {
            throw new BusinessException(ErrorCodeEnum.DELETE_PARAM_ERROR);
        }

        for (Long id : ids) {
            checkGroupCanBeDeleted(id);
        }
        return reportGroupMapper.deleteBatchIds(CollUtil.toList(ids));
    }

    private void checkGroupCanBeDeleted(Long id) {

        LambdaQueryWrapper<ReportGroup> groupQueryWrapper = Wrappers.<ReportGroup>lambdaQuery();
        groupQueryWrapper.eq(ReportGroup::getDelFlag, "0");
        groupQueryWrapper.eq(ReportGroup::getParentId, id);

        Long childGroupCount = this.reportGroupMapper.selectCount(groupQueryWrapper);

        LambdaQueryWrapper<ReportMainInfo> designQueryWrapper = Wrappers.<ReportMainInfo>lambdaQuery();
        designQueryWrapper.eq(ReportMainInfo::getDelFlag, "0");
        designQueryWrapper.eq(ReportMainInfo::getGroupId, id);

        Long childTableDesignCount = this.reportManagerService.count(designQueryWrapper);
        if (childGroupCount > 0 || childTableDesignCount > 0) {
            throw new BusinessException("0", "分组下存在报表，无法删除分组");
        }
    }


    /**
     * 封装树结构
     *
     * @return
     */
    private Function<ReportGroupVO, TreeNode<Long>> getNodeFunction() {
        return dbGroup -> {
            TreeNode<Long> node = new TreeNode<>();

            node.setId(dbGroup.getId());
            node.setName(dbGroup.getBizGroupName());
            node.setParentId(Objects.isNull(dbGroup.getParentId()) ? 0L : dbGroup.getParentId());
            node.setWeight(dbGroup.getSeq());

            Map<String, Object> extra = new HashMap<>();
            extra.put("bizGroupName", dbGroup.getBizGroupName());
            extra.put("bizGroupCode", dbGroup.getBizGroupCode());
            extra.put("projectId", dbGroup.getProjectId());
            extra.put("tenantId", dbGroup.getTenantId());

            node.setExtra(extra);
            return node;
        };
    }
}




