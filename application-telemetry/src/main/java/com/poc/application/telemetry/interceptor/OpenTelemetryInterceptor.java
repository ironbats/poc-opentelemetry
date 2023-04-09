package com.poc.application.telemetry.interceptor;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class OpenTelemetryInterceptor implements ClientHttpRequestInterceptor {

    private Tracer tracer;

    public OpenTelemetryInterceptor(){
        final String applicationId = "APPLICATION-OPENTELEMETRY";
        this.tracer = OpenTelemetry.noop().getTracerProvider().get(applicationId,"server-application");
    }



    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        final Span span = tracer.spanBuilder(request.getURI().toString()).startSpan();

        try (Scope scope = span.makeCurrent()) {
            Context context = Context.current().with(Span.wrap(span.getSpanContext()));
            TextMapPropagator textMapPropagator = OpenTelemetry.noop().getPropagators().getTextMapPropagator();
            textMapPropagator.inject(context, request.getHeaders(), new HttpHeadersSetter());
            return execution.execute(request, body);

        } catch (Exception cause) {
            span.recordException(cause);
            throw cause;
        } finally {
            span.end();
        }
    }
}
