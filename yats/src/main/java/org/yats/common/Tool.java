package org.yats.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Tool {


    public static void sleepABit() {
        sleepFor(500);
    }

    public static void sleepFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getUTCTimestampString() {
        return getUTCTimestamp().toString();
    }

    public static DateTime getUTCTimestamp() {
        return DateTime.now(DateTimeZone.UTC);
    }

}
