package org.yats.trading;

import org.joda.time.DateTime;
import org.yats.common.FileTool;

/**
 * Created by abbanerjee on 14/10/14.
 */
public class StorePriceCSV implements IStorePrice {

    public static final String CSV_TIME_SEPARATOR = "@";

    @Override
    public void store(PriceData p) {
        FileTool.writeToTextFile(filename, p.getTimestamp() +CSV_TIME_SEPARATOR + p.getOfferBookAsCSV().toString() + "\n", true);
    }

    @Override
    public PriceData readLast() {
        if (!FileTool.exists(filename)) return PriceData.NULL;
        String lastLine = FileTool.getTail(filename, 1);
        String priceParts[] = lastLine.split(CSV_TIME_SEPARATOR);
        String timeStampString = priceParts[0];
        String priceDataCSV = priceParts[1];

        OfferBook offerbook = OfferBook.fromStringCSV(priceDataCSV);
        return new PriceData(DateTime.parse(timeStampString),
                productId,
                offerbook.getBookRow(BookSide.BID, 0).getPrice(), //bid
                offerbook.getBookRow(BookSide.ASK, 0).getPrice(), //ask
                offerbook.getBookRow(BookSide.BID, 0).getPrice(), //last
                offerbook.getBookRow(BookSide.BID, 0).getSize(),
                offerbook.getBookRow(BookSide.ASK, 0).getSize(),
                offerbook.getBookRow(BookSide.BID, 0).getSize()
        );
    }


    public StorePriceCSV(String baseLocation, String productId) {
        this.productId = productId;
        String username = System.getProperty("user.name").replace(" ", "");
        String path = baseLocation;
        FileTool.createDirectories(path);
        this.filename = baseLocation + "/" + productId + ".csv";

    }

    String filename;
    String productId;
}
