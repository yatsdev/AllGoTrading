package org.yats.connectivity.excel;

import com.pretty_tools.dde.client.DDEClientEventListener;

public interface IProvideDDEConversation {

    void disconnect();
    void stopAdvice(String s);
    void poke(String where, String what);
    String request(String what);
    void startAdvice(String s);
    void setEventListener(DDELinkEventListener listener);
    void setTimeout(int millis);
    void connect(String where, String what);
}
