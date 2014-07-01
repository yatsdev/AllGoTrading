package org.yats.trading;

import au.com.bytecode.opencsv.CSVReader;
import org.yats.common.CommonExceptions;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ProductList implements IProvideProduct {

    @Override
    public boolean isProductIdExisting(String productId){
        return list.containsKey(productId);
    }

    @Override
    public Product getProductForProductId(String productId) {
        if(!isProductIdExisting(productId)) throw new TradingExceptions.ItemNotFoundException("productId not found: " + productId);
        return list.get(productId);
    }

    @Override
    public IProvideProduct getProductsWithUnit(String productId) {
        ProductList newList = new ProductList();
        for(Product p : this.list.values()) {
            if(p.hasUnitId(productId)) newList.add(p);
        }
        return newList;
    }

    @Override
    public IProvideProduct getProductsWithUnderlying(String productId) {
        ProductList newList = new ProductList();
        for(Product p : this.list.values()) {
            if(p.hasUnderlying(productId)) newList.add(p);
        }
        return newList;
    }

    @Override
    public Collection<Product> values()
    {
        return list.values();
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
                Product p = new Product()
                        .withProductId(checkForNull(nextLine[0].trim()))
                        .withSymbol(checkForNull(nextLine[1].trim()))
                        .withExchange(checkForNull(nextLine[2].trim()))
                        .withBloombergId(checkForNull(nextLine[3].trim()))
                        .withName(checkForNull(nextLine[4].trim()))
                        .withRoute(checkForNull(nextLine[5].trim()))
                        .withUnderlyingId(checkForNull(nextLine[6].trim()))
                        .withUnitId(checkForNull(nextLine[7].trim()))
                        ;
                add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonExceptions.FileReadException(e.getMessage());
        }
    }

    private String checkForNull(String text) {
        if(text==null) throw new TradingExceptions.FieldIsNullException("");
        return text;
    }

    public void writeWithAppend(String path, String appendToEachLine)
    {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            for(Product p : list.values()) {
                out.write(p.toStringCSV());
                out.write(appendToEachLine);
                out.newLine();
            }
            out.close();
        } catch (IOException e)
        {
            throw new CommonExceptions.FileWriteException(e.getMessage());
        }
    }

    public void add(Product p) {
        list.put(p.getProductId(), p);
    }

    public int size() {
        return list.size();
    }

    public ProductList() {
        list = new ConcurrentHashMap<String, Product>();
    }


    ConcurrentHashMap<String, Product> list;

} // class
