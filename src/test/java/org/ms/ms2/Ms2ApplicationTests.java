package org.ms.ms2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;
import org.ms.dto.Message;
import org.ms.ms2.service.MS2Service;
import org.ms.ms2.websocket.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@TestPropertySource(locations = "/application-dev.properties")
class Ms2ApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private MS2Service ms2Service;

    @Autowired
    private KafkaTemplate<String, Message> kafkaTemplate;

    @Autowired
    private Tracer tracer;

    @Test
    void contextLoads() {
        assertThat(objectMapper, is(notNullValue()));
        assertThat(webSocketHandler, is(notNullValue()));
        assertThat(ms2Service, is(notNullValue()));
        assertThat(kafkaTemplate, is(notNullValue()));
        assertThat(tracer, is(notNullValue()));
    }

}
