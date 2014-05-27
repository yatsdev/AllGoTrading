package org.yats.trading;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PositionSnapshot {

    public String toStringCSV() {
        throw new NotImplementedException();
    }

    public static PositionSnapshot fromStringCSV(String csv) {
        throw new NotImplementedException();
    }

    public AccountPosition getAccountPosition(PositionRequest r) {
        AccountPosition position = new AccountPosition("", Decimal.ZERO, "");
        String key = r.getProductId() + "," + r.getAccount();
        if (positionMap.containsKey(key)) {
            position = positionMap.get(key);
        }
        return position;
    }

    public Position getPositionForAllAccounts(String productId) {
        return new Position("pid", Decimal.ZERO);
    }

    public List<AccountPosition> getAllPositionsForOneAccount(String account) {
        return new ArrayList<AccountPosition>();
    }

    public void add(PositionSnapshot positionSnapshot) {
        throw new NotImplementedException();
    }

    public void add(AccountPosition p) {
        AccountPosition newPosition = p;
        String key = p.getKey();
        if (positionMap.containsKey(key)) {
            AccountPosition oldPosition = positionMap.get(key);
            newPosition = oldPosition.add(newPosition);
        }
        positionMap.put(key, newPosition);
    }

    public PositionSnapshot() {
        positionMap = new ConcurrentHashMap<String, AccountPosition>();
    }

    private ConcurrentHashMap<String, AccountPosition> positionMap;

} // class
