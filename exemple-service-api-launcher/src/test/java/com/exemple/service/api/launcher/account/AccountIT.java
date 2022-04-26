package com.exemple.service.api.launcher.account;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/feature/account", glue = {
        "com.exemple.service.api.launcher.core",
        "com.exemple.service.api.launcher.account",
        "com.exemple.service.api.launcher.login" })
public class AccountIT {

}
