package org.yats.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Date: 4/17/14
 * Time: 1:11 PM
 */

public class UniqueId {

    public boolean isSameAs(UniqueId other) {
        boolean same = (0 == id.compareTo(other.id));
        return same;
    }

    public synchronized static UniqueId create()
    {
        UniqueId id = new UniqueId();
        counter++;
        id.id = "" + counter + "-" + DateTime.now(DateTimeZone.UTC).toString(ISODateTimeFormat.basicDateTime());
        return id;
    }

    public synchronized static UniqueId createFromString(String externalId)
    {
        UniqueId id = new UniqueId();
        id.id = externalId;
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    public UniqueId()
    {
    }


    private static long counter;

    String id;

} // class
