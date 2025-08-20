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
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.common.exception.ErrorCodeEnum;
import com.yunjian.datarelation.cache.DictCache;
import com.yunjian.datarelation.cache.DictCache.Dict;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.dto.DatasourceDTO;
import com.yunjian.datarelation.dto.DatasourcePageDTO;
import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.dto.TableDTO;
import com.yunjian.datarelation.entity.Datasource;
import com.yunjian.datarelation.entity.ReportBusinessNode;
import com.yunjian.datarelation.entity.ReportBusinessRelationInfo;
import com.yunjian.datarelation.enums.DatasourceEventEnum;
import com.yunjian.datarelation.event.DatasourceChangeEvent;
import com.yunjian.datarelation.mapper.DatasourceMapper;
import com.yunjian.datarelation.pool.DatasourcePoolManager;
import com.yunjian.datarelation.pool.Ds;
import com.yunjian.datarelation.service.DatasourceService;
import com.yunjian.datarelation.service.ReportBusinessRelationInfoService;
import com.yunjian.datarelation.utils.PageUtils;
import com.yunjian.datarelation.vo.BusinessRelationInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
@Service
@RequiredArgsConstructor
public class DatasourceServiceImpl extends ServiceImpl<DatasourceMapper, Datasource> implements
        DatasourceService, InitializingBean {

    private final ApplicationEventPublisher publisher;


    @Resource
    private DatasourcePoolManager datasourcePoolManager;
    @Resource
    private ReportBusinessRelationInfoService reportBusinessRelationInfoService;
    @Resource
    private DictCache dictCache;


    @Override
    public Page<DatasourceDTO> getPage(DatasourcePageDTO pageDTO) {
        Page<Datasource> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        page = page(page, getDatasourceQueryWrapper(pageDTO));

        return PageUtils.mapRecords(page, ds -> convert.apply(ds, true));
    }

    @Override
    public List<DatasourceDTO> getList(DatasourcePageDTO pageDTO, boolean back) {
        List<Datasource> list = list(getDatasourceQueryWrapper(pageDTO));
        return list.stream().map(ds -> {
            DatasourceDTO apply = convert.apply(ds, false);
            if (!back) {
                apply.setOptions(null);
            }
            return apply;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AnyData> preview(Long datasourceId, String tableName) {
        Ds ds = getDatasourceFromPool(datasourceId);

        return ds.privew(tableName, 100);
    }

    @Override
    public List<TableDTO> getTablesByRelationId(Long relationId) {
        ReportBusinessRelationInfo reportBusinessRelationInfo = new ReportBusinessRelationInfo();
        reportBusinessRelationInfo.setRelationId(relationId);
        BusinessRelationInfoVO vo = reportBusinessRelationInfoService.getNodeAndEdgeByRelationId(
                reportBusinessRelationInfo);


        Set<Long> dictId = new HashSet<>();
        List<TableDTO> data = new ArrayList<>();
        for (ReportBusinessNode reportBusinessNode : vo.getNodeList()) {
            JSONArray fields = JSONUtil.parseArray(reportBusinessNode.getComponentJson());
            for (Object field : fields) {
                JSONObject json = (JSONObject) field;
                String dictConfId = json.getStr("dictConfId");
                if (StrUtil.isNotBlank(dictConfId)) {
                    dictId.add(Long.valueOf(json.getStr("dictConfId")));
                }
            }
            data.add(new TableDTO(reportBusinessNode.getTableName(), reportBusinessNode.getCommentName(), reportBusinessNode.getDatasourceId(), reportBusinessNode.getDatasourceName()));
        }
        if (CollUtil.isNotEmpty(dictId)) {
            for (Long id : dictId) {
                Dict dict = dictCache.getDict(id);
                if (dict != null) {
                    data.add(new TableDTO(dict.getTableName(), "", dict.getDatasourceId(), dict.getDatasourceName()));
                }
            }
        }

        return data.stream().distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void createDatasource(DatasourceDTO dto) {
        Datasource datasource = new Datasource();
        BeanUtils.copyProperties(dto, datasource, "id");
        boolean exist = count(Wrappers.<Datasource>lambdaQuery()
                .eq(Datasource::getName, dto.getName())) > 0;

        if (exist) {
            throw new BusinessException(ErrorCodeEnum.SERVER_ERROR.errCode, "数据源名称重复");
        }

        boolean ok = datasource.getOptions().testConnection();

        if (!ok) {
            throw new BusinessException(ErrorCodeEnum.SERVER_ERROR.errCode, "测试连接失败,请检查配置");
        }

        save(datasource);

        publisher.publishEvent(new DatasourceChangeEvent(this, datasource, DatasourceEventEnum.ADD));

    }

    @Override
    public DatasourceDTO detail(Long id) {
        Datasource datasource =
                Optional.ofNullable(getById(id))
                        .orElseThrow(() -> new BusinessException("0", "数据源不存在"));

        DatasourceDTO dto = new DatasourceDTO();

        BeanUtils.copyProperties(datasource, dto);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteDatasource(Long id) {
        removeById(id);
        publisher.publishEvent(new DatasourceChangeEvent(this, id, DatasourceEventEnum.REMOVE));
    }

    @Override
    public List<TableDTO> getTablesByDatasourceId(Long datasourceId, String tableName) {
        Ds ds = getDatasourceFromPool(datasourceId);
        return ds.allTables(tableName);
    }

    @Override
    public List<DbFieldDTO> getTableFieldsByDatasourceId(Long datasourceId, String tableName) {
        Ds ds = getDatasourceFromPool(datasourceId);
        return ds.tableFields(tableName);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateDatasource(Long id, DatasourceDTO dto) {
        Long datasourceId = Optional.ofNullable(id)
                .orElseThrow(() -> new BusinessException("0", "数据源id不能为空"));

        Datasource datasource = getById(datasourceId);

        BeanUtils.copyProperties(dto, datasource, "id");

        boolean ok = datasource.getOptions().testConnection();

        if (!ok) {
            throw new BusinessException(ErrorCodeEnum.SERVER_ERROR.errCode, "测试连接失败,请检查配置");
        }

        updateById(datasource);

        publisher.publishEvent(new DatasourceChangeEvent(this, datasource, DatasourceEventEnum.UPDATE));

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        datasourcePoolManager.init(this);
    }

    /**
     * 格式转换
     */
    BiFunction<Datasource, Boolean, DatasourceDTO> convert = (ds, test) -> {
        DatasourceDTO dto = ds.convert();
        datasourcePoolManager.getDatasource(ds.getId())
                .ifPresent(d -> dto.setStatus(d.isHealthStatus()));

        return dto;
    };

    public LambdaQueryWrapper<Datasource> getDatasourceQueryWrapper(DatasourcePageDTO pageDTO) {
        LambdaQueryWrapper<Datasource> wrappers = Wrappers.lambdaQuery();
        wrappers.like(StrUtil.isNotBlank(pageDTO.getName()), Datasource::getName,
                SqlUtils.concatLike(pageDTO.getName(),
                        SqlLike.DEFAULT));
        wrappers.eq(ObjectUtil.isNotNull(pageDTO.getDatasourceType()), Datasource::getType,
                pageDTO.getDatasourceType());
        return wrappers;
    }

    /**
     * 从数据源池中获取数据源
     *
     * @param datasourceId 数据源id
     * @return 数据源对象
     */
    public Ds getDatasourceFromPool(Long datasourceId) {
        return datasourcePoolManager.getDatasource(datasourceId)
                .orElseThrow(
                        () -> {
                            Datasource datasource = getById(datasourceId);
                            if (datasource == null) {
                                return new BusinessException("0",
                                        "数据源 【" + datasourceId + "】 不存在或已被删除");
                            }
                            return new BusinessException("0",
                                    "数据源 【" + datasource.getName()
                                            + "】 无法连接,请联系开发检查数据库是否过期或密码被修改");
                        });
    }
}
