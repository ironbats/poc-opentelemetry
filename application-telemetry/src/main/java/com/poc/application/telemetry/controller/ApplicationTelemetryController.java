package com.poc.application.telemetry.controller;

import com.poc.application.telemetry.service.ApplicationTelemetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class ApplicationTelemetryController {

    @Autowired
    private ApplicationTelemetryService applicationTelemetryService;


    @GetMapping
    public ResponseEntity<String> getTransactions(){
        return applicationTelemetryService.responseEntity();
    }

}
