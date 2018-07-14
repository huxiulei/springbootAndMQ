package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class WorkX {
    private static final String TASK_QUEUE_NAME = "task_queue";
    public static final Logger logger = LoggerFactory.getLogger(WorkX.class);

    public static void main(String[] args) throws IOException, TimeoutException {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        logger.info("Worker1正在等待消息~~~~~~~~~~~");

        /**
         * channel.basicQos(1);保证一次只分发一个 。autoAck是否自动回复，如果为true的话，每次生产者只要发送信息就会从内存中删除，
         * 那么如果消费者程序异常退出，就无法获取数据，我们当然是不希望出现这样的情况，所以才去手动回复，每当消费者收到并处理信息然后在通知生成者。
         * 最后从队列中删除这条信息。如果消费者异常退出，如果还有其他消费者，那么就会把队列中的消息发送给其他消费者，如果没有，等消费者启动时候再次发送
         */
        //每次从队列获取的数量  如果消息没有被确认  则不接收其他消息
        channel.basicQos(0,0,false);

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.info("Worker1 接收到消息 {}",message);
                try {
                    //throw new Exception();
                    doWork(message);
                } catch (Exception e) {
                    /**
                     *   channel.basicRecover(true);
                         channel.basicReject(1,false);
                         channel.basicNack(1,true,true);
                     *   basicRecover：是路由不成功的消息可以使用recovery重新发送到队列中。
                     basicReject：是接收端告诉服务器这个消息我拒绝接收,不处理,可以设置是否放回到队列中还是丢掉，而且只能一次拒绝一个消息,官网中有明确说明不能批量拒绝消息，为解决批量拒绝消息才有了basicNack。
                     basicNack：可以一次拒绝N条消息，客户端可以设置basicNack方法的multiple参数为true，服务器会拒绝指定了delivery_tag的所有未确认的消息(tag是一个64位的long值，最大值是9223372036854775807)。
                     */
                    //channel.abort();
                    //if("Hello RabbitMQ-5".equals(message)){
                        channel.basicRecover(true);
                    //}
                } finally {
                    //logger.info("Worker1接收消息{} 完成",message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        boolean autoAck = false;
        //消息消费完成确认
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
    }

    private static void doWork(String task) {
        try {
            Thread.sleep(1000); // 暂停1秒钟
            if("Hello RabbitMQ-4".equals(task) || "Hello RabbitMQ-5".equals(task)){
                logger.info("Hello RabbitMQ-4 和  Hello RabbitMQ-5 想要抛异常");
                throw new RuntimeException("消息 Hello RabbitMQ-4 和  Hello RabbitMQ-5 都发送失败, 需要重发.. 但是我只想重发 5 ");
            }
        } catch (InterruptedException _ignored) {
            //Thread.currentThread().interrupt();
            _ignored.printStackTrace();
        }
    }
}