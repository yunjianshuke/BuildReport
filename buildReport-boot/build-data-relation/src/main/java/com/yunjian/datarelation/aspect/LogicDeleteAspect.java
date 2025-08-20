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
package com.yunjian.datarelation.aspect;

import com.yunjian.datarelation.pool.DatasourcePoolManager;
import com.yunjian.datarelation.pool.LogicDelete;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 代理需要逻辑删除的方法
 *
 * @author yujian
 **/

@Aspect
public class LogicDeleteAspect {

    /**
     * 是否开启逻辑删除填充操作,如果执行流程中需要执行异步操作,那么应该使用阿里配套的线程池
     *
     * @param pjp 目标
     * @return value
     * @throws Throwable e
     */
    @Around("@annotation(com.yunjian.datarelation.anno.LogicDeleteAware)")
    public Object service(ProceedingJoinPoint pjp) throws Throwable {
        DatasourcePoolManager.LOGIC_DELETE_HOLDER.set(new LogicDelete());
        try {
            return pjp.proceed();
        } finally {
            DatasourcePoolManager.LOGIC_DELETE_HOLDER.remove();
        }

    }

}
