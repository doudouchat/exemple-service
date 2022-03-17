package com.exemple.service.store.stock.distribution;

import java.util.function.Supplier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StockDistributionAspect {

    private final StockDistribution distribution;

    public StockDistributionAspect(StockDistribution distribution) {

        this.distribution = distribution;
    }

    @Around("execution(public * com.exemple.service.store.stock.StockService.update(..)) && args(company,store,product,quantity))")
    public Object lock(ProceedingJoinPoint joinPoint, String company, String store, String product, int quantity) throws Exception {

        return this.distribution.lockStock(company, store, product, () -> ThrowingSupplier.sneaky(joinPoint::proceed).get());
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Throwable> {
        T get() throws E;

        static <T1> Supplier<T1> sneaky(ThrowingSupplier<T1, ?> supplier) {
            return () -> {
                try {
                    return supplier.get();
                } catch (Throwable ex) {
                    return sneakyThrow(ex);
                }
            };
        }

        @SuppressWarnings("unchecked")
        static <T extends Exception, R> R sneakyThrow(Throwable t) throws T {
            throw (T) t;
        }
    }

}
