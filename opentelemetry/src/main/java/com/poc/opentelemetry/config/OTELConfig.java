package com.poc.opentelemetry.config;


import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class OTELConfig {


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public TracerProvider tracerProvider() {
        return OpenTelemetrySdk.builder().build().getTracerProvider();
    }

    @Bean
    public ZipkinSpanExporter zipkinSpanExporter() {
        final String URL = "http://localhost:9411/api/v2/spans";
        return ZipkinSpanExporter.builder()
                .setEndpoint(URL)
                .build();
    }

    @Bean
    public Tracer tracer() {

        final String applicationId = "APPLICATION-OPENTELEMETRY";

        Resource serviceNameResource =
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, applicationId));

        TracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(zipkinSpanExporter()))
                .setResource(Resource.getDefault().merge(serviceNameResource))
                .build();

        return tracerProvider.get(applicationId,"server-application");
    }

    @Bean
    public W3CTraceContextPropagator propagator(){
        return W3CTraceContextPropagator.getInstance();
    }

}
