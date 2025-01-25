package org.ms.ms2.websocket;


import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;


@Configuration
public class OpenTelemetryConfiguration {

    @Value("${jaeger.host}")
    private String jaegerHost;

    @Value("${spring.application.name}")
    private String springApplicationName;

    @Bean
    public Tracer tracer() {
        SpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint("http://" + jaegerHost + ":4318/v1/traces")
                .setTimeout(Duration.ofSeconds(10))
                .build();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(SERVICE_NAME, springApplicationName)));
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.builder(spanExporter).build())
                .setResource(resource)
                .build();
        OpenTelemetrySdk openTelemetrySdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(sdkTracerProvider)
                        .setMeterProvider(
                                SdkMeterProvider.builder()
                                        .registerMetricReader(
                                                PeriodicMetricReader.builder(
                                                                        OtlpHttpMetricExporter.builder()
                                                                                .setEndpoint("http://" + jaegerHost + ":4318/v1/metrics")
                                                                                .build()
                                                        )
                                                        .setInterval(Duration.ofMillis(1000))
                                                        .build()
                                        ).build()
                        )
                        .setLoggerProvider(
                                SdkLoggerProvider.builder()
                                        .addLogRecordProcessor(
                                                BatchLogRecordProcessor.builder(OtlpHttpLogRecordExporter.builder()
                                                                                        .setEndpoint("http://" + jaegerHost + ":4318/v1/logs")
                                                                                        .setTimeout(Duration.ofSeconds(10))
                                                                                        .build()
                                                                        ).build()
                                        ).build()
                        )
                        .buildAndRegisterGlobal();
        return openTelemetrySdk.getTracer("scopeName");
    }
}
