

CREATE TABLE `report2_business_group` (
                                          `id` bigint NOT NULL COMMENT 'ID',
                                          `biz_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务关系分组名',
                                          `seq` int DEFAULT NULL COMMENT '排序字段',
                                          `parent_id` bigint DEFAULT NULL COMMENT '父ID',
                                          `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否删除;(是:1 0:否)',
                                          `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                          `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                          `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                          `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                          `version` int DEFAULT NULL COMMENT '版本号',
                                          `tenant_id` bigint DEFAULT NULL COMMENT '租户号',
                                          `biz_group_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组编号',
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `report2_business_node` (
                                         `id` bigint NOT NULL COMMENT '主键',
                                         `relation_id` bigint DEFAULT NULL COMMENT '业务关系id',
                                         `cell_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '节点id',
                                         `gui_x` int DEFAULT NULL COMMENT '节点坐标x',
                                         `gui_y` int DEFAULT NULL COMMENT '节点坐标y',
                                         `datasource_id` bigint DEFAULT NULL COMMENT '数据源id',
                                         `table_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表名称',
                                         `alias_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                         `comment_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据库备注表名',
                                         `component_json` json DEFAULT NULL COMMENT '组件json',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新',
                                         `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                         `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                         `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                         `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                         `version` int DEFAULT NULL,
                                         `hidden` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '是否隐藏',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;


CREATE TABLE `report2_business_node_relation` (
                                                  `id` bigint NOT NULL COMMENT '主键',
                                                  `relation_id` bigint NOT NULL COMMENT '业务关系id',
                                                  `edge_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '前端id',
                                                  `source_cell_node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '当前节点id',
                                                  `target_cell_node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标节点id',
                                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新',
                                                  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                                  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                                  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                                  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                                  `version` int DEFAULT NULL,
                                                  `component_json` json DEFAULT NULL,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;


CREATE TABLE `report2_business_relation_info` (
                                                  `relation_id` bigint NOT NULL COMMENT '主键',
                                                  `biz_group_id` bigint DEFAULT NULL COMMENT '分组id',
                                                  `relation_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '关系名称',
                                                  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标识',
                                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
                                                  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '更新人',
                                                  `tenant_id` bigint DEFAULT NULL,
                                                  `version` int DEFAULT NULL,
                                                  PRIMARY KEY (`relation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='报表业务关系图';


CREATE TABLE `report2_data_set` (
                                    `id` bigint NOT NULL COMMENT '主键',
                                    `business_type` int DEFAULT NULL COMMENT '数据集类型。0是设计器，1是筛选器',
                                    `report_id` bigint DEFAULT NULL COMMENT '报表id',
                                    `type` int DEFAULT '0' COMMENT '数据集类型，0是数据集，1是单表',
                                    `relation_id` bigint NOT NULL COMMENT '业务关系ID',
                                    `data_filter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '数据过滤',
                                    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据集名称',
                                    `field_json_array` json NOT NULL COMMENT '数据集字段',
                                    `create_time` datetime NOT NULL COMMENT '创建时间',
                                    `update_time` datetime NOT NULL COMMENT '更新时间',
                                    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                    `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                    `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                    `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                    `version` int DEFAULT NULL COMMENT '版本号',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='数据集表';




CREATE TABLE `report2_data_set_relation` (
                                             `id` bigint NOT NULL COMMENT '主键',
                                             `report_id` bigint NOT NULL COMMENT '报表id',
                                             `data_set_id` bigint NOT NULL DEFAULT '0' COMMENT '数据集id',
                                             `create_time` datetime NOT NULL COMMENT '创建时间',
                                             `update_time` datetime NOT NULL COMMENT '更新时间',
                                             `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                             `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                             `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                             `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                             `version` int DEFAULT NULL COMMENT '版本号',
                                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `report2_data_set_v2` (
                                       `id` bigint NOT NULL COMMENT '主键',
                                       `business_type` int DEFAULT NULL COMMENT '数据集类型。0是设计器，1是筛选器',
                                       `type` int DEFAULT '0' COMMENT '数据集类型，0是数据集，1是单表',
                                       `relation_id` bigint NOT NULL COMMENT '业务关系ID',
                                       `data_filter` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '数据过滤',
                                       `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据集名称',
                                       `field_json_array` json DEFAULT NULL COMMENT '数据集字段',
                                       `create_time` datetime NOT NULL COMMENT '创建时间',
                                       `update_time` datetime NOT NULL COMMENT '更新时间',
                                       `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                       `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                       `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                       `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                       `version` int DEFAULT NULL COMMENT '版本号',
                                       `data_source_id` bigint DEFAULT NULL COMMENT '数据源id',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据集表';


CREATE TABLE `report2_datasource` (
                                      `id` bigint NOT NULL COMMENT '主键',
                                      `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '别名',
                                      `type` int NOT NULL COMMENT '数据库类型0mysql 1 oracle',
                                      `options` json NOT NULL COMMENT 'db连接信息',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新',
                                      `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                      `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                      `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                      `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                      `version` int DEFAULT NULL,
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;


CREATE TABLE `report2_dict_condition` (
                                          `id` bigint NOT NULL COMMENT '主键',
                                          `dict_conf_id` bigint NOT NULL COMMENT '字典配置id',
                                          `condition_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件字段',
                                          `condition_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件值',
                                          `create_time` datetime NOT NULL COMMENT '创建时间',
                                          `update_time` datetime NOT NULL COMMENT '更新时间',
                                          `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                          `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                          `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                          `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                          `version` int DEFAULT NULL COMMENT '版本号',
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `report2_dict_conf` (
                                     `id` bigint NOT NULL COMMENT '主键',
                                     `datasource_id` bigint NOT NULL COMMENT '数据所在数据源',
                                     `table_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表名',
                                     `dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
                                     `dict_value_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典值字段',
                                     `dict_label_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段标签字段',
                                     `default_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '默认展示值',
                                     `create_time` datetime NOT NULL COMMENT '创建时间',
                                     `update_time` datetime NOT NULL COMMENT '更新时间',
                                     `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                     `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                     `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                     `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                     `version` int DEFAULT NULL COMMENT '版本号',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



CREATE TABLE `report2_etl_fun` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                   `fun_name` varchar(90) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '函数名称',
                                   `fun_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '函数代码',
                                   `fun_description` varchar(900) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组描述',
                                   `fun_enable` int DEFAULT '1' COMMENT '分组启用标识,1启用，2禁用',
                                   `group_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '函数所属组ID',
                                   `introduce` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '介绍',
                                   `instruct` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用法',
                                   `example` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '示例',
                                   `sort` int DEFAULT NULL COMMENT '排序',
                                   `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `tenant_id` bigint DEFAULT NULL COMMENT '租户号',
                                   `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0',
                                   `version` int DEFAULT NULL,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='函数';


CREATE TABLE `report2_etl_fun_group` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                         `group_name` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组名称',
                                         `group_description` varchar(900) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组描述',
                                         `group_enable` int DEFAULT '1' COMMENT '分组启用标识,1启用，2禁用',
                                         `sort` int DEFAULT NULL COMMENT '排序',
                                         `version` int DEFAULT '0' COMMENT '版本号',
                                         `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `tenant_id` bigint DEFAULT NULL COMMENT '租户号',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='函数分组';


CREATE TABLE `report2_etl_fun_symbol` (
                                          `id` bigint NOT NULL COMMENT '主键',
                                          `symbol_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '符号名称',
                                          `symbol_code` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '符号代码',
                                          `symbol_description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '符号描述',
                                          `sort` int DEFAULT NULL COMMENT '排序',
                                          `symbol_enable` int DEFAULT NULL COMMENT '启用标识,1启用，2禁用',
                                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新',
                                          `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                          `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                          `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                          `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                          `version` int DEFAULT NULL,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='函数常用符号';




CREATE TABLE `report2_group` (
                                 `id` bigint NOT NULL COMMENT 'ID',
                                 `biz_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务分组名',
                                 `seq` int DEFAULT NULL COMMENT '排序字段',
                                 `parent_id` bigint DEFAULT NULL COMMENT '父ID',
                                 `project_id` bigint DEFAULT NULL COMMENT '项目ID',
                                 `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否删除;(是:1 0:否)',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                 `version` int DEFAULT NULL COMMENT '版本号',
                                 `tenant_id` bigint DEFAULT NULL COMMENT '租户号',
                                 `biz_group_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组编号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;


CREATE TABLE `report2_main_info` (
                                     `report_id` bigint NOT NULL COMMENT '主键',
                                     `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报表名称',
                                     `component_json` json NOT NULL COMMENT '报表组件',
                                     `filter_components` json DEFAULT NULL COMMENT '条件搜索',
                                     `group_id` bigint DEFAULT NULL COMMENT '分组ID',
                                     `create_time` datetime NOT NULL COMMENT '创建时间',
                                     `update_time` datetime NOT NULL COMMENT '更新时间',
                                     `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
                                     `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                     `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                     `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                     `version` int DEFAULT NULL COMMENT '版本号',
                                     PRIMARY KEY (`report_id`),
                                     KEY `idx_report_main_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='报表主体信息表';



INSERT INTO report2_etl_fun_symbol (id,symbol_name,symbol_code,symbol_description,sort,symbol_enable,create_time,update_time,del_flag,tenant_id,create_by,update_by,version) VALUES
(1,'>','>','大于号',1,1,'2023-12-28 14:42:45','2024-01-05 09:54:21','0',NULL,'1681141178533486593','1681141178533486593',1),
(2,'<','<','小于号',2,1,'2023-12-28 14:42:45','2024-01-05 09:54:21','0',NULL,'1681141178533486593','1681141178533486593',1),
(3,'=','=','等号',3,1,'2023-12-28 16:51:05','2024-01-05 09:54:21','0',NULL,'1681589387848413185','1681141178533486593',1),
(4,'==','==','双等号',4,1,'2023-12-29 09:41:37','2024-01-05 09:54:21','0',NULL,'1681141178533486593','1681141178533486593',1),
(5,'<=','<=','小于等于',5,1,'2024-01-02 10:16:03','2024-01-05 09:54:21','0',NULL,'1681141178533486593','1681141178533486593',1),
(6,'>=','>=','大于等于',6,1,'2024-01-02 10:24:31','2024-01-05 09:54:21','0',NULL,'1689476805888958465','1681141178533486593',1),
(7,'!=','!=','非等',7,1,'2024-01-02 10:27:26','2024-01-05 09:54:21','0',NULL,'1689476805888958465','1681141178533486593',1),
(8,'if/else','if(1==1){
     /* 输出值需要使用return语句 */
    return "a";
}else{
     /* 输出值需要使用return语句 */
    return "b";
}','判断',8,1,'2024-01-02 14:06:00','2024-01-22 09:24:20','0',NULL,'1681141178533486593','1681141178533486593',1),
(9,'for','// 要遍历的集合
var list=["a","b","c"];
// 返回信息
var res="";
// 循环代码块
for(var s:list){
    // 业务操作代码
    res=res+s+"|";
}
// 最终返回信息
return res;','循环',9,1,'2024-01-02 14:06:00','2024-01-22 09:22:57','0',NULL,'1681141178533486593','1681141178533486593',1),
(10,'return','return','返回',10,1,'2024-01-02 14:06:00','2024-01-05 09:54:21','0',NULL,'1681141178533486593','1681141178533486593',1);

INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(1, 'tcUtil.toLong() 文本转换整数', 'tcUtil.toLong()', '该函数将输入文本转换成整数输出。 参数radix指定输出的进制。若输入不是整数格式则转换失败。 arg是要转换为long的值。 如果参数为null，则函数返回null', 1, '1', '该函数将输入文本转换成整数输出。若输入不是整数格式则转换失败。 value是要转换为long的值，如果value为""或者null。返回defaultValue值；
<br/><b style="color:red">注意:defaultValue必须指定类型，例如：4，需要写成4L</b>', 'var res = tcUtil.toLong(Object value); <br/>
var res = tcUtil.toLong(Object value, Long defaultValue);', '函数 tcUtil.toLong("123456789012")输出123456789012 <br/>
函数 tcUtil.toLong("123.45")输出123。 <br/>
函数 tcUtil.toLong(null, 8L) 输出 8。<br/>
函数 tcUtil.toLong("", 8L) 输出 8。<br/>', 1, '', '2024-01-03 03:01:55', '', '2024-01-08 07:43:40', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(2, 'tcUtil.toDouble() 文本转换数值', 'tcUtil.toDouble()', '该函数将输入字符串转换成数值。 arg是要转换为double的字符串。 如果参数为null，则函数返回null。 用户也可以通过参数format来指定格式。', 1, '1', '该函数将输入字符串转换成数值。 value是要转换为double的值。 如果value为""或者null。返回defaultValue值；
<br/><b style="color:red">注意:defaultValue必须指定类型，例如：4，需要写成4D</b>', 'var res = tcUtil.toLong(Object value); <br/>
var res = tcUtil.toLong(Object value, Long defaultValue);', '函数 tcUtil.toDouble("123.25")输出123.25。 <br/>
函数 tcUtil.toDouble("", 4.52D)输出4.52。<br/>
函数 tcUtil.toDouble(null, 4.52D)输出4.52。', 2, '', '2024-01-03 03:01:55', '', '2024-01-08 07:43:48', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(3, 'strUtil.length() 字符串长度', 'strUtil.length()', '该函数计算字符串的长度。', 1, '3', '该函数计算字符串的长度。', 'var len=strUtil.length(CharSequence cs);', '函数 strUtil.length("abcd") 输出4', 1, '', '2024-01-03 03:01:55', '', '2024-01-08 08:47:36', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(4, 'strUtil.sub() 截取字符串', 'strUtil.sub()', '该函数截取字符串。', 1, '3', '该函数截取字符串。str 要截取的字符串，fromIndexInclude – 开始的index（包括），toIndexExclude – 结束的index（不包括）', 'var newStr=strUtil.length(string str,int fromIndexInclude,int toIndexExclude)', '函数 strUtil.sub("abcd",1,3) 输出"bc"', 2, '', '2024-01-04 02:40:56', '', '2024-01-05 02:43:28', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(5, 'numUtil.add() 数字类型做加法', 'numUtil.add()', '数字类型做加法。', 1, '5', '数字类型做加法，支持多个数值参数运算。注意：参数中类型保持一致。', 'var res=numUtil.add(Number...);<br/>
var res=numUtil.add(String...); <br/>
var res=numUtil.add(Number n1,Number n2);<br/>
var res=numUtil.add(String s1,String s2);<br/>
var res=numUtil.add(Double d1,Double d2);<br/>', '函数 numUtil.add("1","2");输出”3.0“; <br/>
函数 numUtil.add(1,1,2);输出”4.0“; <br/>
函数 numUtil.add("1.2323","2");输出”3.2323“;<br/>
函数 numUtil.add(1.2323D,2);输出”3.2323“;<br/>', 1, '', '2024-01-05 02:40:12', '', '2024-01-08 07:51:45', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(6, 'numUtil.sub() 数字类型做减法', 'numUtil.sub()', '数字类型做减法。', 1, '5', '数字类型做减法，支持多个数值参数运算。注意：参数中类型保持一致。', 'var res=numUtil.sub(Number...);<br/>
var res=numUtil.sub(String...); <br/>
var res=numUtil.sub(Number n1,Number n2);<br/>
var res=numUtil.sub(String s1,String s2);<br/>
var res=numUtil.sub(Double d1,Double d2);<br/>', '函数 numUtil.sub("2","1");输出”1“; <br/>
函数 numUtil.sub("2","1","1");输出”0“; <br/>
函数 numUtil.sub("2","1.1");输出”0.9“;</br>', 1, '', '2024-01-05 02:40:12', '', '2024-01-08 07:54:10', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(7, 'numUtil.mul() 数字类型做乘法', 'numUtil.mul()', '数字类型做乘法。', 1, '5', '数字类型做乘法，支持多个数值参数运算。注意：参数中类型保持一致。', 'var res=numUtil.mul(Number...);<br/>
var res=numUtil.mul(String...); <br/>
var res=numUtil.mul(Number n1,Number n2);<br/>
var res=numUtil.mul(String s1,String s2);<br/>
var res=numUtil.mul(Double d1,Double d2);<br/>', '函数 numUtil.mul("2","1");输出”2“; <br/>
函数 numUtil.mul("2","1","2");输出”4“; </br>
函数 numUtil.mul("2","1.1");输出”2.2“;</br>', 1, '', '2024-01-05 02:40:12', '', '2024-01-08 07:55:48', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(8, 'numUtil.divide() 数字类型做除法', 'numUtil.divide()', '数字类型做除法，并提供重载方法用于规定除不尽的情况下保留小数位数和舍弃方式。<br/>
  参数：<br/>
     @param v1    被除数<br/>
     @param v2    除数<br/>
     @param scale 精确度，如果为负值，取绝对值', 1, '5', '数字类型做除法，并提供重载方法用于规定除不尽的情况下保留小数位数和舍弃方式。<br/>
  参数：<br/>
     @param v1    被除数<br/>
     @param v2    除数<br/>
     @param scale 精确度，如果为负值，取绝对值', 'var res=numUtil.divide(Double d1,Double d2);<br/>
var res=numUtil.divide(Double d1,Double d2,int scale);<br/>
var res=numUtil.divide(Number n1,Number n2);<br/>
var res=numUtil.divide(Number n1,Number n2,int scale);<br/>
var res=numUtil.divide(String s1,String s2);<br/>
var res=numUtil.divide(String s1,String s2,int scale);<br/>', '函数 numUtil.divide("4","2"); 输出”2.0000000000“; <br/>
函数 numUtil.divide("4","2",2); 输出”2.00“; <br/>
函数 numUtil.divide(4,2,2); 输出”2.0“;</br>', 1, '', '2024-01-05 02:40:12', '', '2024-01-08 08:01:50', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(9, 'numUtil.round() 保留小数', 'numUtil.round()', 'numUtil.round() 方法主要封装BigDecimal中的方法来保留小数，返回BigDecimal，这个方法更加灵活，可以选择四舍五入或者全部舍弃等模式。', 1, '5', 'numUtil.round() 方法主要封装BigDecimal中的方法来保留小数，返回BigDecimal，这个方法更加灵活，可以选择四舍五入或者全部舍弃等模式。<br/>
  参数：<br/>
    @param v     值
    @param scale 保留小数位数
    @return 新值', 'var res=numUtil.round(Double val,int scale);<br/>
var res=numUtil.round(String val,int scale);<br/>', '函数 numUtil.round(2.236,2); 输出”2.24“; <br/>
函数 numUtil.round(2.234,2); 输出”2.23“; <br/>', 1, '', '2024-01-05 02:40:12', '', '2024-01-08 08:08:07', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(10, 'numUtil.toStr()  数字转字符串', 'numUtil.toStr()', '数字转字符串，自动并去除尾小数点儿后多余的0', 1, '5', '数字转字符串，自动并去除尾小数点儿后多余的0', 'var res=numUtil.toStr(Number number);<br/>
//defaultValue 如果number参数为{@code null}，返回此默认值<br/>
var res=numUtil.toStr(Number number, String defaultValue);<br/>
//isStripTrailingZeros 是否去除末尾多余0，例如5.0返回5<br/>
var res=numUtil.toStr(Number number, boolean isStripTrailingZeros);<br/>', '函数 numUtil.toStr(3.00); 输出”3“; <br/>
函数 numUtil.toStr(null,"3"); 输出”3“; <br/>
函数 numUtil.toStr(3.00100); 输出”3.001“; <br/>', 1, '', '2024-01-05 02:40:12', '', '2024-01-08 08:14:30', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(11, 'tcUtil.digitToChinese() 金钱数转换为大写', 'tcUtil.digitToChinese()', '金钱数转换为大写', 1, '1', '金钱数转换为大写', 'var res=tcUtil.digitToChinese(Number n);<br/>', '函数 tcUtil.digitToChinese(3.00); 输出”叁元整“; <br/>
函数 numUtil.numUtil.toStr(3.00100); 输出”叁元零壹分“; </br>', 3, '', '2024-01-03 03:01:55', '', '2024-01-08 07:34:09', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(12, 'strUtil.split() 切分字符串', 'strUtil.split()', '切分字符串', 1, '3', '切分字符串', 'var res=strUtil.split(CharSequence str, char separator);', '<pre>
函数 strUtil.split("a,b,c", ","); 输出”[a,b,c]“;<br/>
可以对数据遍历：
var list=strUtil.split("a,b,c", ",");
var res="";
for(var s:list){
    res=res+s;
}
</pre>', 3, '', '2024-01-03 03:01:55', '', '2024-01-08 08:43:41', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(13, 'strUtil.isBlank() 字符串是否为空白', 'strUtil.isBlank()', '字符串是否为空白', 1, '3', '<p>字符串是否为空白，空白的定义如下：</p>
<ol>
  <li>{@code null}</li>
  <li>空字符串：{@code ""}</li>
  <li>空格、全角空格、制表符、换行符，等不可见字符</li>
</ol>

<p>例：</p>
<ul>
  <li>{@code StrUtil.isBlank(null)     // true}</li>
  <li>{@code StrUtil.isBlank("")       // true}</li>
  <li>{@code StrUtil.isBlank(" \\t\\n")  // true}</li>
  <li>{@code StrUtil.isBlank("abc")    // false}</li>
</ul>', 'var isBlank=strUtil.isBlank(CharSequence str);', '<pre>
函数 strUtil.isBlank(""); 输出：“true”<br/>
可以对数据判断：
if(strUtil.isBlank("")){
    return "空值";
}else{
    return "有值";
}

</pre>', 4, '', '2024-01-03 03:01:55', '', '2024-01-08 09:46:38', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(14, 'strUtil.isNotBlank() 字符串是否为非空白', 'strUtil.isNotBlank()', '字符串是否为非空白', 1, '3', '<p>字符串是否为非空白，非空白的定义如下： </p>
<ol>
  <li>不为 {@code null}</li>
  <li>不为空字符串：{@code ""}</li>
  <li>不为空格、全角空格、制表符、换行符，等不可见字符</li>
</ol>

<p>例：</p>
<ul>
  <li>{@code StrUtil.isNotBlank(null)     // false}</li>
  <li>{@code StrUtil.isNotBlank("")       // false}</li>
  <li>{@code StrUtil.isNotBlank(" \\t\\n")  // false}</li>
  <li>{@code StrUtil.isNotBlank("abc")    // true}</li>
</ul>', 'var isNotBlank=strUtil.isNotBlank(CharSequence str);', '<pre>
函数 strUtil.isNotBlank(""); 输出：“false”<br/>
可以对数据判断：
if(strUtil.isNotBlank("")){
    return "有值";
}else{
    return "空值";
}

</pre>', 5, '', '2024-01-03 03:01:55', '', '2024-01-08 09:52:28', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(15, 'strUtil.isEmpty() 字符串是否为非空白', 'strUtil.isEmpty()', '字符串是否为空', 1, '3', '<p>字符串是否为空，空的定义如下：</p>
<ol>
  <li>{@code null}</li>
  <li>空字符串：{@code ""}</li>
</ol>

<p>例：</p>
<ul>
  <li>{@code StrUtil.isEmpty(null)     // true}</li>
  <li>{@code StrUtil.isEmpty("")       // true}</li>
  <li>{@code StrUtil.isEmpty(" \\t\\n")  // false}</li>
  <li>{@code StrUtil.isEmpty("abc")    // false}</li>
</ul>', 'var isEmpty=strUtil.isEmpty(CharSequence str);', '<pre>
函数 strUtil.isEmpty(""); 输出：“true”<br/>
可以对数据判断：
if(strUtil.isEmpty("")){
    return "空值";
}else{
    return "有值";
}

</pre>', 6, '', '2024-01-03 03:01:55', '', '2024-01-22 08:00:50', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(16, 'strUtil.isNotEmpty() 字符串是否为空', 'strUtil.isNotEmpty()', '字符串是否为非空白', 0, '3', '<p>字符串是否为非空白，非空白的定义如下： </p>
<ol>
  <li>不为 {@code null}</li>
  <li>不为空字符串：{@code ""}</li>
</ol>

<p>例：</p>
<ul>
  <li>{@code StrUtil.isNotEmpty(null)     // false}</li>
  <li>{@code StrUtil.isNotEmpty("")       // false}</li>
  <li>{@code StrUtil.isNotEmpty(" \\t\\n")  // true}</li>
  <li>{@code StrUtil.isNotEmpty("abc")    // true}</li>
</ul>', 'var isNotEmpty=strUtil.isNotEmpty(CharSequence str);', '<pre>
函数 strUtil.isNotEmpty(""); 输出：“false”<br/>
可以对数据判断：
if(strUtil.isNotEmpty("")){
    return "有值";
}else{
    return "空值";
}

</pre>', 6, '', '2024-01-03 03:01:55', '', '2024-01-22 07:44:34', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(17, 'dateUtil.format() 根据特定格式格式化日期', 'dateUtil.format()', '根据特定格式格式化日期', 1, '4', '<pre>
根据特定格式格式化日期

@param localDateTime 被格式化的日期
@param format        日期格式
@return 格式化后的字符串
</pre>', 'var dateStr=dateUtil.format(LocalDateTime localDateTime, String format);', '函数 dateUtil.format(日期类型,"yyy-MM-dd") 输出"yyyy-MM-dd格式的日期字符串"', 1, '', '2024-01-03 03:01:55', '', '2024-01-09 02:08:02', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(18, 'dateUtil.now() 当前时间，格式 yyyy-MM-dd HH:mm:ss', 'dateUtil.now()', '当前时间，格式 yyyy-MM-dd HH:mm:ss', 1, '4', '<pre>
当前时间，格式 yyyy-MM-dd HH:mm:ss
@return 当前时间的标准形式字符串
</pre>', 'var dateStr=dateUtil.now();', '函数 dateUtil.now() 输出"当前时间的标准形式字符串"', 2, '', '2024-01-03 03:01:55', '', '2024-01-09 02:10:38', NULL, '0', NULL);
INSERT INTO report2_etl_fun
(id, fun_name, fun_code, fun_description, fun_enable, group_id, introduce, instruct, example, sort, create_by, create_time, update_by, update_time, tenant_id, del_flag, version)
VALUES(19, 'dateUtil.today() 当前日期，格式 yyyy-MM-dd', 'dateUtil.today()', '当前日期，格式 yyyy-MM-dd', 1, '4', '<pre>
当前日期，格式 yyyy-MM-dd
@return 当前日期的标准形式字符串
</pre>', 'var dateStr=dateUtil.today();', '函数 dateUtil.today() 输出"当前日期的标准形式字符串"', 3, '', '2024-01-03 03:01:55', '', '2024-01-09 02:14:54', NULL, '0', NULL);

INSERT INTO report2_business_group (id,biz_group_name,seq,parent_id,del_flag,create_time,create_by,update_time,update_by,version,tenant_id,biz_group_code) VALUES
(1,'默认分组',0,NULL,'0','2025-01-01 00:00:00',NULL,'2025-01-01 00:00:00',NULL,0,NULL,'001');

INSERT INTO report2_group (id,biz_group_name,seq,parent_id,project_id,del_flag,create_time,create_by,update_time,update_by,version,tenant_id,biz_group_code) VALUES
(1847100820986273793,'默认分组',NULL,0,NULL,'0','2025-01-01 00:00:00','1','2025-01-01 00:00:00','1',1,NULL,'test_group');

INSERT INTO report2_etl_fun_group
(id, group_name, group_description, group_enable, sort, version, create_by, create_time, update_by, update_time, tenant_id)
VALUES(1, '类型转换函数', '类型转换函数', 1, 3, 0, '', '2025-01-01 00:00:00', '', '2025-01-01 00:00:00', NULL);
INSERT INTO report2_etl_fun_group
(id, group_name, group_description, group_enable, sort, version, create_by, create_time, update_by, update_time, tenant_id)
VALUES(2, '数据处理函数', '数据处理函数', 1, 4, 0, '', '2025-01-01 00:00:00', '', '2025-01-01 00:00:00', NULL);
INSERT INTO report2_etl_fun_group
(id, group_name, group_description, group_enable, sort, version, create_by, create_time, update_by, update_time, tenant_id)
VALUES(3, '字符串处理函数', '字符串处理函数', 1, 1, 0, '', '2025-01-01 00:00:00', '', '2025-01-01 00:00:00', NULL);
INSERT INTO report2_etl_fun_group
(id, group_name, group_description, group_enable, sort, version, create_by, create_time, update_by, update_time, tenant_id)
VALUES(4, '日期类函数', '日期类函数', 1, 2, 0, '', '2025-01-01 00:00:00', '', '2025-01-01 00:00:00', NULL);
INSERT INTO report2_etl_fun_group
(id, group_name, group_description, group_enable, sort, version, create_by, create_time, update_by, update_time, tenant_id)
VALUES(5, '数字类函数', '数字类函数', 1, 5, 0, '', '2025-01-01 00:00:00', '', '2025-01-01 00:00:00', NULL);

