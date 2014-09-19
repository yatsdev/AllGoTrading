package org.yats.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


// the properties file needs to contain key-value pairs in form "key=value"

public class PropertiesReader implements IProvideProperties {

    public PropertiesReader() {
//        properties = new Properties();
        properties = new ConcurrentHashMap<String, String>();
    }

    public static PropertiesReader createFromTwoProviders(IProvideProperties prop1, IProvideProperties prop2) {
        PropertiesReader p = new PropertiesReader();
        p.add(prop1);
        p.add(prop2);
        return p;
    }

    public static PropertiesReader createFromProvider(IProvideProperties prop) {
        PropertiesReader p = new PropertiesReader();
        p.add(prop);
        return p;
    }

    @Override
    public int size() {
        return properties.size();
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
        if(!FileTool.exists(pathToConfigFile)) return new PropertiesReader();
        try {
            String configAsString = new Scanner(new File(pathToConfigFile)).useDelimiter("\\Z").next();
            return createFromConfigString(configAsString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new CommonExceptions.FieldNotFoundException(e.getMessage());
        }
    }

    public static PropertiesReader createFromConfigString(String config)
    {
        String configOnlyNewlines = config.replace("\r\n","\n");
        PropertiesReader p = new PropertiesReader();
        List<String> list = toListFromLines(configOnlyNewlines);
        for(String line : list) {
            String lineNoBlanks = line.replace(" ","").replace("\t","");
            if(lineNoBlanks.startsWith("#include=")) {
                String[] keyValue = line.split("=");
                String valueTrimmed = keyValue[1].trim();
                p.add(createFromConfigFile(valueTrimmed));
                continue;
            }
            else if(line.startsWith("#")) {
                continue;
            }
            else if(!line.contains("="))
            {
                continue;
            }
            String[] keyValue = line.split("=");
            String keyTrimmedNoBlanks = keyValue[0].replace(" ","").replace("\t","");
            String valueTrimmed = keyValue[1].trim();
            p.properties.put(keyTrimmedNoBlanks, valueTrimmed);
        }
        return p;
    }

    public void add(IProvideProperties reader) {
        for(String key : reader.getKeySet()) {
            String value = reader.get(key);
            properties.put(key, value);
        }
    }

    private static List<String> toListFromLines(String lines) {
        String[] keyValueArray = lines.split("\\n");
        return Arrays.asList(keyValueArray);
    }

    public static PropertiesReader createFromStringKeyValue(String csv)
    {
        PropertiesReader p = new PropertiesReader();
        String[] parts = csv.split(",");
        if(parts.length<2) return p;
        for (String part : parts) {
            String[] keyvalue = part.split("=");
            p.properties.put(keyvalue[0], keyvalue[1]);
        }
        return p;
    }

    public ConcurrentHashMap<String,String> toMap() {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
        for(String key : properties.keySet()) {
            map.put(key, properties.get(key));
        }
        return map;
    }

    public static PropertiesReader createFromMap(ConcurrentHashMap<String, String> map) {
        PropertiesReader r = new PropertiesReader();
        for(String key : map.keySet()) {
            r.set(key, map.get(key));
        }
        return r;
    }

    @Override
    public Set<String> getKeySet() {
        return properties.keySet();
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
        return properties.get(_key);
    }

    @Override
    public String get(String _key, String _defaultValue)
    {
        if(!properties.containsKey(_key)) return _defaultValue;
        return properties.get(_key);
    }

    @Override
    public boolean getAsBoolean(String _key, boolean _defaultValue)
    {
        if(!properties.containsKey(_key)) return _defaultValue;
        return fromBooleanString(properties.get(_key));
    }

    @Override
    public boolean getAsBoolean(String _key)
    {
        if(!properties.containsKey(_key)) throw new CommonExceptions.FieldNotFoundException("No such key: " + _key);
        return fromBooleanString(properties.get(_key));
    }

    @Override
    public Decimal getAsDecimal(String _key) {
        if(!exists(_key)) throw new CommonExceptions.FieldNotFoundException("No such key: " + _key);
        return Decimal.fromString(get(_key));
    }

    @Override
    public String[] getCSVAsArray(String _key) {
        String all = get(_key);
        String[] parts = all.split(",");
        return parts;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        Enumeration enuKeys = properties.keys();
        boolean first = true;
        while (enuKeys.hasMoreElements()) {
            String key = (String) enuKeys.nextElement();
            String value = properties.get(key);
            if(!first){b=b.append(",");}
            first=false;
            b=b.append(key).append("=").append(value);
        }
            return "PropertiesReader: "+b.toString();
    }

    public String toStringKeyValue() {
        StringBuilder b = new StringBuilder();
        Enumeration enuKeys = properties.keys();
        boolean first = true;
        while (enuKeys.hasMoreElements()) {
            String key = (String) enuKeys.nextElement();
            String value = properties.get(key);
            if(!first){b.append(","); first=false;}
            b.append(key).append("=").append(value);
        }
        return b.toString();
    }

    public String toStringKeyValueFile() {
        StringBuilder b = new StringBuilder();
        Enumeration enuKeys = properties.keys();
        boolean first = true;
        while (enuKeys.hasMoreElements()) {
            String key = (String) enuKeys.nextElement();
            String value = properties.get(key);
            if(!first){b.append("\n"); first=false;}
            b.append(key).append("=").append(value);
        }
        b.append("\n");
        return b.toString();
    }

    @Override
    public void set(String key, boolean value) {
        properties.put(key, value ? "true" : "false");
    }

    @Override
    public void set(String key, Decimal value) {
        properties.put(key, value.toString());
    }

    @Override
    public void set(String key, String value) {
        properties.put(key, value);
    }


    public static String toString(IProvideProperties p) {
        StringBuilder b = new StringBuilder();
        for(String key : p.getKeySet()) b.append(key+"="+p.get(key)+";");
        return b.toString();
    }

    public static boolean fromBooleanString(String value) {
        String lowerCase = value.toLowerCase();
        if(lowerCase.compareTo("yes")==0) return true;
        if(lowerCase.compareTo("y")==0) return true;
        if(lowerCase.compareTo("1")==0) return true;
        return lowerCase.compareTo("true") == 0;
    }


    /////////////////////////////////////////////////////////////////////////////
    ConcurrentHashMap<String, String> properties;

//    Properties properties;
}