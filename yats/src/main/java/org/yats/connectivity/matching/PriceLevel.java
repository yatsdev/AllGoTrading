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
            if(makerReceipt.isEndState()) continue;
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
            Receipt takerIntermediateReceipt = takerReceipt.createCopy().withPrice(makerReceipt.getPrice());
            receiptConsumer.onReceipt(takerIntermediateReceipt);
            receiptConsumer.onReceipt(makerReceipt.createCopy());
            takerReceipt.setCurrentTradedSize(Decimal.ZERO);
            makerReceipt.setCurrentTradedSize(Decimal.ZERO);
            if(takerReceipt.isEndState()) return;
        }
    }

    public void remove(String orderId){
        Receipt receipt = findReceiptWithOrderId(orderId);
        if(receipt==null) return;
        receipt.setEndState(true);
        receipt.setCurrentTradedSize(Decimal.ZERO);
        list.remove(receipt);
        receiptConsumer.onReceipt(receipt.createCopy());
    }

    public Receipt findReceiptWithOrderId(String orderId){
        for(Receipt receipt : list) {
            if (receipt.hasOrderId(orderId)) return receipt;
        }
        return null;
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

    public Decimal getCumulativeSize() {
        Decimal sum = Decimal.ZERO;
        for(Receipt makerReceipt : list) {
            sum=sum.add(makerReceipt.getResidualSize());
        }
        return sum;
    }

    public Decimal getPrice() {
        return price;
    }

    public PriceLevel(Decimal _price, IConsumeReceipt _receiptConsumer) {
        price = _price;
        list = new ArrayList<Receipt>();
        receiptConsumer = _receiptConsumer;
    }

    Decimal price;
    ArrayList<Receipt> list;
    IConsumeReceipt receiptConsumer;

} // class
