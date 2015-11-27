package com.rainbow.kam.bt_scanner.Nursing.PatientRealmOBJ;

import io.realm.RealmObject;

/**
 * Created by kam6512 on 2015-11-20.
 */
public class BandData extends RealmObject {

    private String calendar = null;

    private int step;
    private int calorie;
    private int distance;

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
}