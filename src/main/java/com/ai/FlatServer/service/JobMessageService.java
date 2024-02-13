package com.ai.FlatServer.service;

import com.ai.FlatServer.domain.dto.message.RequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobMessageService {

    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void sendRequestMessage(RequestMessage messageDto) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
    }
}
