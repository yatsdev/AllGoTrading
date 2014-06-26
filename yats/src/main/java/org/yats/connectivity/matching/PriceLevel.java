package org.yats.connectivity.matching;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.OrderNew;
import org.yats.trading.Receipt;

import java.util.ArrayList;

public class PriceLevel {

    public boolean isEmpty() {
        for(Receipt makerReceipt : list) {
            if(makerReceipt.getResidualSize().isGreaterThan(Decimal.ZERO)) return false;
        }
        return true;
    }

    public void match(OrderNew takerOrder) {
        Receipt takerReceipt = takerOrder.createReceiptDefault();
        match(takerReceipt);
    }

    public void match(Receipt takerReceipt) {
        for(Receipt makerReceipt : list) {
            if(!takerReceipt.isSamePriceOrInfront(makerReceipt)) throw new CommonExceptions.ContainerEmptyException("taker infront of maker receipt");;
            makerReceipt.match(takerReceipt);
            receiptConsumer.onReceipt(takerReceipt.createCopy());
            receiptConsumer.onReceipt(makerReceipt.createCopy());
            if(takerReceipt.isEndState()) return;
        }
    }

    public void add(OrderNew order) {
        add(order.createReceiptDefault());
    }

    public void add(Receipt receipt) {
        list.add(receipt);
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
