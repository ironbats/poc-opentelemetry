package com.poc.application.telemetry.service;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ApplicationTelemetryService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Tracer tracer;


    public ResponseEntity<String> responseEntity() {


        final Span spanV1 = tracer.spanBuilder("application-process").startSpan();

        try (Scope scope = spanV1.makeCurrent())
        {

            spanV1.addEvent("application-process-" + UUID.randomUUID());
            spanV1.addEvent("application-process-time", Instant.now());

            spanV1.setStatus(StatusCode.OK);
            spanV1.setAttribute("process-otel", "process-ok");
            spanV1.setAttribute(SemanticAttributes.HTTP_METHOD, "POST");
            spanV1.setAttribute("service-one", "AMQP");
            spanV1.end();
            Thread.sleep(1000);

        } catch (Exception cause) {
            spanV1.recordException(cause);
            try {
                throw cause;
            } catch (InterruptedException e) {
                spanV1.recordException(e);
                throw new RuntimeException(e);
            }
        } finally {
            spanV1.end();
        }

        TraceFlags traceFlags = TraceFlags.fromHex(spanV1.getSpanContext().getTraceFlags().asHex(),0);
        TraceState traceState = TraceState.builder().build();
        SpanContext spanContext = SpanContext.createFromRemoteParent(spanV1.getSpanContext().getTraceId(),spanV1.getSpanContext().getSpanId(),traceFlags,traceState);
        Context context = Context.current().with(Span.wrap(spanContext));

        final Span span = tracer.spanBuilder("application-server")
                .setParent(context).setSpanKind(SpanKind.SERVER).startSpan();
        final String applicationInit = UUID.randomUUID().toString();


        try (Scope scope = span.makeCurrent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("traceId", span.getSpanContext().getTraceId());
            headers.set("spanId", span.getSpanContext().getSpanId());
            headers.set("traceFlags", span.getSpanContext().getTraceFlags().asHex());

            Map<String, String> traceStateMap = new HashMap<>();
            traceStateMap.putAll(span.getSpanContext().getTraceState().asMap());
            log.info(traceStateMap.toString());
            log.info("map: " + span.getSpanContext().getTraceState().asMap());
            log.info("traceid: " + span.getSpanContext().getTraceId());
            log.info("spanid: " + span.getSpanContext().getSpanId());
            log.info("traceflags: " + span.getSpanContext().getTraceFlags().asHex());


            HttpEntity<?> request = new HttpEntity<>(headers);

            span.addEvent("application-client-" + applicationInit);
            ResponseEntity<String> requestEntity = restTemplate
                    .exchange("http://localhost:8080/telemetry/application", HttpMethod.GET, request, String.class, traceStateMap);


            span.setStatus(StatusCode.OK);
            span.setAttribute("application-otel", "application-ok");
            span.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
            span.setAttribute("service-one", "http");

            span.end();
            return requestEntity;

        } catch (Exception cause) {

            log.error(cause.getMessage());
            span.recordException(cause);
            final String applicationErrorId = "application-error-" + UUID.randomUUID();
            span.setAttribute("application-otel-error-", "application-error");
            span.addEvent("errorTransactionId-" + applicationErrorId);
            span.setStatus(StatusCode.ERROR);
        } finally {
            span.end();
        }

        return ResponseEntity.status(400).build();
    }
}
