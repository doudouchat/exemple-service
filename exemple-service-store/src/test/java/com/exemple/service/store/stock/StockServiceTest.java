package com.exemple.service.store.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.core.StoreTestConfiguration;

@SpringJUnitConfig(StoreTestConfiguration.class)
public class StockServiceTest {

    @Autowired
    private StockService service;

    @Autowired
    private StockResource resource;

    @Autowired
    @Qualifier("storeCuratorFramework")
    private CuratorFramework zookeeper;

    private final String company = "/test";

    private static String product = "/product#" + UUID.randomUUID();

    private static String store = "/store#" + UUID.randomUUID();

    @Nested
    class MultiplateUpdate {

        @DisplayName("multiple update stock")
        @Test
        public void update() throws InterruptedException {

            // setup mock resource

            Mockito.when(resource.get(Mockito.eq(store), Mockito.eq(product))).thenReturn(Optional.of(100L));

            // when perform multiple update

            ExecutorService executorService = new ThreadPoolExecutor(5, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

            executorService.submit(() -> update(store, product, -15));
            executorService.submit(() -> update(store, product, -5));
            executorService.submit(() -> update(store, product, -3));
            executorService.submit(() -> update(store, product, -7));
            executorService.submit(() -> update(store, product, -8));
            executorService.submit(() -> update(store, product, -6));
            executorService.submit(() -> update(store, product, -9));
            executorService.submit(() -> update(store, product, -5));
            executorService.submit(() -> update(store, product, -2));
            executorService.submit(() -> update(store, product, 100));

            executorService.awaitTermination(5, TimeUnit.SECONDS);
            executorService.shutdown();

            // Then check mock

            Mockito.verify(resource).get(Mockito.eq(store), Mockito.eq(product));

            // And check stock

            assertThat(service.get(company, store, product)).hasValue(140L);

        }

        private void update(String store, String product, int quantity) {

            try {
                service.update(company, store, product, quantity);
            } catch (InsufficientStockException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @DisplayName("get stock if not exists")
    @Test
    public void getFailureNoFoundStock() {

        // when perform get

        String product = "/product#" + UUID.randomUUID();
        String store = "/store#" + UUID.randomUUID();

        Optional<Long> stock = service.get(company, store, product);

        // Then check stock is missing
        assertThat(stock).isEmpty();

    }

    @DisplayName("update stock if stock is insufficient")
    @Test
    public void updateFailureInsufficientStock() throws InsufficientStockException {

        // setup mock resource

        String product = "/product#" + UUID.randomUUID();
        String store = "/store#" + UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(store), Mockito.eq(product))).thenReturn(Optional.of(5L));

        // when update stock
        service.update(company, store, product, -3);

        // and update again
        Throwable throwable = catchThrowable(() -> service.update(company, store, product, -3));

        // Then check throwable
        assertAll(
                () -> assertThat(throwable).isInstanceOf(InsufficientStockException.class),
                () -> assertThat(throwable).hasMessageEndingWith("2 is insufficient for quantity -3"));

    }

    @DisplayName("update stock if stock is insufficient because resource is missing")
    @Test
    public void updateFailureInsufficientStockBecauseNonStock() throws InsufficientStockException {

        // setup mock resource

        String product = "/product#" + UUID.randomUUID();
        String store = "/store#" + UUID.randomUUID();

        // when update stock
        Throwable throwable = catchThrowable(() -> service.update(company, store, product, -3));

        // Then check throwable
        assertAll(
                () -> assertThat(throwable).isInstanceOf(InsufficientStockException.class),
                () -> assertThat(throwable).hasMessageEndingWith("0 is insufficient for quantity -3"));
    }

    @Nested
    @DirtiesContext
    class FailureZookeeper {

        @BeforeEach
        private void closeZookeeper() {
            CloseableUtils.closeQuietly(zookeeper);

        }

        @Test
        public void updateFailure() {

            // when update stock
            String product = "/product#" + UUID.randomUUID();
            String store = "/store#" + UUID.randomUUID();
            Throwable throwable = catchThrowable(() -> service.update(company, store, product, 5));

            // Then check throwable
            assertThat(throwable).isInstanceOf(IllegalStateException.class);

        }

        @Test
        public void getFailure() {

            // when get stock
            String product = "/product#" + UUID.randomUUID();
            String store = "/store#" + UUID.randomUUID();
            Throwable throwable = catchThrowable(() -> service.get(company, store, product));

            // Then check throwable
            assertThat(throwable).isInstanceOf(IllegalStateException.class);

        }
    }
}
