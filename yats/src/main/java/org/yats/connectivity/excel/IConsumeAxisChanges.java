package org.yats.connectivity.excel;

import java.util.Collection;

public interface IConsumeAxisChanges {
    void onFirstRowChange(Collection<String> firstRow);
    void onFirstColumnChange(Collection<String> firstColumn);
}
