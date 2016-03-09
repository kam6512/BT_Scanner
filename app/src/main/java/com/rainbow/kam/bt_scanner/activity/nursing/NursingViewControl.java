package com.rainbow.kam.bt_scanner.activity.nursing;

import com.rainbow.kam.bt_scanner.data.item.RealmUserActivityItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;

import java.util.List;

/**
 * Created by kam6512 on 2016-03-03.
 */
public interface NursingViewControl {

    void setFragments();

    void setToolbar();

    void setMaterialView();

    void setNavigationView();

    void setMaterialDialog();

    void onDeviceConnected();

    void onDeviceDisconnected();

    void showDeviceSettingSnackBar();

    void showUserSettingSnackBar();

    void showDisconnectDeviceSnackBar();

    void showDeviceSettingFragment();

    void showUserSettingFragment();

    void showGoalSettingFragment();

    void showSwipeRefresh();

    void dismissSwipeRefresh();

    void setDeviceValue(DeviceVo deviceValue);

    void setUpdateTimeValue(String updateValue);

    void setBatteryValue(int batteryValue);

    void updateRssiValue(int rssiValue);

    void setPrimeValue(List<RealmUserActivityItem> primeValue);

    void setPrimeGoalRange(GoalVo goalVo);

    void setPrimeEmptyValue();

    void fail();
}

