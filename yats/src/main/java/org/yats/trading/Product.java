package org.yats.trading;


// todo: introduce class ProductStaticData that describes details of product. get data from db on subscription

public class Product {

    public static ProductNULL NULL = new ProductNULL();

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ",symbol=" + symbol +
                ",exchange=" + exchange +
                '}';
    }

    public boolean isSameAs(Product other) {
        boolean same = id.compareTo(other.id) == 0;
        return same;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public Product(String id, String symbol, String exchange) {
        this.id = id;
        this.symbol = symbol;
        this.exchange = exchange;
    }


    private String id;
    private String symbol;
    private String exchange;

     private static class ProductNULL extends Product {
        public String getId() { throw new RuntimeException("This is null object!");}
        public String getSymbol() {
            throw new RuntimeException("This is null object!");
        }
        public String getExchange() {
            throw new RuntimeException("This is null object!");
        }
        private ProductNULL() { super("NULL","NULL","NULL");}
    }

} // class
