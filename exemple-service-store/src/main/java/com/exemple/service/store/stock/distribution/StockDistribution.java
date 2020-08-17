package com.exemple.service.store.stock.distribution;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.framework.recipes.nodes.PersistentTtlNode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.exemple.service.store.common.NoFoundStockException;
import com.google.common.base.Throwables;

@Component
public class StockDistribution {

    private final long productTtlMs;

    private final CuratorFramework client;

    public StockDistribution(@Qualifier("stockCuratorFramework") CuratorFramework client,
            @Value("${store.zookeeper.ttlMs.product:30000}") long productTtlMs) {

        this.client = client;
        this.productTtlMs = productTtlMs;
    }

    public <T, E extends Exception> T lockStock(String company, String store, String product, LockStock<T> action, Class<E> exception) throws E {

        try (PersistentTtlNode node = createProduct(company, store, product)) {

            InterProcessLock lock = new InterProcessSemaphoreMutex(client, company + store + product);

            try {
                lock.acquire();
                return action.get();
            } finally {
                lock.release();
            }

        } catch (Exception e) {
            Throwables.throwIfInstanceOf(e, exception);
            throw new IllegalStateException(e);
        }

    }

    @FunctionalInterface
    public interface LockStock<T> {
        T get() throws Exception;

        static <T> T accessStock(String store, String product, LockStock<T> action) throws NoFoundStockException {
            try {
                return action.get();
            } catch (KeeperException.NoNodeException e) {
                throw new NoFoundStockException(store, product, e);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public byte[] getStock(String company, String store, String product) throws NoFoundStockException {
        return LockStock.accessStock(store, product, () -> client.getData().forPath(company + store + product));
    }

    public void updateStock(String company, String store, String product, byte[] stock) throws NoFoundStockException {
        LockStock.accessStock(store, product, () -> client.setData().forPath(company + store + product, stock));
    }

    private PersistentTtlNode createProduct(String company, String store, String product) throws Exception {

        if (client.checkExists().creatingParentsIfNeeded().forPath(company + store) == null) {
            (new PersistentNode(client, CreateMode.PERSISTENT, false, company + store, new byte[0])).start();
        }

        if (client.checkExists().forPath(company + store + product) == null) {
            PersistentTtlNode node = new PersistentTtlNode(client, company + store + product, productTtlMs, new byte[0]);
            node.start();
            return node;
        }

        return null;
    }
}
