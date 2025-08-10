package com.exemple.service.launcher.subscription;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import io.cucumber.junit.platform.engine.Constants;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("feature/subscription")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.exemple.service.launcher.core, "
        + "com.exemple.service.launcher.authorization, "
        + "com.exemple.service.launcher.subscription")
public class SubscriptionIT {

}
