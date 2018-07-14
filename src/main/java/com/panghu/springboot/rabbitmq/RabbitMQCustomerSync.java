package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 代码我们可以看出和生成者一样的，后面的是获取生产者发送的信息，其中envelope主要存放生产者相关信息（比如交换机、路由key等）body是消息实体。
 */
public class RabbitMQCustomerSync {
    private final static String QUEUE_NAME = "rabbitMQ.test.sync";
    public static final Logger logger = LoggerFactory.getLogger(RabbitMQCustomerSync.class);


    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ地址
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //声明要关注的队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        logger.info("RabbitMQCustomerSync 消费端正在等待获取消息~~~~~~~~~~ ");

        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，
        // 告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                logger.info("RabbitMQCustomerSync 消费端获取到的消息是:{}", message);

                /**
                 * channel.basicConsume(QUEUE_NAME, false, consumer); 如果它中设置的是false, 则需要手动ack ,反之不用 true会自动ack
                 * channel.basicAck()第二个参数若是true，则表示小于等于这个envelope.getDeliveryTag()的全部发ack
                 */
                channel.basicAck(envelope.getDeliveryTag(), false);
                /**
                 *   channel.basicRecover(true);
                     channel.basicReject(1,false);
                     channel.basicNack(1,true,true);
                 *  basicRecover：是路由不成功的消息可以使用recovery重新发送到队列中。
                    basicReject：是接收端告诉服务器这个消息我拒绝接收,不处理,可以设置是否放回到队列中还是丢掉，而且只能一次拒绝一个消息,官网中有明确说明不能批量拒绝消息，为解决批量拒绝消息才有了basicNack。
                    basicNack：可以一次拒绝N条消息，客户端可以设置basicNack方法的multiple参数为true，服务器会拒绝指定了delivery_tag的所有未确认的消息(tag是一个64位的long值，最大值是9223372036854775807)。
                 */
            }
        };
        //消费消息,其中的false表示显示的调用basicAck，告诉MQ将msg去queue中删除,如果是true 则自动应答
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}