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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.yunjian.common.base.BasePage;
import com.yunjian.common.group.HoldUpdateGroup;
import com.yunjian.common.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 业务分组表
 *
 * @author build code generator
 * @date 2023-09-05 10:06:00
 */
@Data
@Schema(description = "业务分组表传输对象")
public class ReportGroupDTO extends BasePage {

    /**
     * ID
     */
    @NotNull(groups = {UpdateGroup.class, HoldUpdateGroup.class}, message = "ID不能为空")
    @Schema(description = "ID")
    private Long id;

    /**
     * 业务分组名
     */
    @Schema(description = "业务分组名")
    private String bizGroupName;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private Integer seq;

    /**
     * 父ID
     */
    @Schema(description = "父ID")
    private Long parentId;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 是否删除;(是:1 0:否)
     */
    @Schema(description = "是否删除;(是:1 0:否)")
    private String delFlag;

    /**
     * 创建时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建人Name")
    private String createByName;
    /**
     * 更新时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新人Name")
    private String updateByName;
    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 租户号
     */
    @Schema(description = "租户号", hidden = true)
    private Long tenantId;

    /**
     * 业务分组code
     */
    @Schema(description = "业务分组code")
    private String bizGroupCode;

}
