package com.rainbow.kam.bt_scanner.adapter.nurse.dashboard_NotInUse;

/**
 * Created by kam6512 on 2015-11-23.
 */
public class DashboardItem {
    private final int step;
    private final int calorie;
    private final int distance;

    private final String calendar;

    public DashboardItem(int step, int calorie, int distance, String calendar) {
        this.step = step;
        this.calorie = calorie;
        this.distance = distance;
        this.calendar = calendar;
    }

    public int getStep() {
        return step;
    }

    public int getCalorie() {
        return calorie;
    }


    public int getDistance() {
        return distance;
    }

    public String getCalendar() {
        return calendar;
    }
}
