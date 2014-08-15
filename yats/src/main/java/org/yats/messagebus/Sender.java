package org.yats.messagebus;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Sender<T>
{
    final Logger log = LoggerFactory.getLogger(Sender.class);

    public void publish(String topic, T msg)
    {
        try{
            String msgString = serializer.convertToString(msg);
            channel.basicPublish(exchangeName, topic, null, msgString.getBytes());
        } catch(IOException e)
        {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new RuntimeException(e.toString());
        } catch(Throwable t) {
            t.printStackTrace();
            log.error(t.getMessage());
            throw new RuntimeException(t.toString());
        }
    }

    private void close()  {
        try{
            channel.close();
            connection.close();
        } catch(Throwable e)
        {
            e.printStackTrace();
            log.error(e.getMessage());
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
        } catch(Throwable e)
        {
            e.printStackTrace();
            log.error(e.getMessage());
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
