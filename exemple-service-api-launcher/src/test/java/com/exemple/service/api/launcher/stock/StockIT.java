package com.exemple.service.api.launcher.stock;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/feature/stock", glue = {
        "com.exemple.service.api.launcher.core",
        "com.exemple.service.api.launcher.stock" })
public class StockIT {

}
