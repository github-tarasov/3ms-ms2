package org.ms.ms2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ms.dto.Message;
import org.ms.ms2.service.MS2Service;
import org.ms.ms2.websocket.WebSocketHandler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.sockjs.transport.handler.DefaultSockJsService;
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebSocketHandlerTest {
    @InjectMocks
    private WebSocketHandler webSocketHandler;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MS2Service ms2Service;

    @Mock
    private Tracer tracer;

    private Message message;

    private final ObjectMapper realObjectMapper = new ObjectMapper();

    @BeforeEach
    public void beforeEach() {
        message = Message.builder()
                .sessionId(123)
                .service1Timestamp(new Date())
                .build();
    }

    @Test
    public void listen() throws Exception {
        doNothing().when(ms2Service).forwardMessage(any(Message.class));
        when(objectMapper.readValue(any(String.class), any(Class.class))).thenReturn(message);
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        when(spanBuilder.setAttribute(any(AttributeKey.class), any())).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(mock(Span.class));
        when(tracer.spanBuilder(any(String.class))).thenReturn(spanBuilder);

        TextMessage textMessage = new TextMessage(realObjectMapper.writeValueAsBytes(message));
        WebSocketSession session = new WebSocketServerSockJsSession(
                                        "",
                                        new DefaultSockJsService(new ConcurrentTaskScheduler(null)),
                                        new BinaryWebSocketHandler(),
                                        null
        );
        webSocketHandler.handleMessage(session, textMessage);

        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        verify(ms2Service, times(1)).forwardMessage(argument.capture());
        assertThat(message, equalTo(argument.getValue()));
    }
}
