package org.yats.connectivity.matching;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.*;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

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
            removeEmptyFrontRows();
        }
    }

    public void cancel(UniqueId orderId) {
        String idString = orderId.toString();
        if(bookByOrderId.containsKey(idString)) {
            bookByOrderId.get(idString).remove(idString);
            bookByOrderId.remove(idString);
            removeEmptyFrontRows();
        }
    }


    @Override
    public void onReceipt(Receipt receipt) {
        takerReceiptSent=true;
        if(receipt.isOpposite(side)) lastTakerReceipt = receipt;
        if(receipt.isEndState()) removeOrderFromBook(receipt.getOrderId().toString());
        receiptConsumer.onReceipt(receipt);
    }

    private void removeOrderFromBook(String orderId) {
        if(bookByOrderId.containsKey(orderId)) bookByOrderId.remove(orderId);
    }

    public void add(OrderNew order) {
        add(order.createReceiptDefault());
    }

    public void add(Receipt receiptNew) {
        Decimal limit = receiptNew.getPrice();
        PriceLevel row = book.containsKey(limit) ? book.get(limit) : new PriceLevel(this);
        row.add(receiptNew);
        book.put(limit, row);
        bookByOrderId.put(receiptNew.getOrderId().toString(), row);
        receiptConsumer.onReceipt(receiptNew.createCopy());
    }

    public Decimal getFrontRowPrice() {
        if(isEmpty()) throw new CommonExceptions.ContainerEmptyException("book is empty!");
        Decimal frontRowPrice = side==BookSide.BID ? book.lastKey() : book.firstKey();
        return frontRowPrice;
    }

    public Decimal getFrontRowSize() {
        if(isEmpty()) return Decimal.ZERO;
        return book.get(getFrontRowPrice()).getCumulativeSize();
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
        bookByOrderId = new ConcurrentHashMap<String, PriceLevel>();
    }

    private void removeEmptyFrontRows() {
        PriceLevel frontRow;
        do {
            Decimal frontRowPrice = getFrontRowPrice();
            frontRow = book.get(frontRowPrice);
            if (frontRow.isEmpty()) book.remove(frontRowPrice);
        } while(frontRow.isEmpty() && book.size()>0);
    }


    private Receipt lastTakerReceipt;
    private BookSide side;
    private IConsumeReceipt receiptConsumer;

    private SortedMap<Decimal,PriceLevel> book;
    private ConcurrentHashMap<String, PriceLevel> bookByOrderId;
    private boolean takerReceiptSent;


} // class
