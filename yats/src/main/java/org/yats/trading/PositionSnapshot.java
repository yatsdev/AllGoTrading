package org.yats.trading;

import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.common.Tool;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PositionSnapshot implements IProvidePosition {


    @Override
    public AccountPosition getAccountPosition(PositionRequest positionRequest) {
        AccountPosition position = new AccountPosition("", "", Decimal.ZERO);
        String key = positionRequest.getAsKey();
        if (positionMap.containsKey(key)) {
            position = positionMap.get(key);
        }
        return position;
    }

    @Override
    public IProvidePosition getAllPositionsForOneAccount(String accountId) {
        PositionSnapshot snapshot = new PositionSnapshot();
        Collection<AccountPosition> newPosition = positionMap.values();
        for (AccountPosition p : newPosition) {
            if(p.isForAccount(accountId)) {
                snapshot.add(p);
            }
        }
        return snapshot;
    }

    @Override
    public Collection<AccountPosition> getAllPositions()
    {
        return positionMap.values();
    }

    @Override
    public Position getValueForAccountProduct(String targetProductId, PositionRequest request) {
        Position p = getAccountPosition(request);
        Position targetPosition = rateConverter.convert(p, targetProductId);
        return targetPosition;
    }

    @Override
    public Position getValueForAccount(String targetProductId, String accountId) {
        IProvidePosition positionsOfAccount = getAllPositionsForOneAccount(accountId);
        Position targetPosition = positionsOfAccount.getValueForAllPositions(targetProductId);
        return targetPosition;
    }

    @Override
    public Position getValueForAllPositions(String targetProductId) {
        Position result = new Position(targetProductId, Decimal.ZERO);
        for(Position p : positionMap.values()) {
            Position additional = rateConverter.convert(p, targetProductId);
            result = result.add(additional);
        }
        return result;
    }

    public int size() {
        return positionMap.size();
    }

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

        b.append(lastChange.toString());
        b.append(";");
        for(AccountPosition a : positionMap.values()) {
            b.append(a.toStringCSV());
            b.append('|');
        }
        return b.toString();
    }

    public static PositionSnapshot fromStringCSV(String csv) {
        PositionSnapshot p = new PositionSnapshot();
        String[] snapshotParts = csv.split(";");
        String positionPartString="";
        if(snapshotParts.length==1 && csv.contains(";")) {
            p.lastChange = DateTime.parse(snapshotParts[0]);
        } else if(snapshotParts.length>1) {
            p.lastChange = DateTime.parse(snapshotParts[0]);
            positionPartString = snapshotParts[1];
        } else {
            positionPartString = csv;
        }

        String[] positionStrings = positionPartString.split("\\|");
        for(String s : positionStrings) {
            if(s.isEmpty()) continue;
            AccountPosition a = AccountPosition.fromStringCSV(s);
            p.add(a);
        }
        return p;
    }

    public Position getPositionForAllAccounts(String productId) {
        Position positionForAllAccounts=new Position(productId, Decimal.ZERO);
        for (AccountPosition a : positionMap.values()) {
            if(!positionForAllAccounts.isForProductId(a.getProductId())) continue;
            positionForAllAccounts = positionForAllAccounts.add(a);
        }
        return positionForAllAccounts;
    }


//    public List<AccountPosition> getAllPositionsForOneAccount(String account) {
//        Collection<AccountPosition> newPosition = positionMap.getAllPositions();
//        ArrayList<AccountPosition> arrayList= new ArrayList<AccountPosition>();
//        for (AccountPosition p : newPosition) {
//            if(p.getInternalAccount().compareTo(account)==0) {
//                arrayList.add(p);
//            }
//        }
//        return arrayList;
//    }

    public void add(PositionSnapshot other) {
       Collection<AccountPosition> newPositionCollection = other.positionMap.values();
       for (AccountPosition p : newPositionCollection) {
            add(p);
        }
        updateLastChange();
    }

    public void add(AccountPosition p) {
        AccountPosition newPosition = p;
        String key = p.getKey();
        if (positionMap.containsKey(key)) {
            AccountPosition oldPosition = positionMap.get(key);
            newPosition = oldPosition.add(newPosition);
        }
        positionMap.put(key, newPosition);
        updateLastChange();
    }

    public void setRateConverter(IConvertRate rateConverter) {
        this.rateConverter = rateConverter;
    }

    public PositionSnapshot() {
        positionMap = new ConcurrentHashMap<String, AccountPosition>();
        rateConverter = new RateConverter(new ProductList());
        updateLastChange();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void updateLastChange() {
        lastChange = Tool.getUTCTimestamp();
    }

    private DateTime lastChange;
    private ConcurrentHashMap<String, AccountPosition> positionMap;
    private IConvertRate rateConverter;

} // class
