package com.exemple.service.api.common;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.manifests.Manifests;
import com.jcabi.manifests.ServletMfs;

public final class ManifestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ManifestUtils.class);

    private ManifestUtils() {

    }

    public static String version(ServletContext servletContext) throws IOException {

        Manifests.DEFAULT.clear();
        Manifests.DEFAULT.append(new ServletMfs(servletContext));

        String version = Manifests.DEFAULT.get("Implementation-Version");

        LOG.debug("La version du projet est {}", version);

        return version;
    }

    public static String buildTime(ServletContext servletContext) throws IOException {

        Manifests.DEFAULT.clear();
        Manifests.DEFAULT.append(new ServletMfs(servletContext));

        String buildTime = Manifests.DEFAULT.get("Build-Time");

        LOG.debug("Le build date du {}", buildTime);

        return buildTime;
    }

}
