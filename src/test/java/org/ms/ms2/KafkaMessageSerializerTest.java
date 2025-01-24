package org.ms.ms2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ms.dto.Message;
import org.ms.ms2.kafka.KafkaMessageSerializer;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class KafkaMessageSerializerTest {

    private final KafkaMessageSerializer kafkaMessageSerializer = new KafkaMessageSerializer();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Message message;

    @BeforeEach
    public void beforeEach() {
        message = Message.builder()
                .sessionId(123)
                .service1Timestamp(new Date())
                .service2Timestamp(new Date())
                .build();
    }

    @Test
    public void serialize_Null() {
        byte[] result = kafkaMessageSerializer.serialize("ms3", null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void deserialize_NotNull() throws JsonProcessingException {
        byte[] data = objectMapper.writeValueAsBytes(message);

        byte[] result = kafkaMessageSerializer.serialize("ms3", message);

        assertThat(result, equalTo(data));
    }

}
