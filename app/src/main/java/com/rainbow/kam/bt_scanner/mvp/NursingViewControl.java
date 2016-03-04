package com.rainbow.kam.bt_scanner.mvp;

import android.content.Context;

import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.trello.rxlifecycle.ActivityLifecycleProvider;

import java.util.List;

/**
 * Created by kam6512 on 2016-03-03.
 */
public interface NursingViewControl extends BaseNursingViewControl {

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

    void setPrimeValue(List<RealmPrimeItem> primeValue);

    void setPrimeGoalRange(GoalVo goalVo);

    void setPrimeEmptyValue();

    void fail();

}

interface BaseNursingViewControl {
    Context getContext();

    ActivityLifecycleProvider ActivityLifecycleProvider();
}