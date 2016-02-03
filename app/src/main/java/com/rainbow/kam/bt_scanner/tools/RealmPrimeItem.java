package com.rainbow.kam.bt_scanner.tools;

import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-20.
 */
public class RealmPrimeItem extends RealmObject {

    private String calendar;

    private int step, calorie, distance;

    private static int totalStep, totalCalorie, totalDistance;


    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }


    public String getCalendar() {
        return calendar;
    }


    public void setStep(int step) {
        this.step = step;
    }


    public int getStep() {
        return this.step;
    }


    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }


    public int getCalorie() {
        return this.calorie;
    }


    public void setDistance(int distance) {
        this.distance = distance;
    }


    public int getDistance() {
        return this.distance;
    }


    public static void setTotalValue(RealmResults<RealmPrimeItem> results) {
        totalStep = 0;
        totalCalorie = 0;
        totalDistance = 0;
        for (RealmPrimeItem realmPrimeItem : results) {
            totalStep += realmPrimeItem.getStep();
            totalCalorie += realmPrimeItem.getCalorie();
            totalDistance += realmPrimeItem.getDistance();
        }
    }


    public static int getTotalValue(int index) {
        switch (index) {
            case 0:
                return totalStep;

            case 1:
                return totalCalorie;

            case 2:
                return totalDistance;

            default:
                return totalStep;
        }
    }
}
