package com.exemple.service.launcher.account;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import io.cucumber.junit.platform.engine.Constants;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("feature/account")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.exemple.service.launcher.core, "
        + "com.exemple.service.launcher.account, "
        + "com.exemple.service.launcher.authorization, "
        + "com.exemple.service.launcher.login")
public class AccountIT {

}
