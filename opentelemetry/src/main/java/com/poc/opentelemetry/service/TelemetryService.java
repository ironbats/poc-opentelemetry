package com.poc.opentelemetry.service;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TelemetryService {

    @Autowired
    private Tracer tracer;

    public List<String> callTransactions(String traceId, String spanId, String traceFlagsId) {


        List<String> transactions = new ArrayList<>();

        TraceFlags traceFlags = TraceFlags.fromHex(traceFlagsId,0);
        TraceState traceState = TraceState.builder().build();
        SpanContext spanContext = SpanContext.createFromRemoteParent(traceId,spanId,traceFlags,traceState);
        Context context = Context.current().with(Span.wrap(spanContext));


        final String applicationId = "poc-opentelemetry-" + UUID.randomUUID();
        transactions.add(applicationId);
        final Span span = tracer.spanBuilder("poc-opentelemetry")
                .addLink(spanContext)
                .setParent(context)
                .startSpan();


        try (Scope scope = span.makeCurrent()) {

            span.addEvent("transactionId-" + applicationId);
            span.setStatus(StatusCode.OK);
            span.setAttribute("poc-otel-success-", "otel-ok");
            Thread.sleep(100);
            span.end();
            log.info(applicationId);

        } catch (Throwable cause) {

            final String applicationErrorId = "poc-opentelemetry-error-" + UUID.randomUUID();
            log.error(cause.getMessage());
            transactions.add(applicationErrorId);
            span.setAttribute("poc-otel-error-", "otel-error");
            span.addEvent("errorTransactionId-" + applicationErrorId);
            span.setStatus(StatusCode.ERROR);
            span.recordException(cause);
            log.error(applicationErrorId);
        } finally {
            span.end();
        }
        return transactions;
    }
}
