package org.yats.trading;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;

public class ProductList {

    public void read(String path) {

        try {
            CSVReader reader = new CSVReader(new FileReader(path));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                Product p = new Product(nextLine[0].trim(), nextLine[1].trim(), nextLine[2].trim());
                list.add(p);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public int size() {
        return list.size();
    }

    public ProductList() {
        list = new ArrayList<Product>();
    }

    ArrayList<Product> list;

} // class
