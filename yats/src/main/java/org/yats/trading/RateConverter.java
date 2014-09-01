package org.yats.trading;

import org.yats.common.Decimal;
import org.yats.common.UniqueId;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumePriceData, IConvertRate {

    public Position calculateProfit(Position oldPosition, Position newPosition, String targetPid) {
        Position oldPositionInTarget = convert(oldPosition, targetPid);
        Position newPositionInTarget = convert(newPosition, targetPid);
        return newPositionInTarget.subtract(oldPositionInTarget);
    }

    public Position convert(Position position, String targetProductId)
    {
        String key = createKey(position.getProductId(), targetProductId);
        RatesChain bestRatesChain = cache.containsKey(key)
                ? getFromCache(key)
                : findBestNode(position.getProductId(), targetProductId);
        if(bestRatesChain.isEmpty()) {
            throw new TradingExceptions.RateConverterException("empty chain found");
        }
        cache.put(key, bestRatesChain);
        return convert(position, targetProductId, bestRatesChain);
    }

    private RatesChain getFromCache(String key) {
        cacheHits++;
        return cache.get(key);
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public int getCacheSize() {
        return cache.size();
    }

    public boolean isChainInCache(String startProductId, String targetProductId) {
        String key = createKey(startProductId, targetProductId);
        return cache.containsKey(key);
    }

    public Position convert(Position position, String targetProductId, RatesChain bestRatesChain) {

        Decimal positionInTargetProduct = getLastForProductId(position.getProductId());
        if (isPositionHasUnitId(position, targetProductId)) {
            positionInTargetProduct= getLastForProductId(position.getProductId());
            return new Position(targetProductId, positionInTargetProduct.multiply(position.getSize()));
        }

        Product startProduct = products.getProductWith(position.getProductId());
        String currentProductId;

        String firstUnitId = bestRatesChain.getFirstElementUnitId();
        Decimal lastPriceOfFirstElement = rates.get(bestRatesChain.getFirstElementProductId()).getLast();
        if(startProduct.hasUnitId(firstUnitId)){
            positionInTargetProduct = positionInTargetProduct.multiply(lastPriceOfFirstElement.invert());
            currentProductId = bestRatesChain.getFirstElementUnderlyingId();
        }else {
            positionInTargetProduct =  positionInTargetProduct.multiply(lastPriceOfFirstElement);
            currentProductId= firstUnitId;
        }

        for(int i=1;i< bestRatesChain.getChain().size();i++)
        {
            String pid = bestRatesChain.getChain().elementAt(i).getProductId();
            Decimal lastPriceForPid = rates.get(pid).getLast();
            if(bestRatesChain.hasElementUnitId(i, currentProductId))
            {
                if(bestRatesChain.hasElementUnderlyingId(i, targetProductId)){
                    positionInTargetProduct =  positionInTargetProduct.multiply(lastPriceForPid.invert());
                    break;
                }
                currentProductId= bestRatesChain.getUnderlyingIdForIndex(i);
                positionInTargetProduct =  positionInTargetProduct.multiply(lastPriceForPid.invert());
            }else
            {
                if(bestRatesChain.hasElementUnitId(i, targetProductId))
                {
                    positionInTargetProduct =  positionInTargetProduct.multiply(lastPriceForPid);
                    break;
                }
                currentProductId= bestRatesChain.getUnitIdForIndex(i);
                positionInTargetProduct =  positionInTargetProduct.multiply(lastPriceForPid);
            }
        }

        return new Position(targetProductId, positionInTargetProduct.multiply(position.getSize()));

    }

    @Override
    public void onPriceData(PriceData priceData) {
        rates.put(priceData.getProductId(), priceData);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    private RatesChain findBestNode(String startProductId, String targetProductId)
    {
        Vector<Product> onlinePairs = new Vector<Product>();

        Vector<Product> originalProducts = new Vector<Product>();
        ConcurrentHashMap<Product, Vector<Product>> nextPair = new ConcurrentHashMap<Product, Vector<Product>>();


//In this segment I'll collect the pairs available in rates
        for(Product product : products.values())
        {
            if (rates.containsKey(product.getProductId())) {
                onlinePairs.add(product);
            }
        }

//In this segment I collect the pairs that are good as the first link of my chain
        Product startProduct = products.getProductWith(startProductId);
        for (int z = 0; z < onlinePairs.size(); z++) {
            if (onlinePairs.elementAt(z).getUnitId().compareTo(startProduct.getUnitId()) == 0 || onlinePairs.elementAt(z).getUnderlyingId().compareTo(startProduct.getUnitId()) == 0) {
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
        Vector<Vector<RatesChain>> chainsAtLevel = new Vector<Vector<RatesChain>>();
        Vector<RatesChain>nodesAtLevel0= new Vector<RatesChain>();
        for(int i=0;i<originalProducts.size();i++){
            RatesChain ratesChain =new RatesChain();
            ratesChain.add(originalProducts.elementAt(i));
            nodesAtLevel0.add(ratesChain);
        }
        chainsAtLevel.add(nodesAtLevel0);

//Here I build the three of all possible nodes (which contain their relative chains of conversions). The first node which satisfies the condition "currentNode.getNode().getUnitId().compareTo(targetProductId) == 0 || currentNode.getNode().getUnderlyingId().compareTo(targetProductId) == 0" is certainly, by construction, the shortest path of conversion.
        RatesChain bestRatesChain =new RatesChain();
        loops:
        for(int l=0;l<chainsAtLevel.size();l++) {
            Vector<RatesChain> newLevelofTheTree=new Vector<RatesChain>();
            for (int i = 0; i < chainsAtLevel.lastElement().size(); i++) {
                RatesChain currentRatesChain = chainsAtLevel.lastElement().elementAt(i);
                if (currentRatesChain.getNode().getUnitId().compareTo(targetProductId) == 0 || currentRatesChain.getNode().getUnderlyingId().compareTo(targetProductId) == 0) {
                    bestRatesChain = currentRatesChain;
                    break loops;

                } else {

                    Vector<Product> possibleSons = nextPair.get(currentRatesChain.getNode());

                    for (int j = 0; j < possibleSons.size(); j++) {

                        if (!currentRatesChain.getChain().contains(possibleSons.elementAt(j))) {
                            RatesChain newRatesChain = new RatesChain();
                            newRatesChain.setChain(new Vector<Product>(currentRatesChain.getChain()));
                            newRatesChain.add(possibleSons.elementAt(j));
                            newLevelofTheTree.add(newRatesChain);
                        }

                    }

                }

            }
            if(!newLevelofTheTree.isEmpty()){
                chainsAtLevel.add(newLevelofTheTree);
            }
        }
        return bestRatesChain;
    } // findBestNode

//    public Decimal returnPositionInTargetCurrency(String targetProductId, Position position, String fromCurrency, String toCurrency, Decimal priceInOriginalCurrency, Decimal positionInTargetCurrency, String OANDAFXPAIR) {
//
//        if (targetProductId.compareTo(toCurrency) == 0 && (products.getProductWith(position.getProductId()).getUnitId().compareTo(fromCurrency) == 0)) {
//            positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get(OANDAFXPAIR).getLast());
//        }
//
//        return positionInTargetCurrency;
//    }

    public RateConverter(IProvideProduct p) {
        products = p;
        rates = new PriceDataMap();
        cache = new ConcurrentHashMap<String, RatesChain>();
        cacheHits=0;
    }

    private boolean isPositionHasUnitId(Position position, String unitId) {
        return products.getProductWith(position.getProductId()).hasUnitId(unitId);
    }

    private Decimal getLastForProductId(String pid) {

        if (!products.containsProductWith(pid))
            throw new TradingExceptions.ItemNotFoundException("Can not find product for pid=" + pid);
        Product p = products.getProductWith(pid);
        if(p.isNoRateProduct())
            return Decimal.ONE;
        if (!rates.containsKey(pid))
            throw new TradingExceptions.ItemNotFoundException("Can not find rate for pid=" + pid);
        return rates.getLastPrice(pid);
    }

    private String createKey(String startProductId, String targetProductId) {
        return startProductId+"->"+targetProductId;
    }

    private PriceDataMap rates;
    private IProvideProduct products;
    private ConcurrentHashMap<String, RatesChain> cache;
    private int cacheHits;

}




