package com.exemple.service.api.launcher.actuate;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/feature/actuate", glue = {
        "com.exemple.service.api.launcher.core",
        "com.exemple.service.api.launcher.actuate" })
public class ActuatorIT {

}
