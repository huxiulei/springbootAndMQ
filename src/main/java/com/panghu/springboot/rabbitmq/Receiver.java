package com.panghu.springboot.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName: Receiver.java
 * @Description: TODO 用于接收Demo4SpringBootApplication中的rabbitmq
 * @Author huxl
 * @Version V1.0
 * @Date 2017/8/7 16:39
 */
@Component
public class Receiver {
    public static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    @RabbitListener(queues = "huhuhu-queue")
    public void receiveMessage(String message){

        logger.info("Receiver接收到的消息是:{}",message);
    }
}
