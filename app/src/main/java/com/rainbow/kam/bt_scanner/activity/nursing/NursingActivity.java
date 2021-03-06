package com.rainbow.kam.bt_scanner.activity.nursing;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.profile.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.device.DeviceAdapter;
import com.rainbow.kam.bt_scanner.data.item.RealmUserActivityItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.fragment.device.DeviceListFragment;
import com.rainbow.kam.bt_scanner.fragment.nursing.menu.GoalDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.nursing.menu.UserDataDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.nursing.user.NursingFragment;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kam6512 on 2016-03-03.
 */

public class NursingActivity extends AppCompatActivity implements
        NursingViewControl,
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener,
        UserDataDialogFragment.OnSaveUserDataListener,
        GoalDialogFragment.OnSaveGoalListener,
        NursingFragment.OnRefreshListener {


    private NursingPresenter presenter;

    public static final int INDEX_STEP = 0;
    public static final int INDEX_CALORIE = 1;
    public static final int INDEX_DISTANCE = 2;

    private String rssiUnit;
    private final String none = "--";

    private FragmentManager fragmentManager;

    private NursingFragment nursingFragment;
    private UserDataDialogFragment userDataDialogFragment;
    private DeviceListFragment deviceListFragment;
    private GoalDialogFragment goalDialogFragment;

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;

    private ImageView navBattery;
    private TextView navDeviceName, navDeviceAddress, navUpdate;

    private MaterialDialog removeDialog, reconnectDialog;

    private Snackbar deviceSettingSnackBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothHelper.requestBluetoothPermission(this);
        }

        setContentView(R.layout.a_nursing);


        presenter = new NursingPresenter(this);

        presenter.initDB();
        presenter.initializeViews();
    }


    @Override
    protected void onResume() {
        super.onResume();
        presenter.registerBluetooth();
    }


    @Override
    public void onPause() {
        super.onPause();
        presenter.disconnectDevice();
    }


    @Override
    public void onRefresh() {
        presenter.onRefresh();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menu_nursing_setting_device:

                presenter.disconnectDevice();

                dismissSwipeRefresh();

                showDeviceSettingFragment();

                if (deviceSettingSnackBar != null) {
                    deviceSettingSnackBar.dismiss();
                }

                return true;

            case R.id.menu_nursing_setting_user:

                presenter.userSettingPressed();

                return true;

            case R.id.menu_nursing_setting_goal:

                presenter.goalSettingPressed();

                return true;

            case R.id.menu_nursing_about_dev:

                startActivity(new Intent(NursingActivity.this, MainActivity.class));

                return true;

            case R.id.menu_nursing_about_setting:

                presenter.disconnectDevice();

                dismissSwipeRefresh();

                removeDialog.show();

                return true;

            case R.id.menu_nursing_about_about:

                Snackbar.make(coordinatorLayout, "준비중입니다..", Snackbar.LENGTH_SHORT).show();

                return true;
            default:
                return true;
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            presenter.backPressed();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onRequestEnableResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    @Override
    public void setFragments() {
        fragmentManager = getSupportFragmentManager();
        int fragmentLayoutId = R.id.nursing_fragment_frame;

        deviceListFragment = new DeviceListFragment();
        userDataDialogFragment = new UserDataDialogFragment();
        goalDialogFragment = new GoalDialogFragment();

        nursingFragment = new NursingFragment();
        fragmentManager.beginTransaction().replace(fragmentLayoutId, nursingFragment).commit();
    }


    @Override
    public void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.nursing_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbarRssi = (TextView) findViewById(R.id.nursing_toolbar_rssi);
        toolbarRssi.setText(none);
        rssiUnit = getString(R.string.bt_rssi_unit);
        toolbarBluetoothFlag = (ImageView) findViewById(R.id.nursing_toolbar_bluetoothFlag);
    }


    @Override
    public void setMaterialView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);
    }


    @Override
    public void setNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nursing_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);

        navDeviceName = (TextView) navHeader.findViewById(R.id.nursing_device_name);
        navDeviceName.setText(none);

        navDeviceAddress = (TextView) navHeader.findViewById(R.id.nursing_device_address);
        navDeviceAddress.setText(none);

        navUpdate = (TextView) navHeader.findViewById(R.id.nursing_update);
        navUpdate.setText(none);

        navBattery = (ImageView) navHeader.findViewById(R.id.nursing_battery);
    }


    @Override
    public void setMaterialDialog() {

        final int REMOVE_USER = 0, REMOVE_HISTORY = 1, REMOVE_ALL = 2;
        final List<String> removeChoiceList = new ArrayList<>();
        removeChoiceList.add(getString(R.string.nursing_remove_item_user_device));
        removeChoiceList.add(getString(R.string.nursing_remove_item_exercise));
        removeChoiceList.add(getString(R.string.nursing_remove_item_all));
        removeDialog = new MaterialDialog.Builder(this)
                .title(R.string.nursing_remove_title)
                .items(removeChoiceList)
                .itemsCallbackSingleChoice(0, (dialog, itemView, which, text) -> false)
                .positiveText(R.string.nursing_remove_accept).onPositive((dialog, which) -> {

                    switch (dialog.getSelectedIndex()) {
                        case REMOVE_USER:
                            presenter.removeDeviceUserData();
                            break;
                        case REMOVE_HISTORY:
                            presenter.removeHistoryData();
                            break;
                        case REMOVE_ALL:
                            presenter.removeDeviceUserData();
                            presenter.removeHistoryData();
                            break;
                    }
                    dialog.dismiss();
                })
                .negativeText(R.string.nursing_remove_denied).onNegative((dialog, which) -> dialog.dismiss())
                .onAny((dialog, which) -> presenter.registerBluetooth())
                .canceledOnTouchOutside(false).build();


        reconnectDialog = new MaterialDialog.Builder(this)
                .title(R.string.nursing_reconnect_title)
                .content(R.string.nursing_reconnect_content)
                .positiveText(R.string.nursing_reconnect_accept)
                .negativeText(R.string.nursing_reconnect_denied)
                .onNegative((dialog, which) -> presenter.removeHistoryData())
                .onAny((dialog, which) -> {
                    deviceListFragment.dismiss();
                    presenter.registerBluetooth();
                }).build();
    }


    @Override
    public void showDeviceSettingSnackBar() {
        deviceSettingSnackBar = Snackbar.make(coordinatorLayout, R.string.nursing_setting_device, Snackbar.LENGTH_INDEFINITE).setAction(R.string.nursing_setting_device_action, v -> {
            deviceListFragment.show(fragmentManager, getString(R.string.nursing_setting_device_tag));
        });
        deviceSettingSnackBar.show();
    }


    @Override
    public void showUserSettingSnackBar() {
        Snackbar.make(coordinatorLayout, R.string.nursing_setting_user, Snackbar.LENGTH_LONG).setAction(R.string.nursing_setting_user_action, v -> {
            userDataDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_user_tag));
        }).show();
    }


    @Override
    public void showDisconnectDeviceSnackBar() {
        Snackbar.make(coordinatorLayout, R.string.nursing_disconnect_snack, Snackbar.LENGTH_INDEFINITE).setAction(R.string.nursing_ignore, v -> {
            presenter.applicationExit();
        }).show();
    }


    @Override
    public void showDeviceSettingFragment() {
        deviceListFragment.show(fragmentManager, getString(R.string.nursing_setting_device_tag));
    }


    @Override
    public void showUserSettingFragment() {
        userDataDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_user_tag));
    }


    @Override
    public void showGoalSettingFragment() {
        goalDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_goal_tag));
    }


    @Override
    public void onDeviceConnected() {
        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

        dismissSwipeRefresh();
    }


    @Override
    public void onDeviceDisconnected() {
        navDeviceName.setText(none);
        navDeviceAddress.setText(none);
        navUpdate.setText(none);
        setBatteryValue(-1);

        toolbarRssi.setText(none);
        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
    }


    @Override
    public void showSwipeRefresh() {
        nursingFragment.setRefreshing(true);
    }


    @Override
    public void dismissSwipeRefresh() {
        if (nursingFragment.isRefreshing()) {
            nursingFragment.setRefreshing(false);
        }
    }


    @Override
    public void setDeviceValue(DeviceVo deviceValue) {
        navDeviceName.setText(deviceValue.name);
        navDeviceAddress.setText(deviceValue.address);
    }


    @Override
    public void setBatteryValue(int batteryValue) {
        Drawable drawable;
        if (0 <= batteryValue && batteryValue <= 25)
            drawable = ContextCompat.getDrawable(this, R.drawable.ic_battery_alert_white_36dp);
        else if (25 < batteryValue && batteryValue <= 100) {
            drawable = ContextCompat.getDrawable(this, R.drawable.ic_battery_std_white_36dp);
        } else {
            drawable = ContextCompat.getDrawable(this, R.drawable.ic_battery_unknown_white_36dp);
        }

        navBattery.setImageDrawable(drawable);
    }


    @Override
    public void setUpdateTimeValue(String updateValue) {
        navUpdate.setText(updateValue);
    }


    @Override
    public void updateRssiValue(int rssiValue) {
        toolbarRssi.setText(rssiValue + rssiUnit);
    }


    @Override
    public void setValue(List<RealmUserActivityItem> nursingValue) {
        nursingFragment.setValue(nursingValue);
    }


    @Override
    public void setGoalRange(GoalVo goalVo) {
        nursingFragment.setCircleCounterGoalRange(goalVo);
    }


    @Override

    public void setEmptyValue() {
        nursingFragment.setValueEmpty();
    }


    @Override

    public void fail() {
        runOnUiThread(nursingFragment::setTextFail);
    }


    @Override

    public void onDeviceSelect(DeviceVo deviceVo) {
        presenter.saveUserData(deviceVo);

        if (presenter.isHistoryDataAvailable()) {
            reconnectDialog.show();
        } else {
            deviceListFragment.dismiss();
            presenter.registerBluetooth();
        }
    }


    @Override
    public void onDeviceUnSelected() {
        presenter.registerBluetooth();
    }


    @Override
    public void onSaveUserData() {
        userDataDialogFragment.dismiss();
        presenter.overWriteTodayHistory();
    }


    @Override
    public void onSaveGoal(GoalVo goalVo) {
        goalDialogFragment.dismiss();
        nursingFragment.setCircleCounterGoalRange(goalVo);
    }
}
