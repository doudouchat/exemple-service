package com.exemple.service.api.launcher.core.info;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.exemple.service.api.core.ApiContext;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/info")
@RequiredArgsConstructor
@Hidden
public class InfoApi {

    private final ApiContext apiContext;

    @GetMapping
    public String template(Model model) {
        model.addAttribute("version", apiContext.getVersion());
        model.addAttribute("buildTime", apiContext.getBuildTime());

        return "info";
    }
}
