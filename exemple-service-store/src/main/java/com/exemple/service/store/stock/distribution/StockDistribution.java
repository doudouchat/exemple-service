package com.exemple.service.store.stock.distribution;

import java.util.Optional;
import java.util.function.Supplier;

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

import com.google.common.primitives.Longs;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDistribution {

    @Value("${store.zookeeper.ttlMs.product:30000}")
    private final long productTtlMs;

    @Qualifier("stockCuratorFramework")
    private final CuratorFramework client;

    @SneakyThrows
    public Optional<Long> getStock(String company, String store, String product) {
        try {
            return Optional.of(client.getData().forPath("/" + company + "/" + store + "/" + product)).filter((byte[] stock) -> stock.length > 0)
                    .map(Longs::fromByteArray);
        } catch (KeeperException.NoNodeException e) {
            LOG.warn("Stock " + e.getPath() + " doesn't exist", e);
            return Optional.empty();
        }
    }

    @SneakyThrows
    public void updateStock(String company, String store, String product, long stock) {
        client.setData().forPath("/" + company + "/" + store + "/" + product, Longs.toByteArray(stock));
    }

    public <T> T lockStock(String company, String store, String product, Supplier<T> action) throws Exception {

        try (PersistentTtlNode node = createProduct("/" + company, "/" + store, "/" + product)) {

            InterProcessLock lock = new InterProcessSemaphoreMutex(client, "/" + company + "/" + store + "/" + product);

            try {
                lock.acquire();
                return action.get();
            } finally {
                lock.release();
            }
        }

    }

    private PersistentTtlNode createProduct(String company, String store, String product) throws Exception {

        if (client.checkExists().creatingParentsIfNeeded().forPath(company + store) == null) {
            (new PersistentNode(client, CreateMode.PERSISTENT, false, company + store, new byte[0])).start();
        }

        if (client.checkExists().forPath(company + store + product) == null) {
            var node = new PersistentTtlNode(client, company + store + product, productTtlMs, new byte[0]);
            node.start();
            return node;
        }

        return null;
    }
}
