package com.exemple.service.customer.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;
import com.pivovarit.function.ThrowingConsumer;

@Component
@EnableScheduling
public class CustomerScriptFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerScriptFactory.class);

    private final ApplicationDetailService applicationDetailService;

    private final ApplicationContext defaultApplicationContext;

    private final ConcurrentMap<String, ApplicationContext> scriptApplicationContexts;

    private final ConcurrentMap<String, Long> checksumApplicationContexts;

    private final String contextsPath;

    private static final ApplicationContext EMPTY_CONTEXT = new GenericApplicationContext();

    public CustomerScriptFactory(ApplicationDetailService applicationDetailService, @Value("${customer.contexts.path}") String contextsPath) {
        this.defaultApplicationContext = new FileSystemXmlApplicationContext("classpath:exemple-service-customer.xml");
        this.scriptApplicationContexts = new ConcurrentHashMap<>();
        this.checksumApplicationContexts = new ConcurrentHashMap<>();
        this.applicationDetailService = applicationDetailService;
        this.contextsPath = contextsPath;
    }

    public <T> T getBean(String beanName, Class<T> beanClass) {

        String app = ServiceContextExecution.context().getApp();
        ApplicationDetail applicationDetail = applicationDetailService.get(app);

        ApplicationContext applicationContext = defaultApplicationContext;
        if (checkIfBeanIsPresent(applicationDetail.getCompany(), beanName)) {

            applicationContext = scriptApplicationContexts.get(applicationDetail.getCompany());
        }

        return applicationContext.getBean(beanName, beanClass);
    }

    private boolean checkIfBeanIsPresent(String company, String beanName) {

        return scriptApplicationContexts.getOrDefault(company, EMPTY_CONTEXT).containsLocalBean(beanName);
    }

    @Scheduled(fixedDelayString = "${customer.contexts.delay:30000}")
    public void completeScriptApplicationContexts() throws IOException {

        try (Stream<Path> paths = Files.list(Paths.get(this.contextsPath))) {

            paths.forEach(ThrowingConsumer.sneaky(path -> saveContextIfPresent(path, "exemple-service-customer.xml")));
        }

    }

    private void saveContextIfPresent(Path path, String contextName) throws IOException {

        try (Stream<Path> paths = Files.list(path)) {

            paths.filter((Path p) -> Paths.get(contextName).equals(p.getFileName())).findFirst().ifPresent(ThrowingConsumer.sneaky((Path p) -> {
                String contextKey = path.getFileName().toString();
                saveContextIfUpdated(contextKey, p);
            }));
        }

    }

    private void saveContextIfUpdated(String contextKey, Path contextPath) throws IOException {

        long checksum = FileUtils.checksumCRC32(contextPath.toFile());
        if (checksum != checksumApplicationContexts.getOrDefault(contextKey, 0L)) {

            String configLocation = contextPath.toString();
            LOG.debug("loading context {}", configLocation);

            scriptApplicationContexts.put(contextKey, new FileSystemXmlApplicationContext("file:///" + configLocation));
            checksumApplicationContexts.put(contextKey, checksum);
        }

    }

}
