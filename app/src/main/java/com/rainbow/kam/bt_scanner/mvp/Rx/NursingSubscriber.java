package com.rainbow.kam.bt_scanner.mvp.Rx;

import android.content.Context;

import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.mvp.NursingViewControl;

import java.util.List;
import java.util.Observable;

import rx.Observer;
import rx.Subscriber;

/**
 * Created by kam6512 on 2016-03-07.
 */
public abstract class NursingSubscriber extends Subscriber {
    public abstract void setFragments();

    public abstract void setToolbar();

    public abstract void setMaterialView();

    public abstract void setNavigationView();

    public abstract void setMaterialDialog();

    public abstract void onDeviceConnected();

    public abstract void onDeviceDisconnected();

    public abstract void showDeviceSettingSnackBar();

    public abstract void showUserSettingSnackBar();

    public abstract void showDisconnectDeviceSnackBar();

    public abstract void showDeviceSettingFragment();

    public abstract void showUserSettingFragment();

    public abstract void showGoalSettingFragment();

    public abstract void showSwipeRefresh();

    public abstract void dismissSwipeRefresh();

    public abstract void setDeviceValue(DeviceVo deviceValue);

    public abstract void setUpdateTimeValue(String updateValue);

    public abstract void setBatteryValue(int batteryValue);

    public abstract void updateRssiValue(int rssiValue);

    public abstract void setPrimeValue(List<RealmPrimeItem> primeValue);

    public abstract void setPrimeGoalRange(GoalVo goalVo);

    public abstract void setPrimeEmptyValue();

    public abstract void fail();
}
