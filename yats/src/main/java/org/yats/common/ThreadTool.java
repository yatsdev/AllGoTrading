package org.yats.common;

public class ThreadTool {


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

}
