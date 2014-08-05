package org.yats.messagebus.messages;

import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;

public class KeyValueMsg {

    public String getTopic() {
        return KeyValueMsg.class.getSimpleName();
    }

    public IProvideProperties toProperties() {
        IProvideProperties p = new PropertiesReader();
        String[] pairs = message.split(";");
        for(String pair : pairs) {
            if(pair.length()==0) continue;
            if(pair.charAt(0)=='=') continue;
            String[] kv = pair.split("=");
            if(kv.length==0) continue;
            if(kv[0].length()==0) continue;
            String value = (kv.length==1) ? "" : kv[1];
            p.set(kv[0],value);
        }
        return p;
    }

    public static KeyValueMsg fromProperties(IProvideProperties p) {
        KeyValueMsg m = new KeyValueMsg();
        StringBuilder b = new StringBuilder();
        boolean first=true;
        for(String s : p.getKeySet()) {
            if(!first) { b.append(";"); }
            b.append(s);
            b.append("=");
            b.append(p.get(s));
            first=false;
        }
        m.message = b.toString();
        return m;
    }

    public KeyValueMsg() {
        message = "";
    }

    public String message;

} // class
