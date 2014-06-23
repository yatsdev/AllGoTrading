package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


    ConcurrentHashMap<String, MarketData> rates;
    IProvideProduct products;

    public RateConverter(IProvideProduct p) {
        products = p;
        rates = new ConcurrentHashMap<String, MarketData>();
    }

    public Position convert(Position position, String targetProductId) {


        Vector<Product> currencies = new Vector<Product>();
        Vector<Product> pairs = new Vector<Product>();
        Vector<Product> pairsBackup = new Vector<Product>();
        Vector<Product> originalProducts= new Vector<Product>();
        Vector<Product> targetProducts = new Vector<Product>();

        Vector< Vector<Product>> chainLinks=new Vector< Vector<Product>>();

        Collection collection;
        collection = products.values();


        Product currentProduct;

//In this segment I'll collect in my currencies Vector all the currencies available in the .csv file
        Iterator itr = collection.iterator();
        while (itr.hasNext()) {
            Product product = (Product) itr.next();
            if (product.getUnitId().compareTo(product.getUnderlyingId()) == 0) {
                currencies.add(product);
            }
        }

//In this segment I'll collect in my chainLinks Matrix all the pairs available in the .csv file and I'll create my pairs Vector,too
        Iterator itr2 = collection.iterator();
        while (itr2.hasNext()) {
            Product product = (Product) itr2.next();
            for (int i = 0; i < currencies.size(); i++) {
                if (product.getUnderlyingId().compareTo(currencies.elementAt(i).getProductId()) == 0 && !(product.getUnitId().compareTo(product.getUnderlyingId()) == 0))//This is a pair
                {
                    pairs.add(product);
                    pairsBackup.add(product);
                   // Vector<Product> ChainStart=new Vector<Product>();
                   // ChainStart.add(product);
                   // chainLinks.add(ChainStart);
                }
            }
        }

   //In this segment I collect the pairs that are good as the last link of my chain
        for (int z=0;z<pairs.size();z++)
        {
        if (pairs.elementAt(z).getUnitId().compareTo(targetProductId) == 0 ||pairs.elementAt(z).getUnderlyingId().compareTo(targetProductId) == 0) {
            targetProducts.add(pairs.elementAt(z));
        }
    }

//In this segment I collect the pairs that are good as the first link of my chain
        for (int z=0;z<pairs.size();z++)
        {
            if (pairs.elementAt(z).getUnitId().compareTo(products.getProductForProductId(position.getProductId()).getUnitId()) == 0 ||pairs.elementAt(z).getUnderlyingId().compareTo(products.getProductForProductId(position.getProductId()).getUnitId()) == 0) {
                originalProducts.add(pairs.elementAt(z));
            }
        }


//Initializing the chainLinks Matrix with the possible starting products
for (int l=0;l<originalProducts.size();l++)
{
    Vector<Product> newstartingChain=new Vector<Product>();
    newstartingChain.add(originalProducts.elementAt(l));
    chainLinks.add(newstartingChain);

}








        throw new NotImplementedException();

    }

    @Override
    public void onMarketData(MarketData marketData) {
        rates.put(marketData.getProductId(), marketData);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

     private MarketData getMarketDataForProduct(String pid) {
        if(!rates.containsKey(pid)) throw new TradingExceptions.ItemNotFoundException("Can not find rate for pid="+pid);
        return rates.get(pid);
    }


} // class

class Chain {

private Vector<Product> Chain= new Vector<Product>();
private Product startingProduct;
private Product targetProduct;
private Product lastElement;
private boolean isCompleted=false;
private int length;



    public void addProduct(Product product){
       if(isCompleted==false) {
           if (!(Chain.contains(product))) {
               if (Chain.lastElement().isProductAsPairChainable(product)) {
                   Chain.add(product);
                   if (Chain.lastElement().isProductAsPairChainable(targetProduct)) {
                       isCompleted = true;
                   }
               }
           }

       }
          }

    public Product getStartingProduct() {
        return Chain.elementAt(0);
    }

    public void setStartingProduct(Product startingProduct) {
         Chain.add(startingProduct);
        this.startingProduct=startingProduct;
    }

    public Product getTargetProduct() {
        return targetProduct;
    }

    public void setTargetProduct(Product targetProduct) {
        this.targetProduct = targetProduct;
    }

    public Product getLastElement() {
        return Chain.elementAt(Chain.size());
    }

    public int getLength() {
        return Chain.size();
    }


}