package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDELink implements IProvideDDEConversation {

    final Logger log = LoggerFactory.getLogger(DDELink.class);


    public static class ConversationException extends RuntimeException {
        public ConversationException(String s) { super(s);
        }
    }


    @Override
    public void stopAdvice(String s) {
        try {
            c.stopAdvice(s);
        } catch (DDEException e) {
//            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            c.disconnect();
        } catch (DDEException e) {
//            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    @Override
    public void poke(String where, String what)  {
        try {
//            DateTime startSheet = DateTime.now();
            c.poke(where,what);
//            System.out.print("what:"+what.length());
//            Duration d = new Duration(startSheet, DateTime.now());
//            total+=d.getMillis();
//            System.out.println("poke: total="+total + " thistime="+d.getMillis() + " for size:"+what.length());

        } catch (DDEException e) {
//            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    static long total = 0;

    @Override
    public String request(String what) {
        try {
            return c.request(what);
        } catch (DDEException e) {
//            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    @Override
    public void startAdvice(String s) {
        try {
            c.startAdvice(s);
        } catch (DDEException e) {
//            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }


    @Override
    public void setEventListener(DDELinkEventListener listener) {
        NativeEventListener el = new NativeEventListener(listener);
        c.setEventListener(el);
    }

    @Override
    public void setTimeout(int millis) {
        c.setTimeout(millis);
    }

    @Override
    public void connect(String where, String what) {
        log.info("Connecting with "+where+" to sheet "+what);
        try {
            c.connect(where, what);
        } catch (DDEException e) {
            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    public DDELink() {
        c = new DDEClientConversation();
    }

    DDEClientConversation c;



    private static class NativeEventListener implements DDEClientEventListener {
        @Override
        public void onDisconnect() {
            listener.onDisconnect();
        }

        @Override
        public void onItemChanged(String s, String s2, String s3) {
            listener.onItemChanged(s,s2,s3);
        }

        private NativeEventListener(DDELinkEventListener listener) {
            this.listener = listener;
        }

        DDELinkEventListener listener;
    }

}
