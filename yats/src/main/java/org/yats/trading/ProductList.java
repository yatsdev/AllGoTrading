package org.yats.trading;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.util.HashMap;

public class ProductList implements IProvideProduct {

    @Override
    public Product getProductForProductId(String productId) {
        if(!list.containsKey(productId)) Exceptions.throwItemNotFoundException("productId not found: "+productId);
        return list.get(productId);

    }

    public Product findBySymbol(String symbol) {
        for(Product p:list.values()) {
            if(p.getSymbol().compareTo(symbol)==0) {
                return p;
            }
        }
        throw new RuntimeException("Symbol not found: " + symbol);
    }

    public static ProductList createFromFile(String path) {
        ProductList p = new ProductList();
        p.read(path);
        return p;
    }

    public void read(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                Product p = new Product(nextLine[0].trim(), nextLine[1].trim(), nextLine[2].trim());
                add(p);
            }
        } catch (Exception e) {
            Exceptions.throwFileReadException(e.getMessage());
        }
    }


    public void add(Product p) {
        list.put(p.getProductId(), p);
    }

    public int size() {
        return list.size();
    }

    public ProductList() {
        list = new HashMap<String, Product>();
    }


    HashMap<String, Product> list;

} // class
