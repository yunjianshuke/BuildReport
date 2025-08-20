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
package com.yunjian.reportbiz.config;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.common.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
@RestControllerAdvice
@Order(-1)
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBussinessException(BusinessException e) {
        String msg = StrUtil.isBlank(e.getMsg()) ? e.getMessage() : e.getMsg();
        log.error(msg, e);
        return R.failed(String.valueOf(e.getCode()), msg);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public R<Void> handleBadSqlGrammarException(BadSqlGrammarException e) {
        return R.failed(String.valueOf(0), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public R<Void> BindExceptionHandler(BindException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        List<String> msgList = new ArrayList<>();
        for (ObjectError allError : allErrors) {
            msgList.add(allError.getDefaultMessage());
        }
        return R.failed(String.join(StringPool.COMMA, msgList));
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        List<String> msgList = new ArrayList<>();
        for (ObjectError allError : allErrors) {
            msgList.add(allError.getDefaultMessage());
        }
        return R.failed(String.join(StringPool.COMMA, msgList));
    }

    @ResponseBody
    @ExceptionHandler(value = ConstraintViolationException.class)
    public R<Void> ConstraintViolationExceptionHandler(ConstraintViolationException ex) {

        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();
        List<String> msgList = new ArrayList<>();
        while (iterator.hasNext()) {
            ConstraintViolation<?> cvl = iterator.next();
            msgList.add(cvl.getMessageTemplate());
        }
        return R.failed(String.join(StringPool.COMMA, msgList));
    }

    @ExceptionHandler(ConvertException.class)
    public R handleConvertException(Exception e) {
        R reuslt = new R();
        reuslt.setCode(500);
        reuslt.setMsg(e.getMessage());
        log.error(e.getMessage(), e);
        return reuslt;
    }

    @ExceptionHandler(Exception.class)
    public R customException(Exception e) {
        R reuslt = new R();
        reuslt.setCode(500);
        reuslt.setMsg("系统内部出错");
        log.error(e.getMessage(), e);
        return reuslt;
    }

}
