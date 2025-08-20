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
package com.yunjian.datarelation.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunjian.datarelation.aspect.LogicDeleteAspect;
import com.yunjian.datarelation.cache.DictCache;
import com.yunjian.datarelation.engine.BusinessEngine;
import com.yunjian.datarelation.jexl.AdvancedConversionEngine;
import com.yunjian.datarelation.pool.DatasourcePoolManager;
import com.yunjian.datarelation.service.*;
import com.yunjian.datarelation.service.impl.ReportDictConditionService;
import com.yunjian.datarelation.service.impl.ReportDictConfService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author yujian
 **/
@Configuration
@ComponentScan(
        basePackages = {
                "com.yunjian.datarelation.service",
                "com.yunjian.datarelation.service.impl",
                "com.yunjian.datarelation.controller"
        }
)
@MapperScan(basePackages = "com.yunjian.datarelation.mapper")
@EnableAsync(proxyTargetClass = true)
@Import(JsonConfig.class)
public class DataRelationAutoConfiguration {

    @Bean
    public DatasourcePoolManager datasourcePoolManager(
            RedisConnectionFactory redisConnectionFactory) {
        ScheduledThreadPoolExecutor healthCheckExecutor = new ScheduledThreadPoolExecutor(1,
                ThreadFactoryBuilder.create().setNamePrefix("healthCheck-").build());
        return new DatasourcePoolManager(healthCheckExecutor,
                reportProperties().getDatasource(), redisTemplate(redisConnectionFactory));
    }

    @Bean("SerializeRedis")
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public ReportProperties reportProperties() {
        return new ReportProperties();
    }

    @Bean
    public DictCache dictCache(ReportDictConfService reportDictConfService,
                               ReportDictConditionService reportDictConditionService, DatasourcePoolManager datasourcePoolManager) {
        return new DictCache(reportDictConfService, reportDictConditionService,
                datasourcePoolManager, reportProperties().getDict());
    }

    @Bean
    public BusinessEngine businessEngine(DictCache dictCache, ReportBusinessNodeService reportBusinessNodeService,
                                         ReportBusinessNodeRelationService reportBusinessNodeRelationService,
                                         RedisConnectionFactory redisConnectionFactory) {
        return new BusinessEngine(reportBusinessNodeService, reportBusinessNodeRelationService,
                datasourcePoolManager(redisConnectionFactory), dictCache);
    }

    @Bean
    public AdvancedConversionEngine advancedConversionEngine() {
        return new AdvancedConversionEngine();
    }

    @Bean
    public LogicDeleteAspect clearHolderAspect() {
        return new LogicDeleteAspect();
    }


}
