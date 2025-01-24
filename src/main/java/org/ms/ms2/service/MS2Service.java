package org.ms.ms2.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ms.dto.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class MS2Service {
    private final KafkaTemplate<String, Message> kafkaTemplate;

    @Value("${kafka.topic}")
    private String kafkaTopic;

    public void forwardMessage(Message message) {
        message.setService2Timestamp(new Date());
        sendKafkaMessage(message);
    }

    private void sendKafkaMessage(Message message) {
        kafkaTemplate.send(kafkaTopic, message).whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Sent kafka message: {} with offset: {}", message, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send kafka message: {} due to: {}", message, ex.getMessage());
                throw new KafkaException(ex.getMessage(), ex);
            }
        });
    }

}
