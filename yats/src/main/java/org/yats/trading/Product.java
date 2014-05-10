package org.yats.trading;


// todo: introduce class ProductStaticData that describes details of product. get data from db on subscription

public class Product {

//    public static ProductNULL NULL = new ProductNULL();

    @Override
    public String toString() {
        return "Product{" +
                "product=" + productId +
                ",symbol=" + symbol +
                ",exchange=" + exchange +
                '}';
    }

//    public boolean isSameAs(Product other) {
//        return other.hasProductId(productId);
//    }

    public boolean hasProductId(String pid) {
        return pid.compareTo(productId) == 0;
    }

    public String getProductId() {
        return productId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public Product(String productId, String symbol, String exchange) {
        this.productId = productId;
        this.symbol = symbol;
        this.exchange = exchange;
    }


    private String productId;
    private String symbol;
    private String exchange;


//    private static class ProductNULL extends Product {
//        public String getProductId() { throw new RuntimeException("This is null object!");}
//        public String getSymbol() {
//            throw new RuntimeException("This is null object!");
//        }
//        public String getExchange() {
//            throw new RuntimeException("This is null object!");
//        }
//        private ProductNULL() { super("NULL","NULL","NULL");}
//    }

} // class
