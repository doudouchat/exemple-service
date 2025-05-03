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

    @Around(value = "execution(public void com.exemple.service.store.stock.StockService.increment(..)) && args(company,store,product,..)",
            argNames = "company,store,product")
    public void lock(ProceedingJoinPoint joinPoint, String company, String store, String product) throws Exception {

        this.distribution.lockStock(company, store, product, () -> proceed(joinPoint));
    }

    @SneakyThrows
    private static void proceed(ProceedingJoinPoint joinPoint) {
        joinPoint.proceed();
    }

}
