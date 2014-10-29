package org.yats.trading;

/**
 * Created by abbanerjee on 22/10/14.
 */
public class OpenHighLowClose
{
    private boolean isComplete;
    private PriceData open,high,low,close;

    public void setComplete(boolean completeStatus){
        isComplete = completeStatus;
    }

    public void process(PriceData p){
        close = p;
        if(open == null){
            open = p;
            high = p;
            low = p;
        }
        if(p.getBid().isGreaterThan(high.getBid()) || p.getBid().isEqualTo(high.getBid()) ){
            high = p;
        }

        if(p.getBid().isLessThan(low.getBid()) || p.getBid().isEqualTo(low.getBid()) ){
            low = p;
        }


    }

    public PriceData getOpen(){
        return open;
    }

    public PriceData getHigh(){
        return high;
    }

    public PriceData getLow(){
        return low;
    }

    public PriceData getClose(){
        return close;
    }

    public void OpenHighLowClose(){
        open = high = low = close = null;
    }

    public String toString(){
        if(open == null){
            return "Not initialized";
        }
        String stringValue = "{ {Open = " + open.getBid() + "} "+ "{High = " + high.getBid() + "} " +
                "{Low = " + low.getBid() + "} " + "{Close = " + close.getBid() + "} }";
        return stringValue;
    }


}
