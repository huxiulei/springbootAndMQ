package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 发送消息，HEADERS转发器，通过HEADERS中键值对匹配相应的queue
 * headers 类型的 Exchange 不依赖于 routing key 与 binding key 的匹配规则来路由消息，而是根据发送的消息内容中的 headers 属性进行匹配。
    在绑定 Queue 与 Exchange 时指定一组键值对；当消息发送到 Exchange 时，RabbitMQ 会取到该消息的 headers（也是一个键值对的形式），对比其中的键值对是否完全匹配 Queue 与 Exchange 绑定时指定的键值对；如果完全匹配则消息会路由到该 Queue，否则不会路由到该 Queue。

 */
public class HeadersProducer {

    public static final Logger logger = LoggerFactory.getLogger(HeadersProducer.class);
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

        //定义headers存储的键值对  
        Map<String, Object> headers=new HashMap<String, Object>();  
        headers.put("key", "123456");  
        headers.put("value", "huhuhu");
        //把键值对放在properties  
        Builder properties=new BasicProperties.Builder();  
        properties.headers(headers);  
        properties.deliveryMode(2);//持久化

        String message="Hello RabbitMQ ";
        channel.basicPublish("header_exchange", "" , properties.build(), message.getBytes());

        logger.info("HeadersProducer 产生消息: {}" ,message);

        //关闭通道和连接
        channel.close();
        connection.close();
    }  
      

}  