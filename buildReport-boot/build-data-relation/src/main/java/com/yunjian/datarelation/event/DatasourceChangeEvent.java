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
package com.yunjian.datarelation.event;

import com.yunjian.datarelation.entity.Datasource;
import com.yunjian.datarelation.enums.DatasourceEventEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author yujian
 **/
@Getter
public class DatasourceChangeEvent extends ApplicationEvent {

    private final Long datasourceId;
    private final DatasourceEventEnum eventEnum;
    private Datasource instance;


    public DatasourceChangeEvent(Object source, Long datasourceId, DatasourceEventEnum eventEnum) {
        super(source);
        this.datasourceId = datasourceId;
        this.eventEnum = eventEnum;
    }

    public DatasourceChangeEvent(Object source, Datasource instance, DatasourceEventEnum eventEnum) {
        super(source);
        this.datasourceId = instance.getId();
        this.instance = instance;
        this.eventEnum = eventEnum;
    }
}
