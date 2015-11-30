package com.rainbow.kam.bt_scanner.NotInUse.Tools;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-10.
 */
public class BandDeviceList {
    private ArrayList<DeviceListAttribute> list = new ArrayList<>();

    public BandDeviceList() {
        list.add(new DeviceListAttribute("Prime", true, true, false, false, false));
        list.add(new DeviceListAttribute("hesvitband", true, true, false, false, false));

    }

    public DeviceListAttribute getListByName(String name) {
        for (DeviceListAttribute temp : list) {
            if (temp.getDeviceName().equals(name)) {
                return temp;
            }
        }
        return null;
    }

    public ArrayList<DeviceListAttribute> getList() {
        return list;
    }


}
