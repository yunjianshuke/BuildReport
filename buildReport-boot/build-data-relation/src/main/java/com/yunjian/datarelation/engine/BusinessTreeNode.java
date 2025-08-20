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
package com.yunjian.datarelation.engine;

import com.yunjian.datarelation.engine.pojo.SqlField;

import java.util.List;

import lombok.Data;

/**
 * 业务关系图多叉树
 *
 * @author yujian
 **/
@Data
public class BusinessTreeNode {

    private String cellId;
    private String prevNodeCellId;
    private List<BusinessTreeNode> nextNodes;

    private List<SqlField> sqlField;
    private boolean del;

    /**
     * 是否是叶子节点
     *
     * @return true 是叶子节点
     */
    public boolean isLeaf() {
        return nextNodes == null;
    }

    /**
     * 该节点是否选择了字段
     *
     * @return true 选择了字段
     */
    public boolean isSelected() {
        return sqlField != null && !sqlField.isEmpty();
    }
}
