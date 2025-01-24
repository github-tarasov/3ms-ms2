package org.ms.ms2;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ms.dto.Message;
import org.ms.ms2.service.MS2Service;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MS2ServiceTest {
    @InjectMocks
    private MS2Service ms2Service;

    @Mock
    private KafkaTemplate<String, Message> kafkaTemplate;

    private Message message;

    @BeforeEach
    public void beforeEach() {
        message = Message.builder()
                .sessionId(123)
                .service1Timestamp(new Date())
                .build();
    }

    @Test
    public void sendMessageToMS3_success() {
        CompletableFuture<SendResult<String, Message>> future = new CompletableFuture<>();
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("", 0), 0, 0, 0, 0, 0);
        SendResult sendResult = new SendResult(null, recordMetadata);
        future.obtrudeValue(sendResult);
        when(kafkaTemplate.send(any(), any(Message.class))).thenReturn(future);

        ms2Service.forwardMessage(message);

        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate, times(1)).send(any(), argument.capture());
        assertThat(message.getSessionId(), equalTo(argument.getValue().getSessionId()));
        assertThat(message.getService1Timestamp(), equalTo(argument.getValue().getService1Timestamp()));
        assertThat(message.getService2Timestamp(), is(notNullValue()));
    }

    @Test
    public void sendMessageToMS3_asyncException() {
        CompletableFuture<SendResult<String, Message>> future = new CompletableFuture<>();
        future.obtrudeException(new RuntimeException());
        when(kafkaTemplate.send(any(), any(Message.class))).thenReturn(future);

        ms2Service.forwardMessage(message);

        verify(kafkaTemplate, times(1)).send(any(), any(Message.class));
    }

}
