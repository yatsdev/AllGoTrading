package org.yats.messagebus.messages;

import org.yats.trading.PositionSnapshot;

public class PositionSnapshotMsg {

    @Override
    public String toString() {
        String out = data.replace("\n","|");
        return "PositionSnapshotMsg{" +
                "data='" + out + '\'' +
                '}';
    }

    public String getTopic() {
        return "positionSnapshotMsg";
    }

    public PositionSnapshot toPositionSnapshot() {
        return PositionSnapshot.fromStringCSV(data);
    }

    public static PositionSnapshotMsg fromPositionSnapshot(PositionSnapshot snapshot) {
        PositionSnapshotMsg msg = new PositionSnapshotMsg();
        msg.setData(snapshot.toStringCSV());
        return msg;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String data;

    public boolean isSameAs(PositionSnapshotMsg positionSnapshotMsg) {
        return data.compareTo(positionSnapshotMsg.data) == 0;
    }
}
