package org.yats.messagebus;

import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;

import java.util.concurrent.LinkedBlockingQueue;

public class CallingReceiver<T> extends Receiver<T> implements Runnable {

    final Logger log = LoggerFactory.getLogger(CallingReceiver.class);

    public boolean hasMoreMessages()
    {
        return buffer.size()>0;
    }

    public T get() {
        try {
            return buffer.take();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void run() {
        while(!shutdown) {
            try {
                Thread.yield();
                T m = receive();
                log.info("got:" + m.toString());
                buffer.add(m);
                if(buffer.size()>1)
                    log.error("buffer should never be greater 1. size:"+buffer.size());
                observer.onCallback();
            } catch(ShutdownSignalException e) {
                shutdown=true;
                log.debug("closed connection");
            } catch(Throwable t) {
                log.error(t.getMessage());
                t.printStackTrace();
                System.exit(-1);
                throw new RuntimeException("Problem during run! "+t.getMessage());
            }
        }
    }

    public void start() {
        thread.start();
    }
//todo: close procedure for threads would be nice
//    public void close() {
//        close=true;
//        thread.interrupt();
//    }

    public void setObserver(IAmCalledBack observer) {
        this.observer = observer;
    }

    public CallingReceiver(Class<T> _tClass, String _exchange, String _topic, String _rabbitServerAddress) {
        super(_tClass, _exchange, _topic, _rabbitServerAddress);
        thread = new Thread(this);
        buffer = new LinkedBlockingQueue<T>();
        observer = new IamCalledBackDummy();
        shutdown=false;
    }


    private IAmCalledBack observer;
    private Thread thread;
    private LinkedBlockingQueue<T> buffer;
    private boolean shutdown;

    private static class IamCalledBackDummy implements IAmCalledBack {
        @Override
        public void onCallback() {
        }
    }

} // class





