package org.yats.messagebus;

import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.WaitingLinkedBlockingQueue;


public class BufferingReceiver<T> implements Runnable, IAmCalledBack {

    final Logger log = LoggerFactory.getLogger(BufferingReceiver.class);

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
    public void onCallback() {
        while(receiver.hasMoreMessages()) {
            buffer.add(receiver.get());
            if(buffer.size()>5) log.debug("buffer size "+buffer.size());
        }
    }

    @Override
    public void run() {
        while(!shutdown) {
            try {
                Thread.yield();
                //TODO: replace with waiting for the buffer to fill with one element
                buffer.waitNotEmpty();
                observer.onCallback();
            } catch(ShutdownSignalException e) {
                shutdown=true;
                log.debug("closed connection of receiver.");
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
        receiver.start();
    }
//todo: close procedure for threads would be nice
//    public void close() {
//        close=true;
//        thread.interrupt();
//    }

    public void close() {
        receiver.close();
    }

    public void setObserver(IAmCalledBack observer) {
        this.observer = observer;
    }

    public BufferingReceiver(Class<T> _tClass, String _exchange, String _topic, String _rabbitServerAddress) {
        receiver = new CallingReceiver<T>(_tClass,_exchange, _topic, _rabbitServerAddress);
        thread = new Thread(this);
        buffer = new WaitingLinkedBlockingQueue<T>();
        observer = new IamCalledBackDummy();
        shutdown=false;
        receiver.setObserver(this);
    }

    private CallingReceiver<T> receiver;
    private IAmCalledBack observer;
    private Thread thread;
    private WaitingLinkedBlockingQueue<T> buffer;
    private boolean shutdown;

    private static class IamCalledBackDummy implements IAmCalledBack {
        @Override
        public void onCallback() {
        }
    }

} // class
