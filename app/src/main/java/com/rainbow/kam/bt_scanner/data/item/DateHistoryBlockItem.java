package com.rainbow.kam.bt_scanner.data.item;

import android.support.annotation.NonNull;

/**
 * Created by kam6512 on 2016-03-07.
 */
public class DateHistoryBlockItem implements Comparable<DateHistoryBlockItem> {
    public int historyBlockNumber;
    public int totalStep;
    public String historyBlockCalendar;


    @Override
    public int compareTo(@NonNull DateHistoryBlockItem another) {
        return historyBlockCalendar.compareTo(another.historyBlockCalendar);
    }
}
