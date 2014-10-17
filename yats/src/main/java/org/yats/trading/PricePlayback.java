package org.yats.trading;

public class PricePlayback {



    public void setReader(IReadPrices reader) {
        this.reader = reader;
    }

    public IReadPrices getReader() {
        return reader;
    }

    private IReadPrices reader;


    public PriceData getNext() {
        return null;
    }
} // class
