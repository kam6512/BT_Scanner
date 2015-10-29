package com.rainbow.kam.bt_scanner.Deprecated.Adapter.DetailAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sion on 2015-10-14.
 */
public class DetailParentItem { //카드 뷰 틀
    private String parentTitle;
    private String parentUUID;
     ArrayList<ArrayList<HashMap<String, String>>> childItemList;

    public DetailParentItem(String parentTitle,
                            String parentUUID, ArrayList<ArrayList<HashMap<String, String>>> childItemList) {
        this.parentTitle = parentTitle;
        this.parentUUID = parentUUID;
        this.childItemList = childItemList;
    }

    public String getParentTitle() {
        return this.parentTitle;
    }

    public String getParentUUID() {
        return this.parentUUID;
    }



}
