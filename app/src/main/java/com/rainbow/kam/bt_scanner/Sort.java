package com.rainbow.kam.bt_scanner;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by sion on 2015-10-14.
 */
public class Sort {

    private final Collator collator = Collator.getInstance();

    public final Comparator<DeviceItem> COMPARATOR_RSSI = new Comparator<DeviceItem>() {


        @Override
        public int compare(DeviceItem lhs, DeviceItem rhs) {
            return collator.compare(String.valueOf(lhs.getExtraRssi()), String.valueOf(rhs.getExtraRssi()));
        }
    };
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

    public final Comparator<DeviceItem> COMPARATOR_TYPE = new Comparator<DeviceItem>() {


        @Override
        public int compare(DeviceItem lhs, DeviceItem rhs) {
            return collator.compare(String.valueOf(lhs.getExtraType()), String.valueOf(rhs.getExtraType()));
        }
    };
}
