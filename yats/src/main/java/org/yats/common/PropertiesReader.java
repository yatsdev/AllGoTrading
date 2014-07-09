package org.yats.common;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;


// the properties file needs to contain key-value pairs in form "key=value"

public class PropertiesReader implements IProvideProperties {

    public PropertiesReader() {
        properties = new Properties();
    }


//    public void read(String filename) {
//        try {
//            File file = new File(filename);
//            FileInputStream fileInput = new FileInputStream(file);
//            properties = new Properties();
//            properties.load(fileInput);
//            fileInput.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e.getMessage());
//        }
//    }

    public static PropertiesReader create()
    {
        String configStringDefault = "externalAccount=1\n";
        return createFromConfigString(configStringDefault);
    }

    public static PropertiesReader createFromConfigFile(String pathToConfigFile)
    {
        try {
            String configAsString = new Scanner(new File(pathToConfigFile)).useDelimiter("\\Z").next();
            return createFromConfigString(configAsString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static PropertiesReader createFromConfigString(String config)
    {
        try {
            InputStream inputStream = new ByteArrayInputStream(config.getBytes());
            PropertiesReader p = new PropertiesReader();
            p.properties.load(inputStream);
            inputStream.close();
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean exists(String _key)
    {
        return properties.containsKey(_key);
    }

    @Override
    public String get(String _key)
    {
        if(!exists(_key)) throw new CommonExceptions.KeyNotFoundException("Config file does not contain key: " + _key);
        return properties.getProperty(_key);
    }

    @Override
    public String get(String _key, String _defaultValue)
    {
        if(!properties.containsKey(_key)) return _defaultValue;
        return properties.getProperty(_key);
    }

    @Override
    public boolean getAsBoolean(String _key, boolean _defaultValue)
    {
        if(!properties.containsKey(_key)) return _defaultValue;
        return fromBooleanString(properties.getProperty(_key));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        Enumeration enuKeys = properties.keys();
        boolean first = true;
        while (enuKeys.hasMoreElements()) {
            String key = (String) enuKeys.nextElement();
            String value = properties.getProperty(key);
            if(!first){b.append(","); first=false;}
            b.append(key).append("=").append(value);
        }
            return "PropertiesReader: "+b.toString();
    }

    Properties properties;

    public static boolean fromBooleanString(String listening) {
        String lowerCase = listening.toLowerCase();
        if(lowerCase.compareTo("yes")==0) return true;
        if(lowerCase.compareTo("no")==0) return true;
        if(lowerCase.compareTo("y")==0) return true;
        if(lowerCase.compareTo("1")==0) return true;
        if(lowerCase.compareTo("true")==0) return true;
        return false;
    }
}