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

import cn.hutool.core.date.DatePattern;
import cn.hutool.json.JSONNull;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yujian
 */
@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(JSONNull.class,
                com.fasterxml.jackson.databind.ser.std.NullSerializer.instance);
        simpleModule.addSerializer(Long.class, new JsonSerializer<Long>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeString(String.valueOf(value));
            }
        });
        mapper.registerModule(simpleModule);
        mapper.registerModule(javaTimeModule());
        return mapper;
    }

    public Module javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)
        ));
        module.addSerializer(new LocalTimeSerializer(
                DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN))
        );
        module.addSerializer(new LocalDateSerializer(
                DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN))
        );
        module.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(
                        DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)
                ));
        module.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(
                        DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)
                ));
        module.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(
                        DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)
                ));
        return module;
    }


}
