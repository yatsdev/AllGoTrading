package org.yats.trading;

import org.yats.common.Decimal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PositionSnapshot {



    public boolean isSameAs(PositionSnapshot positionSnapshot) {
        if(positionMap.size()!=positionSnapshot.positionMap.size()) return false;
        for(AccountPosition a : positionMap.values()) {
            String key = a.getKey();
            if(!positionSnapshot.positionMap.containsKey(key)) return false;
            if(!positionMap.get(key).isSameAs(positionSnapshot.positionMap.get(key))) return false;
        }
        return true;
    }

    public String toStringCSV() {
        StringBuilder b = new StringBuilder();
        for(AccountPosition a : positionMap.values()) {
            b.append(a.toStringCSV());
            b.append("\n");
        }
        return b.toString();
    }

    public static PositionSnapshot fromStringCSV(String csv) {
        PositionSnapshot p = new PositionSnapshot();
        String[] positionStrings = csv.split("\n");
        for(String s : positionStrings) {
            AccountPosition a = AccountPosition.fromStringCSV(s);
            p.add(a);
        }
        return p;
    }

    public AccountPosition getAccountPosition(PositionRequest r) {
        AccountPosition position = new AccountPosition("", "", Decimal.ZERO);
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
        
        //I think to begin to understand...
        //   System.out.println(positionMap.values()); holds three AccountPosition objects [org.yats.trading.AccountPosition@61dd025, org.yats.trading.AccountPosition@124c278f, org.yats.trading.AccountPosition@15b204a1]
        //   System.out.println(positionSnapshot.positionMap.values()); holds the account position that I add in the test (positionSnapshot.add(positionSnapshot2);) and is [org.yats.trading.AccountPosition@61dd025]
        // But in order to "sum" positionMap and positionSnapshot.positionMap I should retrive positionSnapshot.positionMap keys. How can I do that?
        // can I add new attributes to this class or change other methods' signatures/bodies (apart for where I read throw new NotImplementedException(); like in this method's body)?
        
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

    public int size() {
        return positionMap.size();
    }
} // class
