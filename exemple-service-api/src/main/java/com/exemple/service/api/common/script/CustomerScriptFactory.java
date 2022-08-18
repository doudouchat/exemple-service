package com.exemple.service.api.common.script;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class CustomerScriptFactory {

    private static final ApplicationContext EMPTY_CONTEXT = new GenericApplicationContext();

    private final ApplicationDetailService applicationDetailService;

    private final ApplicationContext defaultApplicationContext;

    private final ConcurrentMap<String, ApplicationContext> scriptApplicationContexts;

    private final ConcurrentMap<String, Long> checksumApplicationContexts;

    private final ApplicationContext applicationContext;

    private final String contextsPath;

    public CustomerScriptFactory(ApplicationDetailService applicationDetailService, @Value("${customer.contexts.path}") String contextsPath,
            ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.defaultApplicationContext = new FileSystemXmlApplicationContext(new String[] { "classpath:exemple-service-customer.xml" },
                this.applicationContext);
        this.scriptApplicationContexts = new ConcurrentHashMap<>();
        this.checksumApplicationContexts = new ConcurrentHashMap<>();
        this.applicationDetailService = applicationDetailService;
        this.contextsPath = contextsPath;
    }

    @PostConstruct
    public void initScriptApplicationContexts() throws IOException {
        completeScriptApplicationContexts();
    }

    public <T> T getBean(String beanName, Class<T> beanClass, String application) {

        ApplicationContext applicationScriptContext = applicationDetailService.get(application)
                .filter((ApplicationDetail applicationDetail) -> checkIfBeanIsPresent(applicationDetail.getCompany(), beanName))
                .map((ApplicationDetail applicationDetail) -> scriptApplicationContexts.get(applicationDetail.getCompany()))
                .orElse(defaultApplicationContext);

        return applicationScriptContext.getBean(beanName, beanClass);

    }

    private boolean checkIfBeanIsPresent(String company, String beanName) {

        return scriptApplicationContexts.getOrDefault(company, EMPTY_CONTEXT).containsLocalBean(beanName);
    }

    @Scheduled(fixedDelayString = "${customer.contexts.delay:30000}")
    public void completeScriptApplicationContexts() throws IOException {

        try (Stream<Path> paths = Files.list(Paths.get(this.contextsPath))) {

            paths.forEach(path -> saveContextIfPresent(path, "exemple-service-customer.xml"));
        }

    }

    @SneakyThrows
    private void saveContextIfPresent(Path path, String contextName) {

        try (Stream<Path> paths = Files.list(path)) {

            paths.filter((Path p) -> Paths.get(contextName).equals(p.getFileName())).findFirst().ifPresent(((Path p) -> {
                var contextKey = path.getFileName().toString();
                saveContextIfUpdated(contextKey, p);
            }));
        }

    }

    @SneakyThrows
    private void saveContextIfUpdated(String contextKey, Path contextPath) {

        long checksum = FileUtils.checksumCRC32(contextPath.toFile());
        if (checksum != checksumApplicationContexts.getOrDefault(contextKey, 0L)) {

            var configLocation = contextPath.toString();
            LOG.debug("loading context {}", configLocation);

            scriptApplicationContexts.put(contextKey,
                    new FileSystemXmlApplicationContext(new String[] { "file:///" + configLocation }, this.applicationContext));
            checksumApplicationContexts.put(contextKey, checksum);
        }

    }

}
