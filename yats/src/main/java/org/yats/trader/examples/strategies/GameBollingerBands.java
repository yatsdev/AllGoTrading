package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.BollingerBandsForProduct;
import org.yats.trading.OrderNew;
import org.yats.trading.PriceData;
import org.yats.trading.Receipt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by abbanerjee on 05/11/14.
 */
public class GameBollingerBands extends StrategyBase {

    final Logger log = LoggerFactory.getLogger(GameBollingerBands.class);
    final int BOLLINGER_WINDOW_SIZE = 20;



    @Override
    public void onPriceDataForStrategy(PriceData priceData) {
        if (shuttingDown) return;
        if (!isInitialised()) return;

        hasPriceData = false;
        for(String tradeProductId : pidList) {
            if (priceData.hasProductId(tradeProductId)) {
                hasPriceData = true;
            }
        }
        if(!hasPriceData) return;

        bollingerBandMap.get(priceData.getProductId()).addPrice(priceData);
        counter++;

    }

    public void onReceiptForStrategy(Receipt receipt) {
        if (shuttingDown) return;
        if (receipt.getRejectReason().length() > 0) {
            log.error("Received rejection! Stopping for now!");
            //System.exit(-1);
        }

        boolean foundProductForReceipt = false;
        String productId = "";

        for (String tradeProductId : pidList) {
            if (receipt.hasProductId(tradeProductId)) {
                foundProductForReceipt = true;
                productId = tradeProductId;
            }
        }

        if (!foundProductForReceipt) {
            log.error("Received receipt for unknown product: " + receipt);
            return;
        }

        BollingerBandsForProduct bandsForProduct =  bollingerBandMap.get(productId);
        bandsForProduct.onReceipt(receipt);
    }


    @Override
    public void onStopStrategy() {
    }

    @Override
    public void onStartStrategy() {

    }

    @Override
    public void onSettingsForStrategy(IProvideProperties p) {
    }

    @Override
    public void onInitStrategy() {
        setInternalAccount(this.getClass().getSimpleName());
        tradeProductList = getConfig("productIdList");
        String[] parts = tradeProductList.split(",");
        pidList = Arrays.asList(parts);
        bollingerBandMap = new ConcurrentHashMap<String, BollingerBandsForProduct>();
        for(String tradeProductId : pidList) {
            BollingerBandsForProduct bollingerBandForProduct = new BollingerBandsForProduct(tradeProductId,BOLLINGER_WINDOW_SIZE);
            bollingerBandForProduct.setOrderSenderSuper(this);
            bollingerBandMap.put(tradeProductId,bollingerBandForProduct);
            subscribe(tradeProductId);
        }
        startStrategy();
    }

    @Override
    public void onShutdown() {
        shuttingDown = true;
    }

    public GameBollingerBands() {
        super();
        shuttingDown = false;

    }

    private boolean shuttingDown;
    private String tradeProductList ;
    private int counter;
    private ConcurrentHashMap<String,BollingerBandsForProduct> bollingerBandMap;
    private List<String> pidList;
    private boolean hasPriceData;
}
