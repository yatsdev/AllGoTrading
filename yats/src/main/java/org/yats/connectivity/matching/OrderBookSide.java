package org.yats.connectivity.matching;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.trading.BookSide;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.OrderNew;
import org.yats.trading.Receipt;

import java.util.SortedMap;
import java.util.TreeMap;

public class OrderBookSide implements IConsumeReceipt {

    public void match(OrderNew takerOrder) {
        Receipt takerReceipt = takerOrder.createReceiptDefault();
        match(takerReceipt);
    }

    public void match(Receipt takerReceipt) {
        takerReceiptSent=false;
        try {
            doMatch(takerReceipt);
        } catch(CommonExceptions.ContainerEmptyException e) {
            if(!takerReceiptSent) onReceipt(takerReceipt);
        }
    }

    public void doMatch(Receipt takerReceipt) {
        while(!takerReceipt.isEndState()) {
            Decimal frontRowPrice = getFrontRowPrice();
            if(!takerReceipt.isExecutingWith(frontRowPrice)) throw new CommonExceptions.ContainerEmptyException("cant execute");
            PriceLevel frontRow = book.get(frontRowPrice);
            frontRow.match(takerReceipt);
            if(frontRow.isEmpty()) book.remove(frontRowPrice);
        }
    }

    @Override
    public void onReceipt(Receipt receipt) {
        takerReceiptSent=true;
        if(receipt.isOpposite(side)) lastTakerReceipt = receipt;
        receiptConsumer.onReceipt(receipt);
    }

    public void add(OrderNew order) {
        Decimal limit = order.getLimit();
        PriceLevel row = book.containsKey(limit) ? book.get(limit) : new PriceLevel(this);
        row.add(order);
        book.put(limit, row);
    }

    public void add(Receipt receiptNew) {
        Decimal limit = receiptNew.getPrice();
        PriceLevel row = book.containsKey(limit) ? book.get(limit) : new PriceLevel(this);
        row.add(receiptNew);
        book.put(limit, row);
    }

    public Decimal getFrontRowPrice() {
        if(isEmpty()) throw new CommonExceptions.ContainerEmptyException("book is empty!");
        Decimal frontRowPrice = side==BookSide.BID ? book.lastKey() : book.firstKey();
        return frontRowPrice;
    }

    public boolean isEmpty() {
        return book.isEmpty();
    }

    public int getSize() {
        return book.size();
    }

    public OrderBookSide(BookSide side, IConsumeReceipt _receiptConsumer) {
        this.side = side;
        receiptConsumer = _receiptConsumer;

        book = new TreeMap<Decimal, PriceLevel>();

    }



    private Receipt lastTakerReceipt;
    private BookSide side;
    private IConsumeReceipt receiptConsumer;

    private SortedMap<Decimal,PriceLevel> book;
    private boolean takerReceiptSent;


} // class
