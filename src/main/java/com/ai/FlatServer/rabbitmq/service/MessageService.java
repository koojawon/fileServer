package com.ai.FlatServer.rabbitmq.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RabbitTemplate rabbitTemplate;
    private final Gson gson = new Gson();
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void sendMessage(JsonObject jsonObject) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, gson.toJson(jsonObject));
    }

    public void sendTransformRequestMessage(String fileUid) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "File");
        jsonObject.addProperty("fileUid", fileUid);

        sendMessage(jsonObject);
    }
}
