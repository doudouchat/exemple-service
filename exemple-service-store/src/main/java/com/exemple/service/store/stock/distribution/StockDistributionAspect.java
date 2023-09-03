package com.exemple.service.store.stock.distribution;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Aspect
@Component
@RequiredArgsConstructor
public class StockDistributionAspect {

    private final StockDistribution distribution;

    @Around(value = "execution(public * com.exemple.service.store.stock.StockService.update(..)) && args(company,store,product,..)",
            argNames = "company,store,product")
    public Object lock(ProceedingJoinPoint joinPoint, String company, String store, String product) throws Exception {

        return this.distribution.lockStock(company, store, product, () -> proceed(joinPoint));
    }

    @SneakyThrows
    private static Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

}
