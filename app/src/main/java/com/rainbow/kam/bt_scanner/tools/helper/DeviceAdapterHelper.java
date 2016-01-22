package com.rainbow.kam.bt_scanner.tools.helper;

import com.rainbow.kam.bt_scanner.adapter.device.DeviceItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by kam6512 on 2016-01-08.
 */
public class DeviceAdapterHelper {

    public static DeviceItem getValue(LinkedHashMap<String, DeviceItem> hashMap, int position) {
        Map.Entry<String, DeviceItem> deviceItemEntry = null;
        Set<Map.Entry<String, DeviceItem>> entries = hashMap.entrySet();
        int index = 0;
        for (Map.Entry<String, DeviceItem> entry : entries) {
            if (index++ == position) {
                deviceItemEntry = entry;
            }
//            Log.i("getValue Entry", entry.getValue().getExtraAddress());
        }

//        for (DeviceItem deviceItem : hashMap.values()) {
//            Log.i("getValue DeviceItem", deviceItem.getExtraAddress());
//        }
        return deviceItemEntry.getValue();
    }
}
