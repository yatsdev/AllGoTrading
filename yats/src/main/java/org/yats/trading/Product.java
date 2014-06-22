package org.yats.trading;

import org.yats.common.UniqueId;

public class Product {

//    public static ProductNULL NULL = new ProductNULL();

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", exchange='" + exchange + '\'' +
                ", bloombergId='" + bloombergId + '\'' +
                ", name='" + name + '\'' +
                ", route='" + route + '\'' +
                ", underlyingId='" + underlyingId + '\'' +
                ", unitId='" + unitId + '\'' +
                '}';
    }

//    public boolean isSameAs(Product other) {
//        return other.hasProductId(productId);
//    }

    public boolean hasProductId(String pid) {
        return productId.compareTo(pid) == 0;
    }

    public boolean hasUnitId(String pid) {
        return unitId.compareTo(pid) == 0;
    }

    public boolean hasUnderlying(String pid) {
        return underlyingId.compareTo(pid) == 0;
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

    public String getBloombergId() {
        return bloombergId;
    }

    public String getName() {
        return name;
    }

    public String getRoute() {
        return route;
    }

    public String getUnderlyingId() {
        return underlyingId;
    }

    public String getUnitId() {
        return unitId;
    }

    public String toStringCSV() {
        return ""+productId+","+symbol+","+exchange+","+bloombergId+","+name+","+route+","+ underlyingId +","+unitId;
    }

    public Product withProductId(String p) {
        productId = p;
        return this;
    }

    public Product withSymbol(String s) {
        symbol=s;
        return this;
    }

    public Product withExchange(String e) {
        exchange=e;
        return this;
    }

    public Product withBloombergId(String s) {
        bloombergId = s;
        return this;
    }

    public Product withName(String s) {
        name = s;
        return this;
    }

    public Product withRoute(String s) {
        route = s;
        return this;
    }

    public Product withUnderlyingId(String s) {
        underlyingId = s;
        return this;
    }

    public Product withUnitId(String s) {
        unitId = s;
        return this;
    }

    public boolean isProductAsPairChainable(Product product){
        boolean isChainable=false;

        if(product.getUnderlyingId().compareTo(underlyingId)==0||product.getUnitId().compareTo(unitId)==0||product.getUnderlyingId().compareTo(unitId)==0||product.getUnitId().compareTo(underlyingId)==0){
            if(!(product.getProductId().compareTo(productId)==0)){

                isChainable=true;
            }

        }
        return isChainable;
    }

    public Product(String productId, String symbol, String exchange) {
        this.productId = productId;
        this.symbol = symbol;
        this.exchange = exchange;
    }

    public Product() {
        productId = new UniqueId().toString();
    }

    private String productId;
    private String symbol;
    private String exchange;
    private String bloombergId;
    private String name;
    private String route;
    private String underlyingId;
    private String unitId;

    public boolean isRoute(String r) {
        return (route.compareTo(r)==0);
    }

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
