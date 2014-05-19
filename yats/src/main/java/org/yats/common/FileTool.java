package org.yats.common;

import java.io.*;

public class FileTool {

    public static void writeToTextFile(String filename, String stringToWrite, boolean append) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(filename, append));
            out.write(stringToWrite);
            out.close();
        } catch (IOException e) {
            CommonExceptions.throwFileWriteException(e.getMessage());
        }
    }

    public static String readFromTextFile(String filePath) {
        StringBuffer fileData = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            return fileData.toString();
        } catch (IOException e) {
            CommonExceptions.throwFileReadException(e.getMessage());
        }
        return fileData.toString();
    }

    public static void deleteFile(String filename) {
        File f = new File(filename);
        f.delete();
    }

}
