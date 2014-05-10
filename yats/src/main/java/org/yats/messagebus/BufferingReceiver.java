package org.yats.messagebus;

import org.yats.common.IAmCalledBack;

import java.util.concurrent.LinkedBlockingQueue;

public class BufferingReceiver<T> extends Receiver<T> implements Runnable {

    public boolean hasMoreMessages()
    {
        return buffer.size()>0;
    }

    public T get() {
        try {
            return buffer.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void run() {
        while(!shutdown) {
            T m = receive();
            buffer.add(m);
            observer.onCallback();
        }
    }

    public void start() {
        thread.start();
    }
//todo: shutdown procedure for threads would be nice
//    public void stop() {
//        shutdown=true;
//        thread.interrupt();
//    }

    public void setObserver(IAmCalledBack observer) {
        this.observer = observer;
    }

    public BufferingReceiver(Class<T> _tClass, String _exchange, String _topic, String _rabbitServerAddress) {
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
