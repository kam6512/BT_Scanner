package com.rainbow.kam.bt_scanner.data.item;

/**
 * Created by kam6512 on 2015-11-20.
 */
public class UserMovementItem {

    private String calendar;

    private int step, calorie, distance;


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


//    @Override public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        UserMovementItem realmUserActivityItem = (UserMovementItem) o;
//        return Objects.equal(this.calendar, realmUserActivityItem.calendar)
//                && Objects.equal(this.step, realmUserActivityItem.step)
//                && Objects.equal(this.calorie, realmUserActivityItem.calorie)
//                && Objects.equal(this.distance, realmUserActivityItem.distance);
//    }


//    @Override
//    public int hashCode() {
//        return Objects.hashCode(this.calendar, this.step, this.calorie, this.distance);
//    }


//    @Override
//    public String toString() {
//        return MoreObjects.toStringHelper(this)
//                .add("calendar", calendar)
//                .add("step", step)
//                .add("calorie", calorie)
//                .add("distance", distance)
//                .toString();
//    }

}
