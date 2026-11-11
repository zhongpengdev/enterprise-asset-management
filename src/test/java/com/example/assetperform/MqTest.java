package com.example.assetperform;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.assetperform.config.RabbitConfig;

@SpringBootTest 
public class MqTest {
    @Autowired 
    private RabbitTemplate rabbitTemplate;

    @Test 
    void sendMessage() {
        String message = "Hello, RabbitMQ!";
        rabbitTemplate.convertAndSend("", RabbitConfig.TEST_QUEUE, message);
        System.out.println("Sent message: " + message);
    }

    @Test 
    void sendMessageDefault() {
        String queueName = "hello.queue1";
        String message = "Hello, MQ";

        rabbitTemplate.convertAndSend(queueName, message);
    }

    @Test
    void receiveMessage() {
        String queueName = "hello.queue1";

        
    }
}
