package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于接收消息，创建一个临时队列，绑定在转发器HEADERS上,并模糊指定键值对
 */
public class HeadersCustomer {

    public static final Logger logger = LoggerFactory.getLogger(HeadersCustomer.class);

    public static void main(String[] args) throws Exception{
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");


        //创建一个新的连接
        Connection connection = factory.newConnection();

        //创建一个通道
        Channel channel = connection.createChannel();

        //声明headers转发器
        channel.exchangeDeclare("header_exchange", BuiltinExchangeType.HEADERS);

        //创建一个临时队列  
        String queueName=channel.queueDeclare().getQueue();  
        //指定headers的匹配类型(all、any)、键值对
        Map<String, Object> headers=new HashMap<String, Object>();
        /**
         * 如果x-match为 any  则有一个值匹配即可
         * 如果x-match为 all  那只要headers中put了值,则需要所有值都和HeadersProducer中headers匹配才行
         */
        headers.put("x-match", "any");
        headers.put("key", "1234561");
        headers.put("value", "huhuhu");
        //绑定临时队列和转发器header_exchange  
        channel.queueBind(queueName, "header_exchange", "", headers);

        logger.info("HeadersCustomer 正在等待接收消息~~~~~~~~");
        Consumer consumer=new DefaultConsumer(channel){  
            @Override  
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body) throws IOException{  
                String msg= new String(body,"UTF-8");
                logger.info("HeadersCustomer接收到消息 routingKey:{} ,msg: {}", envelope.getRoutingKey(),msg);
            }  
        };


        channel.basicConsume(queueName, true, consumer);  
    }  

  
}  