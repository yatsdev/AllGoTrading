package org.yats.messagebus;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

public class Sender<T>
{

    public void publish(String topic, T msg)
    {
        try{
            String msgString = serializer.convertToString(msg);
            channel.basicPublish(exchangeName, topic, null, msgString.getBytes());
        } catch(IOException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    private void close()  {
        try{
            channel.close();
            connection.close();
        } catch(IOException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public void init() {
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitServerAddress);
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "topic");
        } catch(IOException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public Sender(String _exchangeName, String _rabbitServerAddress)
    {
        serializer = new Serializer<T>();
        exchangeName = _exchangeName;
        rabbitServerAddress=_rabbitServerAddress;
        init();
    }

    protected void finalize( ) throws Throwable
    {
        close();
        super.finalize( );
    }

    private Serializer<T> serializer;
    private Connection connection;
    private Channel channel;
    private String exchangeName;
    private String rabbitServerAddress;

}
