package org.yats.connectivity.matching;

import org.yats.common.Decimal;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.OrderNew;
import org.yats.trading.Receipt;

import java.util.ArrayList;

public class PriceLevel {


    public void match(OrderNew taker) {
        for(Receipt maker : list) {
            Receipt takerReceipt = taker.createReceiptDefault();
            Decimal takerResidual = Decimal.max(Decimal.ZERO, takerReceipt.getResidualSize().subtract(maker.getResidualSize()));
            Decimal makerResidual = Decimal.max(Decimal.ZERO, maker.getResidualSize().subtract(taker.getSize()));

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
