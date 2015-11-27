package com.rainbow.kam.bt_scanner.NotInUse.Adapter.DetailAdapter;

/**
 * Created by kam6512 on 2015-10-26.
 */
public class DetailChildItem {

    private String childTitle;
    private String childUUID;
//    private String childValue;

    public DetailChildItem(String childTitle,
                           String childUUID, String childValue) {
        this.childTitle = childTitle;
        this.childUUID = childUUID;
//        this.childValue = childValue;
    }

    public String getchildTitle() {
        return this.childTitle;
    }

    public String getchildUUID() {
        return this.childUUID;
    }

//    public String getChildValue() {
//        return this.childValue;
//    }
}
