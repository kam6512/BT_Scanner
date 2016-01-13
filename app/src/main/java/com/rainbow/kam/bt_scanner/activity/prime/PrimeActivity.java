package com.rainbow.kam.bt_scanner.activity.prime;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.profile.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.prime.setting.LogoFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.setting.SelectDeviceDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.setting.SettingFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.CalorieFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.DashboardFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.DistanceFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.SampleFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.StepFragment;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class PrimeActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, DeviceAdapter.OnDeviceSelectListener, SettingFragment.OnSettingListener, GattCustomCallbacks {

    private static final String TAG = PrimeActivity.class.getSimpleName();

    private final long logoDelay = 2000;

    private String userName;
    private String userAge;
    private String userHeight;
    private String userWeight;
    private String userStep;
    private String userGender;

    private String deviceAddress;


    private enum ListType {
        INIT, READ_TIME, READ_STEP_DATA, ETC, FINISH
    }

    private ListType listType = ListType.INIT;

    private FragmentManager fragmentManager;

    private LogoFragment logoFragment;
    private SettingFragment settingFragment;
    private SelectDeviceDialogFragment selectDeviceDialogFragment;

    private DashboardFragment dashboardFragment;
    private StepFragment stepFragment;
    private CalorieFragment calorieFragment;
    private DistanceFragment distanceFragment;
    private SampleFragment sampleFragment;

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager viewPager;

    private GattManager gattManager;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private SharedPreferences sharedPreferences;
    private Realm realm;

    private MaterialDialog materialDialog;

    private Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (sharedPreferences.getAll().isEmpty()) {
                setSettingFragments();
                setSettingDialog();
                fragmentManager.beginTransaction().replace(R.id.prime_start_frame, settingFragment)
                        .commitAllowingStateLoss();
            } else {
                fragmentManager.beginTransaction().remove(logoFragment)
                        .commitAllowingStateLoss();
                registerBluetooth();
            }
        }
    };


    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_prime);

        sharedPreferences = getSharedPreferences(PrimeHelper.KEY, MODE_PRIVATE);
        realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothHelper.check(PrimeActivity.this);
        }

        logoFragment = new LogoFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.prime_start_frame, logoFragment).commitAllowingStateLoss();
        handler.postDelayed(runnable, logoDelay);

        setPagerFragments();
        setToolbar();
        setMaterialNavigationView();
        setViewPager();
    }


    @DebugLog
    @Override
    public void onPause() {
        super.onPause();
        disconnectDevice();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }


    @DebugLog
    @Override
    public void onRefresh() {
        if (gattManager.isConnected()) {
            disconnectDevice();
        } else {
            registerBluetooth();
        }
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition(), true);
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }


    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menu_prime_dashboard:
                viewPager.setCurrentItem(0, true);
                return true;
            case R.id.menu_prime_dashboard_step:
                viewPager.setCurrentItem(1, true);
                return true;
            case R.id.menu_prime_dashboard_calorie:
                viewPager.setCurrentItem(2, true);
                return true;
            case R.id.menu_prime_dashboard_distance:
                viewPager.setCurrentItem(3, true);
                return true;
            case R.id.menu_prime_dashboard_sleep:
                viewPager.setCurrentItem(4, true);
                return true;
            case R.id.menu_prime_info_user:
                return true;
            case R.id.menu_prime_info_prime:
                return true;
            case R.id.menu_prime_info_goal:
                return true;
            case R.id.menu_prime_about_dev:
                finish();
                startActivity(new Intent(PrimeActivity.this, MainActivity.class));
                return true;
            case R.id.menu_prime_about_setting:
                realm.beginTransaction();
                realm.clear(RealmPrimeItem.class);
                realm.commitTransaction();
                sharedPreferences.edit().clear().apply();
                return true;
            case R.id.menu_prime_about_about:
                return true;
            default:
                return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onActivityResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    private void setSettingFragments() {
        settingFragment = new SettingFragment();
        selectDeviceDialogFragment = new SelectDeviceDialogFragment();
    }


    private void setSettingDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .positiveText(R.string.dialog_accept).negativeText(R.string.dialog_fix)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        selectDeviceDialogFragment.show(getSupportFragmentManager(), "DeviceDialog");
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).build();
    }


    @DebugLog
    public void onSettingSkip() {
        userName = getString(R.string.user_name_default);
        userAge = getString(R.string.user_age_default);
        userHeight = getString(R.string.user_height_default);
        userWeight = getString(R.string.user_weight_default);
        userStep = getString(R.string.user_step_default);
        userGender = getString(R.string.gender_man);

        materialDialog.setTitle(R.string.dialog_skip);
        materialDialog.setContent(R.string.dialog_skip_warning);
        materialDialog.show();
    }


    @Override
    public void onSettingAccept(String userName, String userAge, String userHeight, String userWeight, String userStep, String userGender) {
        String dialogContent = "이름 : " + userName +
                "\n성별 : " + userGender +
                "\n나이 : " + userAge +
                "\n키 : " + userHeight +
                "\n몸무게 : " + userWeight +
                "\n걸음너비 : " + userStep;

        this.userName = userName;
        this.userAge = userAge;
        this.userHeight = userHeight;
        this.userWeight = userWeight;
        this.userStep = userStep;
        this.userGender = userGender;

        materialDialog.setTitle(R.string.dialog_accept_ok);
        materialDialog.setContent(dialogContent);
        materialDialog.show();
    }


    @DebugLog
    @Override
    public void onSettingDeviceSelect(final String name, final String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_NAME, userName);
        editor.putString(PrimeHelper.KEY_AGE, userAge);
        editor.putString(PrimeHelper.KEY_HEIGHT, userHeight);
        editor.putString(PrimeHelper.KEY_WEIGHT, userWeight);
        editor.putString(PrimeHelper.KEY_STEP_STRIDE, userStep);
        editor.putString(PrimeHelper.KEY_GENDER, userGender);
        editor.putString(PrimeHelper.KEY_DEVICE_NAME, name);
        editor.putString(PrimeHelper.KEY_DEVICE_ADDRESS, address);
        editor.apply();

        selectDeviceDialogFragment.dismiss();
        getSupportFragmentManager().beginTransaction()
                .remove(settingFragment)
                .commitAllowingStateLoss();
        registerBluetooth();
    }


    private void setPagerFragments() {
        dashboardFragment = new DashboardFragment();
        stepFragment = new StepFragment();
        calorieFragment = new CalorieFragment();
        distanceFragment = new DistanceFragment();
        sampleFragment = new SampleFragment();
    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.prime_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbarRssi = (TextView) findViewById(R.id.prime_toolbar_rssi);
        toolbarBluetoothFlag = (ImageView) findViewById(R.id.prime_toolbar_bluetoothFlag);
        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_white_24dp);
    }


    private void setMaterialNavigationView() {

        drawerLayout = (DrawerLayout) findViewById(R.id.prime_drawer_layout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.prime_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void setViewPager() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.prime_tabs);
        DashBoardAdapter dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.prime_viewpager);
        viewPager.setAdapter(dashBoardAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setupWithViewPager(viewPager);
    }


    @DebugLog
    private void queryUserInfo() {
        try {
            userAge = sharedPreferences.getString(PrimeHelper.KEY_AGE, getString(R.string.user_age_default));
            userHeight = sharedPreferences.getString(PrimeHelper.KEY_HEIGHT, getString(R.string.user_height_default));
            deviceAddress = sharedPreferences.getString(PrimeHelper.KEY_DEVICE_ADDRESS, "");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @DebugLog
    private void saveRealmData(int step, int calorie, int distance) {

        realm.beginTransaction();
        RealmResults<RealmPrimeItem> results = realm.where(RealmPrimeItem.class).findAll();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String today = formatter.format(Calendar.getInstance().getTime());
        String lastDay;

        try {
            lastDay = results.get(results.size() - 1).getCalendar();
        } catch (Exception e) {
            lastDay = null;
        }
        if (lastDay != null && lastDay.equals(today)) {
            results.last().setCalendar(today);
            results.last().setStep(step);
            results.last().setCalorie(calorie);
            results.last().setDistance(distance);
        } else {
            RealmPrimeItem realmPrimeItem = realm.createObject(RealmPrimeItem.class);
            realmPrimeItem.setCalendar(today);
            realmPrimeItem.setStep(step);
            realmPrimeItem.setCalorie(calorie);
            realmPrimeItem.setDistance(distance);
        }
        Log.e("RealmPrimeItem", "results = " + "\n" +
                results.last().getStep() + "\n" +
                results.last().getCalorie() + "\n" +
                results.last().getDistance() + "\n" +
                results.last().getCalendar() + "\n"
        );

        realm.commitTransaction();
    }


    @DebugLog
    private void registerBluetooth() {
        gattManager = new GattManager(this, this);
        if (gattManager.isBluetoothAvailable()) {
            queryUserInfo();
            connectDevice();
        } else {
            BluetoothHelper.initBluetoothOn(this);
        }
    }


    @DebugLog
    private void connectDevice() {
        if (!gattManager.isConnected()) {
            try {
                gattManager.connect(deviceAddress);
                listType = ListType.INIT;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                finish();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @DebugLog
    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
            listType = ListType.FINISH;
        }
    }


    @Override
    public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Connected");

                toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }


    @Override
    public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Disconnected");

                toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                toolbarRssi.setText("--");
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });
    }


    @DebugLog
    @Override
    public void onServicesFound(final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BluetoothGattService bluetoothGattService = services.get(4); // 0xFFF0
                characteristicList = bluetoothGattService.getCharacteristics();
                bluetoothGattCharacteristicForWrite = characteristicList.get(0); // 0xFFF2
                bluetoothGattCharacteristicForNotify = characteristicList.get(1); // 0xFFF1

                gattManager.setNotification(bluetoothGattCharacteristicForNotify, true);
                onDataNotify(null);
            }
        });
    }


    @DebugLog
    @Override
    public void onServicesNotFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @DebugLog
    @Override
    public void onReadSuccess(BluetoothGattCharacteristic ch) {
        // Not use in this Activity
    }


    @DebugLog
    @Override
    public void onReadFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @DebugLog
    @Override
    public void onDataNotify(@Nullable final BluetoothGattCharacteristic ch) {
        try {
            switch (listType) {
                case INIT:
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_DEVICE_TIME());
                        }
                    }, 100); // Notify 콜백 메서드가 없으므로 강제로 기다린다.

                    listType = ListType.READ_TIME;
                    break;

                case READ_TIME:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dashboardFragment.setTime(PrimeHelper.readTime(ch.getValue()));
                        }
                    });


                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_STEP_DATA());

                    listType = ListType.READ_STEP_DATA;
                    break;

                case READ_STEP_DATA:

                    Bundle bundle = PrimeHelper.readStep(ch.getValue(), userAge, userHeight);
                    final int step = bundle.getInt(PrimeHelper.KEY_STEP);
                    final int kcal = bundle.getInt(PrimeHelper.KEY_KCAL);
                    final int distance = bundle.getInt(PrimeHelper.KEY_DISTANCE);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            saveRealmData(step, kcal, distance);
                            dashboardFragment.setStepData(step, kcal, distance);
                            stepFragment.setStep(step);
                            calorieFragment.setCalorie(kcal);
                            distanceFragment.setDist(distance);
                            sampleFragment.setSample(distance);
                        }
                    });

                    listType = ListType.ETC;
                    break;

                case ETC:
                    listType = ListType.FINISH;
                case FINISH:
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            onReadFail();
        }
    }


    @DebugLog
    @Override
    public void onWriteSuccess() {
    }


    @DebugLog
    @Override
    public void onWriteFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @DebugLog
    @Override
    public void onRSSIUpdate(final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarRssi.setText(rssi + "db");
            }
        });
    }


    @DebugLog
    @Override
    public void onRSSIMiss() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarRssi.setText("--");
            }
        });
    }


    private class DashBoardAdapter extends FragmentStatePagerAdapter {

        private final String tabTitles[] = new String[]{"DASHBOARD", "STEP", "CALORIE", "DISTANCE", "ETC"};
        final int PAGE_COUNT = tabTitles.length;


        public DashBoardAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return dashboardFragment;
                case 1:
                    return stepFragment;
                case 2:
                    return calorieFragment;
                case 3:
                    return distanceFragment;
                default:
                    return sampleFragment;
            }
        }


        @Override
        public int getCount() {
            return PAGE_COUNT;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}