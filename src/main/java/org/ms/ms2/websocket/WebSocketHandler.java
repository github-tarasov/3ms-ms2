package org.ms.ms2.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ms.dto.Message;
import org.ms.ms2.service.MS2Service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@AllArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private ObjectMapper objectMapper;
    private MS2Service ms2Service;

    private Tracer tracer;

    @Override
    @Transactional
    @WithSpan("Handle Message from MS2 websocket")
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws JsonProcessingException {
        Span span = tracer.spanBuilder("WebSocket handler")
                        .setAttribute(AttributeKey.stringKey("message"), textMessage.getPayload())
                        .startSpan();
        Message message = objectMapper.readValue(textMessage.getPayload(), Message.class);
        log.debug("Handle Message from websocket: {}", message);
        ms2Service.forwardMessage(message);
        span.end();
    }
}
