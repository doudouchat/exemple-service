package com.exemple.service.api.core.cache;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Configuration;

import com.exemple.service.resource.core.ResourceExecutionContext;

@Configuration
public class CachingConfiguration implements CachingConfigurer {

    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

    private static class CustomKeyGenerator implements KeyGenerator {

        @Override
        public Object generate(Object o, Method method, Object... params) {

            return SimpleKeyGenerator.generateKey(ArrayUtils.addFirst(params, ResourceExecutionContext.get().keyspace()));
        }

    }
}
