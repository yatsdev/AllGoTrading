package org.yats.messagebus.messages;

import org.yats.trading.PositionRequest;

public class PositionRequestMsg {

    public static final String POSITIONREQUEST_TOPIC = "positionrequest";

    @Override
    public String toString() {
        return "PositionRequestMsg{" +
                "productId='" + productId + '\'' +
                ", accountId='" + accountId + '\'' +
                '}';
    }

    public static PositionRequestMsg fromPositionRequest(PositionRequest request)
    {
        return new PositionRequestMsg(request.getProductId(), request.getAccount());
    }

    public PositionRequest toPositionRequest()
    {
        return new PositionRequest(accountId, productId);
    }

    public PositionRequestMsg() {
    }

    public PositionRequestMsg(String productId, String accountId) {
        this.productId = productId;
        this.accountId = accountId;
    }

    public String productId;
    public String accountId;

} // class
