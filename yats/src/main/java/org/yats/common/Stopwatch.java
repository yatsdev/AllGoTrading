package org.yats.common;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class Stopwatch {

    public Duration getElapsedTime() {
        return new Duration(startTime, DateTime.now());
    }

    public void reset() {
        startTime = DateTime.now();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public Stopwatch() {
        reset();
    }

    /////////////////////////////////////////////////////////////

    private DateTime startTime;
}