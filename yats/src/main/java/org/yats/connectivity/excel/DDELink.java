package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;

public class DDELink implements IProvideDDEConversation {

    public static class ConversationException extends RuntimeException {
        public ConversationException(String s) { super(s);
        }
    }


    @Override
    public void stopAdvice(String s) {
        try {
            c.stopAdvice(s);
        } catch (DDEException e) {
            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            c.disconnect();
        } catch (DDEException e) {
            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    @Override
    public void poke(String where, String what)  {
        try {
            c.poke(where,what);
        } catch (DDEException e) {
            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    @Override
    public String request(String what) {
        try {
            return c.request(what);
        } catch (DDEException e) {
            e.printStackTrace();
            throw new ConversationException(e.getMessage());
        }
    }

    @Override
    public void startAdvice(String s) {
        try {
            c.startAdvice(s);
        } catch (DDEException e) {
            e.printStackTrace();
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
