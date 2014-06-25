package org.yats.connectivity.matching;

import org.yats.trading.IConsumeReceipt;
import org.yats.trading.OrderNew;
import org.yats.trading.Receipt;

import java.util.ArrayList;

public class PriceLevel {

    public void match(OrderNew takerOrder) {
        Receipt takerReceipt = takerOrder.createReceiptDefault();
        match(takerReceipt);
    }

    public void match(Receipt takerReceipt) {
        for(Receipt makerReceipt : list) {
            if(!makerReceipt.isSamePriceOrBehind(takerReceipt)) return;
            makerReceipt.match(takerReceipt);
            receiptConsumer.onReceipt(takerReceipt.createCopy());
            receiptConsumer.onReceipt(makerReceipt.createCopy());
            if(takerReceipt.isEndState()) return;
        }
    }

    public void add(OrderNew order) {
        list.add(order.createReceiptDefault());
    }

    public int size() {
        return list.size();
    }

    public PriceLevel(IConsumeReceipt _receiptConsumer) {
        list = new ArrayList<Receipt>();
        receiptConsumer = _receiptConsumer;
    }

    ArrayList<Receipt> list;
    IConsumeReceipt receiptConsumer;

} // class
