package org.yats.trading;

import org.yats.common.Decimal;
import org.yats.common.UniqueId;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {




    public RateConverter(IProvideProduct p) {
        products = p;
        rates = new MarketDataMap();
    }


    public Position convert(Position position, String targetProductId) {
        Decimal positionInTargetCurrency = null;
        Decimal priceInOriginalCurrency = rates.getLastPrice(position.getProductId());
        Decimal positionSizedInTargetCurrency = null;


        if (isSameCurrency(position, targetProductId)) {
            positionInTargetCurrency = getLastForProductId(position.getProductId());
        } else {

            Vector<Product> currencies = new Vector<Product>();
            Vector<Product> onlinePairs = new Vector<Product>();
            Collection collection = null;
            collection = products.values();


            Vector<Product> originalProducts = new Vector<Product>();
            ConcurrentHashMap nextPair = new ConcurrentHashMap();


            //In this segment I'll collect in my currencies Vector all the currencies available in the .csv file
            Iterator itr = collection.iterator();
            while (itr.hasNext()) {
                Product product = (Product) itr.next();
                if (product.getUnitId().compareTo(product.getUnderlyingId()) == 0) {
                    currencies.add(product);
                }
            }




//In this segment I'll collect the pairs available in rates
            Iterator itr2 = collection.iterator();
            while (itr2.hasNext()) {
                Product product = (Product) itr2.next();
                for (int i = 0; i < currencies.size(); i++) {
                    if (product.getUnderlyingId().compareTo(currencies.elementAt(i).getProductId()) == 0 && !(product.getUnitId().compareTo(product.getUnderlyingId()) == 0))//This is a pair
                    {
                        if (rates.containsKey(product.getProductId())) {
                            onlinePairs.add(product);
                        }
                    }
                }
            }

//In this segment I collect the pairs that are good as the first link of my chain
            for (int z = 0; z < onlinePairs.size(); z++) {
                if (onlinePairs.elementAt(z).getUnitId().compareTo(products.getProductForProductId(position.getProductId()).getUnitId()) == 0 || onlinePairs.elementAt(z).getUnderlyingId().compareTo(products.getProductForProductId(position.getProductId()).getUnitId()) == 0) {
                    originalProducts.add(onlinePairs.elementAt(z));
                }
            }


//In this segment I'll build all the possible next steps from a given pair to another

            for (int i = 0; i < onlinePairs.size(); i++) {
                Vector<Product> nextPairs = new Vector<Product>();
                for (int k = 0; k < onlinePairs.size(); k++) {

                    if (onlinePairs.elementAt(i).isProductAsPairChainable(onlinePairs.elementAt(k))) {
                        nextPairs.add(onlinePairs.elementAt(k));
                    }
                }
                nextPair.put(onlinePairs.elementAt(i), nextPairs);
            }


//Initilizing the tree at level 0 with possible starting pairs
            Vector<Vector<Node>> nodesAtLevel = new Vector<Vector<Node>>();
            Vector<Node>nodesAtLevel0= new Vector<Node>();
            for(int i=0;i<originalProducts.size();i++){
                Node node=new Node();
                node.add(originalProducts.elementAt(i));
                nodesAtLevel0.add(node);
            }
            nodesAtLevel.add(nodesAtLevel0);

//Here I build the three of all possible nodes (which contain their relative chains of conversions). The first node which satisfies the condition "currentNode.getNode().getUnitId().compareTo(targetProductId) == 0 || currentNode.getNode().getUnderlyingId().compareTo(targetProductId) == 0" is certainly, by construction, the shortest path of conversion.
            Node bestNode=new Node();
            loops:
            for(int l=0;l<nodesAtLevel.size();l++) {
                Vector<Node> newLevelofTheTree=new Vector<Node>();
                for (int i = 0; i < nodesAtLevel.lastElement().size(); i++) {
                    Node currentNode = nodesAtLevel.lastElement().elementAt(i);
                    if (currentNode.getNode().getUnitId().compareTo(targetProductId) == 0 || currentNode.getNode().getUnderlyingId().compareTo(targetProductId) == 0) {
                        bestNode=currentNode;
                        break loops;

                    } else {

                        Vector<Product> possibleSons = (Vector<Product>) nextPair.get(currentNode.getNode());

                        for (int j = 0; j < possibleSons.size(); j++) {

                            if (!currentNode.getChainSoFar().contains(possibleSons.elementAt(j))) {
                                Node newNode = new Node();
                                newNode.setChainSoFar(new Vector<Product>(currentNode.getChainSoFar()));
                                newNode.add(possibleSons.elementAt(j));
                                newLevelofTheTree.add(newNode);
                            }

                        }

                    }

                }
                if(!newLevelofTheTree.isEmpty()){
                    nodesAtLevel.add(newLevelofTheTree);
                }
            }

///////////////////////////Here I do the actual conversion using the chain just found, I have to use ifs to evaluate whether to invert pairs or not.

            positionInTargetCurrency=priceInOriginalCurrency;
            if(products.getProductForProductId(position.getProductId()).getUnitId().compareTo(bestNode.getChainSoFar().firstElement().getUnitId())==0){


                positionInTargetCurrency =  positionInTargetCurrency.multiply(rates.get(bestNode.getChainSoFar().firstElement().getProductId()).getLast().invert());
            }else {


                positionInTargetCurrency =  positionInTargetCurrency.multiply(rates.get(bestNode.getChainSoFar().firstElement().getProductId()).getLast());


            }
            for(int i=1;i<bestNode.getChainSoFar().size();i++){


                if(bestNode.getChainSoFar().elementAt(i-1).getUnitId().compareTo(bestNode.getChainSoFar().elementAt(i).getUnitId())==0){


                    if(bestNode.getChainSoFar().elementAt(i).getUnderlyingId().compareTo(targetProductId)==0){
                        positionInTargetCurrency =  positionInTargetCurrency.multiply(rates.get(bestNode.getChainSoFar().elementAt(i).getProductId()).getLast().invert());
                        break;
                    }

                      positionInTargetCurrency =  positionInTargetCurrency.multiply(rates.get(bestNode.getChainSoFar().elementAt(i).getProductId()).getLast().invert());
                }else {

                    if(bestNode.getChainSoFar().elementAt(i).getUnitId().compareTo(targetProductId)==0){

                        positionInTargetCurrency =  positionInTargetCurrency.multiply(rates.get(bestNode.getChainSoFar().elementAt(i).getProductId()).getLast());
                        break;
                    }

                    positionInTargetCurrency =  positionInTargetCurrency.multiply(rates.get(bestNode.getChainSoFar().elementAt(i).getProductId()).getLast());


                }


            }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

        }

        positionSizedInTargetCurrency = positionInTargetCurrency.multiply(position.getSize());
        return new Position(targetProductId, positionSizedInTargetCurrency);

    }

    @Override
    public void onMarketData(MarketData marketData) {
        rates.put(marketData.getProductId(), marketData);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    private Decimal getLastForProductId(String pid) {

        if (!products.isProductIdExisting(pid))
            throw new TradingExceptions.ItemNotFoundException("Can not find product for pid=" + pid);
        Product p = products.getProductForProductId(pid);
        if(p.isNoRateProduct())
            return Decimal.ONE;
        if (!rates.containsKey(pid))
            throw new TradingExceptions.ItemNotFoundException("Can not find rate for pid=" + pid);
        return rates.getLastPrice(pid);
    }

    public boolean isSameCurrency(Position position, String targetProductId) {
        if (products.getProductForProductId(position.getProductId()).getUnitId().toString().compareTo(targetProductId) == 0)
            return true;
        else return false;
    }


    public Decimal returnPositionInTargetCurrency(String targetProductId, Position position, String fromCurrency, String toCurrency, Decimal priceInOriginalCurrency, Decimal positionInTargetCurrency, String OANDAFXPAIR) {

        if (targetProductId.compareTo(toCurrency) == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo(fromCurrency) == 0)) {
            positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get(OANDAFXPAIR).getLast());
        }

        return positionInTargetCurrency;
    }

    private MarketDataMap rates;
    private IProvideProduct products;


}

class Node {

    private Vector<Product> chainSoFar=new Vector<Product>();

    public Product getNode() {
        return chainSoFar.lastElement();
    }

    public void add(Product product){
        chainSoFar.add(product);
    }

    public Vector<Product> getChainSoFar() {
        return chainSoFar;
    }

    public void setChainSoFar(Vector<Product> chainSoFar) {
        this.chainSoFar = chainSoFar;
    }

}



