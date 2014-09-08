package org.yats.trading;

import org.joda.time.DateTime;

public class TimedCallback {

    public boolean isTimeToCall(DateTime now) {
        return now.isAfter(time) || now.isEqual(time);
    }

    public TimedCallback(DateTime time, IAmCalledBackInTime callback) {
        this.time = time;
        this.callback = callback;
    }

    public void call()
    {
        callback.onTimerCallback();
    }

    private DateTime time;
    private IAmCalledBackInTime callback;
}
