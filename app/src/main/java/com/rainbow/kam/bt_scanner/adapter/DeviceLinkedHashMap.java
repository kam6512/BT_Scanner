package com.rainbow.kam.bt_scanner.adapter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by kam6512 on 2016-01-08.
 */
class DeviceLinkedHashMap<String, DeviceItem> extends LinkedHashMap<String, DeviceItem> {


    public final DeviceItem getValue(int position) {
        Map.Entry<String, DeviceItem> deviceItemEntry = this.getEntry(position);
        if (deviceItemEntry == null) {
            return null;
        }
        return deviceItemEntry.getValue();
    }


    final Map.Entry<String, DeviceItem> getEntry(int position) {
        Set<Map.Entry<String, DeviceItem>> entries = entrySet();
        int index = 0;
        for (Map.Entry<String, DeviceItem> entry : entries) {
            if (index++ == position) {
                return entry;
            }
        }
        return null;
    }
}
