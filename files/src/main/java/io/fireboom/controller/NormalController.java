package io.fireboom.controller;

import io.fireboom.plugins.HealthReports;
import io.fireboom.server.hook.Health;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("")
public class NormalController {

    @GetMapping("/health")
    public Health health() {
        return new Health(HealthReports.report, HealthReports.okStatus);
    }
}
