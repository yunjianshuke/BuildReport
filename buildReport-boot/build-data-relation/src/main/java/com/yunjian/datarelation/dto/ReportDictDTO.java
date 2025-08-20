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
package com.yunjian.datarelation.dto;

import cn.hutool.core.collection.CollUtil;
import com.yunjian.common.base.BasePage;
import com.yunjian.datarelation.entity.ReportDictCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDictDTO extends BasePage {

    public interface PageGroup {
    }

    public interface UpdateGroup {
    }

    private Long id;
    /**
     * 字典所在数据源
     */
    @NotNull(groups = UpdateGroup.class, message = "数据源id不能为空")
    private Long datasourceId;
    /**
     * 表名称
     */
    @NotNull(groups = UpdateGroup.class, message = "表名称不能为空")
    private String tableName;
    /**
     * 字典名称
     */
    @NotBlank(groups = UpdateGroup.class, message = "字典名称不能为空")
    private String dictName;

    /**
     * 真实值字段
     */
    @NotBlank(groups = UpdateGroup.class, message = "字典值字段不能为空")
    private String dictValueField;

    /**
     * 显示值字段
     */
    @NotBlank(groups = UpdateGroup.class, message = "字典显示字段不能为空")
    private String dictLabelField;
    /**
     * 默认展示
     */
    private String defaultLabel;

    private List<ReportDictConditionDTO> conditionList;

    @Data
    public static class ReportDictConditionDTO {
        /**
         * 条件字段
         */
        private String conditionKey;
        /**
         * 值
         */
        private String conditionValue;
    }

    public boolean isUpdate() {
        return id != null;
    }

    public void setConditions(List<ReportDictCondition> conditions) {
        if (CollUtil.isNotEmpty(conditions)) {
            List<ReportDictConditionDTO> conditionDtoList = conditions.stream().map(condition -> {
                ReportDictConditionDTO reportDictConditionDTO = new ReportDictConditionDTO();
                BeanUtils.copyProperties(condition, reportDictConditionDTO);
                return reportDictConditionDTO;
            }).collect(Collectors.toList());
            this.setConditionList(conditionDtoList);
        } else {
            this.setConditionList(Collections.emptyList());
        }
    }
}
