package com.exemple.service.launcher.swagger;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import io.cucumber.junit.platform.engine.Constants;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("feature/swagger")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.exemple.service.launcher.core, "
        + "com.exemple.service.launcher.swagger")
public class SwaggerIT {

}
