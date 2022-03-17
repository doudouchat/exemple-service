package com.exemple.service.store.stock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.core.StoreTestConfiguration;

@ContextConfiguration(classes = { StoreTestConfiguration.class })
public class StockServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private StockService service;

    @Autowired
    private StockResource resource;

    @Autowired
    @Qualifier("storeCuratorFramework")
    private CuratorFramework zookeeper;

    private final String company = "/test";

    private final String product = "/product#" + UUID.randomUUID();

    private final String store = "/store#" + UUID.randomUUID();

    @Test
    public void update() throws InterruptedException {

        Mockito.when(resource.get(Mockito.eq(store), Mockito.eq(product))).thenReturn(100L);

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

        Mockito.verify(resource).get(Mockito.eq(store), Mockito.eq(product));

    }

    @Test(dependsOnMethods = "update")
    public void get() {

        assertThat(service.get(company, store, product).get(), is(140L));

    }

    @Test
    public void getFailureNoFoundStock() {

        String product = "/product#" + UUID.randomUUID();
        String store = "/store#" + UUID.randomUUID();

        assertThat(service.get(company, store, product).isPresent(), is(false));

    }

    private void update(String store, String product, int quantity) {

        try {
            service.update(company, store, product, quantity);
        } catch (InsufficientStockException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void updateFailureInsufficientStock() throws InterruptedException, InsufficientStockException {

        String product = "/product#" + UUID.randomUUID();
        String store = "/store#" + UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(store), Mockito.eq(product))).thenReturn(5L);
        service.update(company, store, product, -3);
        try {
            service.update(company, store, product, -3);

            Assert.fail("InsufficientStockException must be throwed");

        } catch (InsufficientStockException e) {

            assertThat(e.getCompany(), is(company));
            assertThat(e.getStore(), is(store));
            assertThat(e.getProduct(), is(product));
            assertThat(e.getStock(), is(2L));
            assertThat(e.getQuantity(), is(-3L));

        }

        Mockito.verify(resource).get(Mockito.eq(store), Mockito.eq(product));

    }

    @Test(dependsOnMethods = { "update", "updateFailureInsufficientStock" })
    public void updateFailure() {

        String product = "/product#" + UUID.randomUUID();
        String store = "/store#" + UUID.randomUUID();

        try {
            CloseableUtils.closeQuietly(zookeeper);

            service.update(company, store, product, 5);

            Assert.fail("IllegalStateException must be throwed");

        } catch (Exception e) {

            assertThat(e, instanceOf(IllegalStateException.class));

        }

    }

    @Test(dependsOnMethods = "updateFailure")
    public void getFailure() {

        String product = "/product#" + UUID.randomUUID();
        String store = "/store#" + UUID.randomUUID();

        try {
            service.get(company, store, product);

            Assert.fail("IllegalStateException must be throwed");

        } catch (Exception e) {

            assertThat(e.getCause(), instanceOf(IllegalStateException.class));

        }

    }
}
