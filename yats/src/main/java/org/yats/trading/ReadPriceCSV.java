package org.yats.trading;

import au.com.bytecode.opencsv.CSVReader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.CommonExceptions;

import java.io.FileReader;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by abbanerjee on 15/10/14.
 */
public class ReadPriceCSV implements IReadPrice {

    final Logger log = LoggerFactory.getLogger(ReadPriceCSV.class);
    public static final String CSV_SEPARATOR = "|";

    @Override
    public PriceData read(){

        try {

            String nextLine[] = reader.readNext();
            String stringCSV;
            stringCSV = nextLine[0];
            for(int i=1;i< nextLine.length;i++){
                stringCSV = stringCSV + "," + nextLine[i];
            }


            OfferBook offerbook = OfferBook.fromStringCSV(stringCSV);

            return new PriceData(DateTime.now(DateTimeZone.UTC),
                    productId,
                    offerbook.getBookRow(BookSide.BID, 0).getPrice(), //bid
                    offerbook.getBookRow(BookSide.ASK, 0).getPrice(), //ask
                    offerbook.getBookRow(BookSide.BID, 0).getPrice(), //last
                    offerbook.getBookRow(BookSide.BID, 0).getSize(),
                    offerbook.getBookRow(BookSide.ASK, 0).getSize(),
                    offerbook.getBookRow(BookSide.BID, 0).getSize()
            );





        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonExceptions.FileReadException(e.getMessage());
        }


    }

    public ReadPriceCSV(String baseLocation, String productId){
        this.productId = productId;
        this.path = baseLocation + "/" + productId + ".csv";
        try {
             reader = new CSVReader(new FileReader(this.path));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonExceptions.FileReadException(e.getMessage());
    }

    }
    private String baseLocation;
    private String productId;
    private List<String> offerBookCSVList;
    private String path;
    private BigInteger listLength;
    CSVReader reader;
}
