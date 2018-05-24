package com.microwise.tracker.iot;

import com.aliyun.openservices.ons.api.*;
import com.google.gson.Gson;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by Administrator on 2018/5/2.
 */
@Component
public class Receiver implements ApplicationRunner {
    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Value("${rabbitmq.enable}")
    private Boolean enable;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        Properties properties = new Properties();
        // 您在控制台创建的 Consumer ID
        properties.put(PropertyKeyConst.ConsumerId, "CID_microwise_tracer");
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, "LTAIoVXMSH0YO4dx");
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, "zIpJSWSdGday8USeeJm8cTtsaQU8Am");
        // 设置 TCP 接入域名（此处以公共云生产环境为例）
        properties.put(PropertyKeyConst.ONSAddr,
                "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet");
        Consumer consumer = ONSFactory.createConsumer(properties);
        //订阅全部Tag
        consumer.subscribe("tracer", "", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                String messageJson = new String(message.getBody());
                System.out.println("consumer Receive: " + messageJson);
                if (enable) {
                    sendMessageToRabbitMQ(messageJson);
                }
                return Action.CommitMessage;
            }
        });
        consumer.start();
        System.out.println("Consumer Started");
    }

    public void sendMessageToRabbitMQ(String message) {
        /*Gson gson = new Gson();
        String trackerData = gson.toJson(message);*/
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitProperties.getHost(), rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
        AmqpAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(new Queue("/topic/tracker-data"));
        AmqpTemplate template = new RabbitTemplate(connectionFactory);
        template.convertAndSend("/topic/tracker-data", message);
        connectionFactory.destroy();
        simpMessagingTemplate.convertAndSend("/topic/tracker-data", message);
    }
}
