package org.yats.trading;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.yats.common.FileTool;

/**
 * Created by macbook52 on 14/10/14.
 */
public class StorePriceCSV implements IStorePrice {

    @Override
    public void store(PriceData p) {
        FileTool.writeToTextFile(filename, p.getOfferBookAsCSV().toString() + "\n", true);
    }

    @Override
    public PriceData readLast() {
        if (!FileTool.exists(filename)) return PriceData.NULL;
        String lastLine = FileTool.getTail(filename, 1);
        OfferBook offerbook = OfferBook.fromStringCSV(lastLine);
        return new PriceData(DateTime.now(DateTimeZone.UTC),
                productId,
                offerbook.getBookRow(BookSide.BID, 0).getPrice(), //bid
                offerbook.getBookRow(BookSide.ASK, 0).getPrice(), //ask
                offerbook.getBookRow(BookSide.BID, 0).getPrice(), //last
                offerbook.getBookRow(BookSide.BID, 0).getSize(),
                offerbook.getBookRow(BookSide.ASK, 0).getSize(),
                offerbook.getBookRow(BookSide.BID, 0).getSize()
        );
    }


    public StorePriceCSV(String productId) {
        this.productId = productId;
        String username = System.getProperty("user.name").replace(" ", "");
        String path = "config/" + username;
        this.filename = path + "/" + productId + ".csv";
    }

    String filename;
    String productId;
}
