package com.exemple.service.store.stock.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.framework.recipes.nodes.PersistentTtlNode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.resource.stock.StockResource;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.common.NoFoundStockException;
import com.exemple.service.store.stock.StockService;
import com.google.common.primitives.Longs;

@Service
@Validated
public class StockServiceImpl implements StockService {

    private static final Logger LOG = LoggerFactory.getLogger(StockServiceImpl.class);

    private final long productTtlMs;

    private final CuratorFramework client;

    private final StockResource resource;

    public StockServiceImpl(@Qualifier("stockCuratorFramework") CuratorFramework client, StockResource resource,
            @Value("${store.zookeeper.ttlMs.product:30000}") long productTtlMs) {

        this.client = client;
        this.resource = resource;
        this.productTtlMs = productTtlMs;
    }

    @Override
    public Long update(String company, String store, String product, int quantity) throws InsufficientStockException {

        try (PersistentTtlNode node = createProduct(company, store, product)) {

            InterProcessLock lock = new InterProcessSemaphoreMutex(client, company + store + product);

            try {
                lock.acquire();

                long stock = getStock(company, store, product);
                if (stock + quantity < 0) {
                    throw new InsufficientStockException(company, store, product, stock, quantity);
                }
                LOG.debug("incrementation stock {} {} {} {}", company, store, product, quantity);
                resource.update(store, product, quantity);
                client.setData().forPath(company + store + product, Longs.toByteArray(stock + quantity));
                return stock + quantity;
            } finally {
                lock.release();
            }

        } catch (InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

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

    private Long getStock(String company, String store, String product) throws Exception {

        byte[] data = client.getData().forPath(company + store + product);
        if (data.length == 0) {
            data = Longs.toByteArray(resource.get(store, product));
        }
        return Longs.fromByteArray(data);

    }

    @Override
    public Long get(String company, String store, String product) throws NoFoundStockException {

        try {

            return getStock(company, store, product);

        } catch (KeeperException.NoNodeException e) {
            throw new NoFoundStockException(store, product, e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
