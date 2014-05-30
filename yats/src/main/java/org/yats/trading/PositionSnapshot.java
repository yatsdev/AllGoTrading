package org.yats.trading;

import org.yats.common.Decimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PositionSnapshot {


    private ConcurrentHashMap<String, AccountPosition> positionMap;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Position>> productAccountMap;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Position>> accountProductMap;

    public PositionSnapshot() {
        positionMap = new ConcurrentHashMap<String, AccountPosition>();

    }

    public static PositionSnapshot fromStringCSV(String csv) {
        PositionSnapshot p = new PositionSnapshot();
        String[] positionStrings = csv.split("\n");
        for (String s : positionStrings) {
            AccountPosition a = AccountPosition.fromStringCSV(s);
            p.add(a);
        }
        return p;
    }

    public boolean isSameAs(PositionSnapshot positionSnapshot) {
        if (positionMap.size() != positionSnapshot.positionMap.size()) return false;
        for (AccountPosition a : positionMap.values()) {
            String key = a.getKey();
            if (!positionSnapshot.positionMap.containsKey(key)) return false;
            if (!positionMap.get(key).isSameAs(positionSnapshot.positionMap.get(key))) return false;
        }
        return true;
    }

    public String toStringCSV() {
        StringBuilder b = new StringBuilder();
        for (AccountPosition a : positionMap.values()) {
            b.append(a.toStringCSV());
            b.append("\n");
        }
        return b.toString();
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

        Position getPositionForAllAccounts=new Position("pid", Decimal.ZERO);

        Decimal position=Decimal.ZERO;

        for (AccountPosition a : positionMap.values()) {
            String key = a.getKey();
            getPositionForAllAccounts = positionMap.get(key);

            if(getPositionForAllAccounts.getProductId().compareTo(productId)==0)
            {
                position=position.add(getPositionForAllAccounts.getSize());

            }

        }

        getPositionForAllAccounts.setSize(position);
        return getPositionForAllAccounts;
    }

    public List<AccountPosition> getAllPositionsForOneAccount(String account) {

        Collection<AccountPosition> newPosition;
        newPosition = positionMap.values();

        ArrayList<AccountPosition> arrayList= new ArrayList<AccountPosition>();

        for (int i=0;i<newPosition.size();i++) {
            AccountPosition p = newPosition.iterator().next();
            if(p.getInternalAccount().compareTo(account)==0) {
                arrayList.add(p);
            }
        }


        return arrayList;
    }

    public void add(PositionSnapshot positionSnapshot) {

       Collection<AccountPosition> newPosition;
       newPosition = positionSnapshot.positionMap.values();

       for (int i=0;i<newPosition.size();i++) {
            AccountPosition p = newPosition.iterator().next();
           //System.out.println(p.toStringCSV());
            add(p);
           //System.out.println(i+" Iteration");
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

    public int size() {
        return positionMap.size();
    }


} // class
