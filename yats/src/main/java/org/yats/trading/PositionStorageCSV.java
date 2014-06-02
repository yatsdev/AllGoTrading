package org.yats.trading;

import org.yats.common.FileTool;

public class PositionStorageCSV implements IStorePositionSnapshots {

    @Override
    public void store(PositionSnapshot positionSnapshot) {
        FileTool.writeToTextFile(filename, positionSnapshot.toStringCSV()+"\n", true);
    }

    @Override
    public PositionSnapshot readLast() {
        String lastLine = FileTool.getTail(filename,1);
        return PositionSnapshot.fromStringCSV(lastLine);
    }

    public PositionStorageCSV(String filename) {
        this.filename = filename;
    }

    String filename;
}
