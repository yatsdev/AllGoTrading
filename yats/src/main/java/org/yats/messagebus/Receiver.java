package org.yats.messagebus;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import flexjson.JSONDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Receiver<T> {

    final Logger log = LoggerFactory.getLogger(Receiver.class);

    public Receiver(Class<T> _tClass, String _exchange, String _topic, String _rabbitServerAddress) {
        tClass=_tClass;
        exchangeName = _exchange;
        topic = _topic;
        lastTopic="";
        rabbitServerAddress=_rabbitServerAddress;
        init();
    }

    private String rabbitServerAddress;

    public String getLastTopic() {
        return lastTopic;
    }

    public T receive()
    {
        try {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            lastTopic = delivery.getEnvelope().getRoutingKey();
            String jsonMessage = new String(delivery.getBody());
            log.debug("Receiver: "+jsonMessage);
            T msg = new JSONDeserializer<T>().deserialize(jsonMessage, tClass);
            log.debug("Receiver parsed: "+msg.toString());
            return msg;
        } catch (InterruptedException e) {
//            e.printStackTrace();
            throw new RuntimeException("Receiver: problem with receiving message!");
        }
    }

    public T tryReceive(int timeoutMillisec)
    {

        try {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeoutMillisec);
            if(delivery==null) return null;
            lastTopic = delivery.getEnvelope().getRoutingKey();
            String jsonMessage = new String(delivery.getBody());
            T msg = new JSONDeserializer<T>().deserialize(jsonMessage, tClass);
            return msg;
        } catch (InterruptedException e) {
//            e.printStackTrace();
            throw new RuntimeException("Receiver: problem with receiving message!");
        }
    }

    private void init() {
        try {
            factory = new ConnectionFactory();
            factory.setHost(rabbitServerAddress);
            connection = null;
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "topic");
            queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, exchangeName, topic);
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Receiver could not create rabbitmq connection.");
        }
    }

    ConnectionFactory factory;
    Connection connection;
    Channel channel;
    String queueName;
    QueueingConsumer consumer;
    private String exchangeName;
    private String topic;
    private String lastTopic;
    private Class<T> tClass;
}
