package com.rainbow.kam.bt_scanner.activity.nursing.Rx;

import com.rainbow.kam.bt_scanner.data.item.RealmUserActivityItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;

import java.util.List;

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

    public abstract void setPrimeValue(List<RealmUserActivityItem> primeValue);

    public abstract void setPrimeGoalRange(GoalVo goalVo);

    public abstract void setPrimeEmptyValue();

    public abstract void fail();
}
//    private NursingSubscriber nursingSubscriber = new NursingSubscriber(){
//
//        @Override
//        public void onCompleted() {
//
//        }
//
//
//        @Override
//        public void onError(Throwable e) {
//
//        }
//
//
//        @Override
//        public void onNext(Object o) {
//
//        }
//
//
//        @Override
//        public void setFragments() {
//            fragmentManager = getSupportFragmentManager();
//            int fragmentLayoutId = R.id.nursing_fragment_frame;
//
//            deviceListFragment = new DeviceListFragment();
//            userDataDialogFragment = new UserDataDialogFragment();
//            goalDialogFragment = new GoalDialogFragment();
//
//            nursingFragment = new NursingFragment();
//            fragmentManager.beginTransaction().replace(fragmentLayoutId, nursingFragment).commit();
//        }
//
//
//        @Override
//        public void setToolbar() {
//            final Toolbar toolbar = (Toolbar) findViewById(R.id.nursing_toolbar);
//            setSupportActionBar(toolbar);
//
//            final ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
//                actionBar.setDisplayHomeAsUpEnabled(true);
//            }
//
//            toolbarRssi = (TextView) findViewById(R.id.nursing_toolbar_rssi);
//            toolbarRssi.setText(none);
//            rssiUnit = getString(R.string.bt_rssi_unit);
//            toolbarBluetoothFlag = (ImageView) findViewById(R.id.nursing_toolbar_bluetoothFlag);
//        }
//
//
//        @Override
//        public void setMaterialView() {
//            drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);
//            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);
//        }
//
//
//        @Override
//        public void setNavigationView() {
//            NavigationView navigationView = (NavigationView) findViewById(R.id.nursing_nav_view);
//            navigationView.setNavigationItemSelectedListener(NursingActivity.this);
//
//            View navHeader = navigationView.getHeaderView(0);
//
//            navDeviceName = (TextView) navHeader.findViewById(R.id.nursing_device_name);
//            navDeviceName.setText(none);
//
//            navDeviceAddress = (TextView) navHeader.findViewById(R.id.nursing_device_address);
//            navDeviceAddress.setText(none);
//
//            navUpdate = (TextView) navHeader.findViewById(R.id.nursing_update);
//            navUpdate.setText(none);
//
//            navBattery = (ImageView) navHeader.findViewById(R.id.nursing_battery);
//        }
//
//
//        @Override
//        public void setMaterialDialog() {
//            final int REMOVE_USER = 0, REMOVE_HISTORY = 1, REMOVE_ALL = 2;
//            final List<String> removeChoiceList = new ArrayList<>();
//            removeChoiceList.add(getString(R.string.nursing_remove_item_user_device));
//            removeChoiceList.add(getString(R.string.nursing_remove_item_exercise));
//            removeChoiceList.add(getString(R.string.nursing_remove_item_all));
//            removeDialog = new MaterialDialog.Builder(NursingActivity.this)
//                    .title(R.string.nursing_remove_title)
//                    .items(removeChoiceList)
//                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
//                        @Override
//                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                            return false;
//                        }
//                    })
//                    .positiveText(R.string.nursing_remove_accept).onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//
//                            switch (dialog.getSelectedIndex()) {
//                                case REMOVE_USER:
//                                    presenter.removeDeviceUserData();
//                                    break;
//                                case REMOVE_HISTORY:
//                                    presenter.removeHistoryData();
//                                    break;
//                                case REMOVE_ALL:
//                                    presenter.removeDeviceUserData();
//                                    presenter.removeHistoryData();
//                                    break;
//                            }
//                            dialog.dismiss();
//                        }
//                    })
//                    .negativeText(R.string.nursing_remove_denied).onNegative(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .onAny(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            presenter.registerBluetooth();
//                        }
//                    })
//                    .canceledOnTouchOutside(false).build();
//
//
//            reconnectDialog = new MaterialDialog.Builder(NursingActivity.this)
//                    .title(R.string.nursing_reconnect_title)
//                    .content(R.string.nursing_reconnect_content)
//                    .positiveText(R.string.nursing_reconnect_accept)
//                    .negativeText(R.string.nursing_reconnect_denied)
//                    .onNegative(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            presenter.removeHistoryData();
//                        }
//                    })
//                    .onAny(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            deviceListFragment.dismiss();
//                            presenter.registerBluetooth();
//                        }
//                    }).build();
//        }
//
//
//        @Override
//        public void onDeviceConnected() {
//            toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);
//
//            dismissSwipeRefresh();
//        }
//
//
//        @Override
//        public void onDeviceDisconnected() {
//            navDeviceName.setText(none);
//            navDeviceAddress.setText(none);
//            navUpdate.setText(none);
//            setBatteryValue(-1);
//
//            toolbarRssi.setText(none);
//            toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
//        }
//
//
//        @Override
//        public void showDeviceSettingSnackBar() {
//            deviceSettingSnackBar = Snackbar.make(coordinatorLayout, R.string.nursing_setting_device, Snackbar.LENGTH_INDEFINITE).setAction(R.string.nursing_setting_device_action, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    deviceListFragment.show(fragmentManager, getString(R.string.nursing_setting_device_tag));
//                }
//            });
//            deviceSettingSnackBar.show();
//        }
//
//
//        @Override
//        public void showUserSettingSnackBar() {
//            Snackbar.make(coordinatorLayout, R.string.nursing_setting_user, Snackbar.LENGTH_LONG).setAction(R.string.nursing_setting_user_action, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    userDataDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_user_tag));
//                }
//            }).show();
//        }
//
//
//        @Override
//        public void showDisconnectDeviceSnackBar() {
//            Snackbar.make(coordinatorLayout, R.string.nursing_disconnect_snack, Snackbar.LENGTH_INDEFINITE).setAction(R.string.nursing_ignore, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    presenter.applicationExit();
//                }
//            }).show();
//        }
//
//
//        @Override
//        public void showDeviceSettingFragment() {
//            deviceListFragment.show(fragmentManager, getString(R.string.nursing_setting_device_tag));
//        }
//
//
//        @Override
//        public void showUserSettingFragment() {
//            userDataDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_user_tag));
//        }
//
//
//        @Override
//        public void showGoalSettingFragment() {
//            goalDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_goal_tag));
//        }
//
//
//        @Override
//        public void showSwipeRefresh() {
//            nursingFragment.setRefreshing(true);
//        }
//
//
//        @Override
//        public void dismissSwipeRefresh() {
//            if (nursingFragment.isRefreshing()) {
//                nursingFragment.setRefreshing(false);
//            }
//        }
//
//
//        @Override
//        public void setDeviceValue(DeviceVo deviceValue) {
//            navDeviceName.setText(deviceValue.name);
//            navDeviceAddress.setText(deviceValue.address);
//        }
//
//
//        @Override
//        public void setUpdateTimeValue(String updateValue) {
//            navUpdate.setText(updateValue);
//        }
//
//
//        @Override
//        public void setBatteryValue(int batteryValue) {
//            Drawable drawable;
//            if (0 <= batteryValue && batteryValue <= 25)
//                drawable = ContextCompat.getDrawable(NursingActivity.this, R.drawable.ic_battery_alert_white_36dp);
//            else if (25 < batteryValue && batteryValue <= 100) {
//                drawable = ContextCompat.getDrawable(NursingActivity.this, R.drawable.ic_battery_std_white_36dp);
//            } else {
//                drawable = ContextCompat.getDrawable(NursingActivity.this, R.drawable.ic_battery_unknown_white_36dp);
//            }
//
//            navBattery.setImageDrawable(drawable);
//        }
//
//
//        @Override
//        public void updateRssiValue(int rssiValue) {
//            toolbarRssi.setText(rssiValue + rssiUnit);
//        }
//
//
//        @Override
//        public void setnursingValue(List<RealmUserActivityItem> nursingValue) {
//            nursingFragment.setnursingValue(nursingValue);
//        }
//
//
//        @Override
//        public void setnursingGoalRange(GoalVo goalVo) {
//            nursingFragment.setCircleCounterGoalRange(goalVo);
//        }
//
//
//        @Override
//        public void setnursingEmptyValue() {
//            nursingFragment.setValueEmpty();
//        }
//
//
//        @Override
//        public void fail() {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    nursingFragment.setTextFail();
//                }
//            });
//        }
//    };
