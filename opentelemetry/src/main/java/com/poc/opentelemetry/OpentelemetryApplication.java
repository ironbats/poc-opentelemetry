package com.poc.opentelemetry;


import io.opentelemetry.api.trace.Tracer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpentelemetryApplication {

    @Autowired
    private Tracer tracer;


    public static void main(String[] args) {
        SpringApplication.run(OpentelemetryApplication.class, args);
    }

}
