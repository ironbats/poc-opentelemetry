package com.poc.application.telemetry.interceptor;

import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.http.HttpHeaders;

public class HttpHeadersSetter implements TextMapSetter<HttpHeaders> {
    @Override
    public void set(HttpHeaders httpHeaders, String s, String s1) {
        httpHeaders.add(s,s1);
    }
}
