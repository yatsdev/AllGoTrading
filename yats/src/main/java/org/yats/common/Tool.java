package org.yats.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tool {

    final static Logger log = LoggerFactory.getLogger(Tool.class);


    public static String getOsName()
    {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }

    public static boolean isWindows()
    {
        return getOsName().startsWith("Windows");
    }

    public static String getPersonalConfigFilename(String prefix)
    {
        String username = System.getProperty("user.name").replace(" ","");
        String userSpecificFIXFilename = prefix+"_"+username+".properties";
        log.info("Trying to read config file: "+userSpecificFIXFilename);
        if(!FileTool.exists(userSpecificFIXFilename))
            throw new CommonExceptions.FileReadException(userSpecificFIXFilename+" not found!");
        return userSpecificFIXFilename;
    }


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



    ///////////////////////////

    private static String OS = null;

}
