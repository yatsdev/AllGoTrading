package org.yats.trading;

import org.yats.common.IProvideProperties;

public interface IConsumeReports {
    public void onReport(IProvideProperties p, boolean hasMoreReports);
} // class
