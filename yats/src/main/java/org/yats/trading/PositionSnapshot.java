package org.yats.trading;

import org.yats.common.Decimal;

import java.util.ArrayList;
import java.util.Collection;
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
        Position positionForAllAccounts=new Position(productId, Decimal.ZERO);
        for (AccountPosition a : positionMap.values()) {
            if(!positionForAllAccounts.isForProductId(a.getProductId())) continue;
            positionForAllAccounts = positionForAllAccounts.add(a);
        }
        return positionForAllAccounts;
    }

       public List<AccountPosition> getAllPositionsForOneAccount(String account) {
        Collection<AccountPosition> newPosition = positionMap.values();
        ArrayList<AccountPosition> arrayList= new ArrayList<AccountPosition>();
        for (AccountPosition p : newPosition) {
            if(p.getInternalAccount().compareTo(account)==0) {
                arrayList.add(p);
            }
        }
        return arrayList;
    }

    public void add(PositionSnapshot other) {
       Collection<AccountPosition> newPositionCollection = other.positionMap.values();
       for (AccountPosition p : newPositionCollection) {
            add(p);
        }
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
