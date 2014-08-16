package org.yats.messagebus;


import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Receiver<T> {

    final Logger log = LoggerFactory.getLogger(Receiver.class);


    private String rabbitServerAddress;

//    public String getLastTopic() {
//        return lastTopic;
//    }

    public T receive()
    {
        try {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//            lastTopic = delivery.getEnvelope().getRoutingKey();
            String jsonMessage = new String(delivery.getBody());
//            log.debug("Receiver: "+jsonMessage);
            T msg = deserializer.convertFromString(jsonMessage);
//            log.debug("Receiver parsed: "+msg.toString());
            return msg;
        } catch(ShutdownSignalException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            log.error(e.getMessage());
            System.exit(-1);
            throw new RuntimeException("Receiver: problem with receiving message! " + e.getMessage());
        }
    }

    public T tryReceive(int timeoutMillisec)
    {
        try {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeoutMillisec);
            if(delivery==null) return null;
//            lastTopic = delivery.getEnvelope().getRoutingKey();
            String jsonMessage = new String(delivery.getBody());
            return deserializer.convertFromString(jsonMessage);
        } catch (Throwable e) {
            e.printStackTrace();
            log.error(e.getMessage());
            System.exit(-1);
            throw new RuntimeException("Receiver: problem with receiving message! " + e.getMessage());
        }
    }

    public void close() {
        try {
            //channel.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new RuntimeException("Receiver: problem with close! " + e.getMessage());
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
        } catch (Throwable e) {
            e.printStackTrace();
            log.error(e.getMessage());
            System.exit(-1);
            throw new RuntimeException("Receiver could not create rabbitmq connection.");
        }
    }


    public Receiver(Class<T> _tClass, String _exchange, String _topic, String _rabbitServerAddress) {
        deserializer = new Deserializer<T>(_tClass);
        exchangeName = _exchange;
        topic = _topic;
//        lastTopic="";
        rabbitServerAddress=_rabbitServerAddress;
        init();
    }

    Deserializer<T> deserializer;
    ConnectionFactory factory;
    Connection connection;
    Channel channel;
    String queueName;
    QueueingConsumer consumer;
    private String exchangeName;
    private String topic;


//    private String lastTopic;
}
