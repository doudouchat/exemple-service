package com.exemple.service.api.launcher.stock;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import io.cucumber.junit.platform.engine.Constants;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("feature/stock")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.exemple.service.api.launcher.core, "
        + "com.exemple.service.api.launcher.authorization, "
        + "com.exemple.service.api.launcher.stock")
public class StockIT {

}
