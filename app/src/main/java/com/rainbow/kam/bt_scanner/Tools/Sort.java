package com.rainbow.kam.bt_scanner.Tools;

import com.rainbow.kam.bt_scanner.Adapter.DeviceItem;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by sion on 2015-10-14.
 */
public class Sort {

    //Collator 초기화
    private final Collator collator = Collator.getInstance();

    //RSSI 정렬
    public final Comparator<DeviceItem> COMPARATOR_RSSI = new Comparator<DeviceItem>() {


        @Override
        public int compare(DeviceItem lhs, DeviceItem rhs) {
            return collator.compare(String.valueOf(lhs.getExtraRssi()), String.valueOf(rhs.getExtraRssi()));
        }
    };

    //네임 정렬
    public final Comparator<DeviceItem> COMPARATOR_NAME = new Comparator<DeviceItem>() {


        @Override
        public int compare(DeviceItem lhs, DeviceItem rhs) {
            if (lhs.getExtraName() == null || rhs.getExtraName() == null) {
                return 0;
            } else {
                return collator.compare(lhs.getExtraName(), rhs.getExtraName());
            }
        }
    };

    //타입 정렬
    public final Comparator<DeviceItem> COMPARATOR_TYPE = new Comparator<DeviceItem>() {


        @Override
        public int compare(DeviceItem lhs, DeviceItem rhs) {
            return collator.compare(String.valueOf(lhs.getExtraType()), String.valueOf(rhs.getExtraType()));
        }
    };
}
