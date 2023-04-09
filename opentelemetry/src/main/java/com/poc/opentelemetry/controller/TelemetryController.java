package com.poc.opentelemetry.controller;

import com.poc.opentelemetry.service.TelemetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/telemetry")
public class TelemetryController {

    @Autowired
    private TelemetryService telemetryService;

    @GetMapping("/application")
    public List<String> getInformation(@RequestHeader("traceId") String traceId,
                                       @RequestHeader("spanId") String spanId,
                                       @RequestHeader("traceFlags") String traceFlags) {

        return telemetryService.callTransactions(traceId, spanId, traceFlags);
    }

}
