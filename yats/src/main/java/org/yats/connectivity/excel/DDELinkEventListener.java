package org.yats.connectivity.excel;

public interface DDELinkEventListener {
    void onDisconnect();
    void onItemChanged(java.lang.String s, java.lang.String s1, java.lang.String s2);
}
