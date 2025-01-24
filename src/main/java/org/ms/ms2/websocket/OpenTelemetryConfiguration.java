package org.ms.ms2.websocket;


import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;

@Configuration
public class OpenTelemetryConfiguration {

    @Value("${jaeger.server}")
    private String jaegerServer;

    @Value("${spring.application.name}")
    private String springApplicationName;

    @Bean
    public Tracer tracer() {
        SpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(jaegerServer)
                .build();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(SERVICE_NAME, springApplicationName)));
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setResource(resource)
                .build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .build();
        GlobalOpenTelemetry.set(openTelemetry);
        return openTelemetry.getTracer("scopeName");
    }
}
