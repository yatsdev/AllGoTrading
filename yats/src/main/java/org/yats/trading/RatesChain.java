package org.yats.trading;

import java.util.Vector;

class RatesChain {

    public boolean hasElementUnitId(int index, String searchedUnitId) {
        return chain.elementAt(index).hasUnitId(searchedUnitId);
    }

    public boolean hasElementUnderlyingId(int index, String searchedUnderlyingId) {
        return chain.elementAt(index).hasUnitId(searchedUnderlyingId);
    }

    public String getUnderlyingIdForIndex(int index) {
        return chain.elementAt(index).getUnderlyingId();
    }

    public String getUnitIdForIndex(int index) {
        return chain.elementAt(index).getUnitId();
    }

    public String getFirstElementUnitId() {
        return chain.firstElement().getUnitId();
    }

    public String getFirstElementUnderlyingId() {
        return chain.firstElement().getUnderlyingId();
    }

    public String getFirstElementProductId() {
        return chain.firstElement().getProductId();
    }

    public Product getNode() {
        return chain.lastElement();
    }

    public void add(Product product){
        chain.add(product);
    }

    public Vector<Product> getChain() {
        return chain;
    }

    public void setChain(Vector<Product> chain) {
        this.chain = chain;
    }

    private Vector<Product> chain =new Vector<Product>();

}
