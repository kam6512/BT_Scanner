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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.profile.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.device.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.SelectDeviceDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.UserDataDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.HistoryFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.DashboardFragment;
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
public class PrimeActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        TabLayout.OnTabSelectedListener,
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener,
        DashboardFragment.OnClickCardListener {

    private static final String TAG = PrimeActivity.class.getSimpleName();

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

    private ListType listType;

    private FragmentManager fragmentManager;

    private UserDataDialogFragment userDataDialogFragment;
    private SelectDeviceDialogFragment selectDeviceDialogFragment;
    private DashboardFragment dashboardFragment;
    private HistoryFragment historyFragment;

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Snackbar deviceSnackBar, userSnackBar;

    private GattManager gattManager;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private SharedPreferences sharedPreferences;
    private Realm realm;

    private final Handler handler = new Handler();
    private final Runnable postSwipeRefresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };


    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_prime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothHelper.checkPermissions(PrimeActivity.this);
        }

        sharedPreferences = getSharedPreferences(PrimeHelper.KEY, MODE_PRIVATE);
        realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());

        fragmentManager = getSupportFragmentManager();

        setFragments();
        setToolbar();
        setMaterialView();
        setViewPager();
        setSnackBar();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        registerBluetooth();
    }


    @DebugLog
    @Override
    public void onPause() {
        super.onPause();
        disconnectDevice();
    }


    @DebugLog
    @Override
    public void onRefresh() {

        if (gattManager != null) {
            if (gattManager.isConnected()) {
                disconnectDevice();
            } else {
                registerBluetooth();
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
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
            case R.id.menu_prime_info_user:
                return true;
            case R.id.menu_prime_info_prime:
                return true;
            case R.id.menu_prime_info_goal:
                return true;
            case R.id.menu_prime_about_dev:
                startActivity(new Intent(PrimeActivity.this, MainActivity.class));
                finish();
                return true;
            case R.id.menu_prime_about_setting:
                removeAllData();
                finish();
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


    private void setMaterialView() {

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.prime_coordinatorLayout);

        drawerLayout = (DrawerLayout) findViewById(R.id.prime_drawer_layout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.prime_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void setViewPager() {
        tabLayout = (TabLayout) findViewById(R.id.prime_tabs);
        DashBoardAdapter dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.prime_viewpager);
        viewPager.setAdapter(dashBoardAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void setFragments() {

        userDataDialogFragment = new UserDataDialogFragment();
        selectDeviceDialogFragment = new SelectDeviceDialogFragment();
        selectDeviceDialogFragment.setCancelable(false);

        dashboardFragment = new DashboardFragment();
        historyFragment = new HistoryFragment();
    }


    private void setSnackBar() {
        deviceSnackBar = Snackbar.make(coordinatorLayout, "Prime 기기 설정이 필요합니다", Snackbar.LENGTH_INDEFINITE).setAction("설정", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDeviceDialogFragment.show(fragmentManager, "Device Select");
            }
        });

        userSnackBar = Snackbar.make(coordinatorLayout, "신체 정보를 입력하시겠습니까?", Snackbar.LENGTH_LONG).setAction("입력", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.beginTransaction().add(userDataDialogFragment, "user info").commit();
            }
        });
    }


    private void registerBluetooth() {
        if (sharedPreferences.getAll().isEmpty()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            deviceSnackBar.show();

            return;
        } else if (sharedPreferences.getString(PrimeHelper.KEY_NAME, getString(R.string.user_name_default)).equals(getString(R.string.user_name_default))) {

            userSnackBar.show();

        }
        Log.i(TAG, "registerBluetooth");
        gattManager = new GattManager(this, gattCallbacks);
        if (gattManager.isBluetoothAvailable()) {
            loadUserData();
            connectDevice();
        } else {
            BluetoothHelper.bluetoothRequest(this);
        }
    }


    @DebugLog
    private void connectDevice() {
        if (!gattManager.isConnected()) {
            try {
                gattManager.connect(deviceAddress);
                listType = ListType.INIT;
                swipeRefreshLayout.post(postSwipeRefresh);
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
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


    @DebugLog
    private void loadUserData() {
        this.userName = sharedPreferences.getString(PrimeHelper.KEY_NAME, getString(R.string.user_name_default));
        this.userAge = sharedPreferences.getString(PrimeHelper.KEY_AGE, getString(R.string.user_age_default));
        this.userHeight = sharedPreferences.getString(PrimeHelper.KEY_HEIGHT, getString(R.string.user_height_default));
        this.userWeight = sharedPreferences.getString(PrimeHelper.KEY_WEIGHT, getString(R.string.user_weight_default));
        this.userStep = sharedPreferences.getString(PrimeHelper.KEY_STEP_STRIDE, getString(R.string.user_step_default));
        this.userGender = sharedPreferences.getString(PrimeHelper.KEY_GENDER, getString(R.string.gender_man));
        this.deviceAddress = sharedPreferences.getString(PrimeHelper.KEY_DEVICE_ADDRESS, "");
        Log.i(TAG,
                userName + "\n" +
                        userAge + "\n" +
                        userHeight + "\n" +
                        userWeight + "\n" +
                        userStep + "\n" +
                        userGender + "\n" +
                        deviceAddress + "\n"
        );
    }


    private void loadPrimeData() {
        historyFragment.addHistory(realm.where(RealmPrimeItem.class).findAll());
    }


    @DebugLog
    private void saveUserData(String name, String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_DEVICE_NAME, name);
        editor.putString(PrimeHelper.KEY_DEVICE_ADDRESS, address);
        editor.apply();
    }


    @DebugLog
    private void savePrimeData(int step, int calorie, int distance) {

        realm.beginTransaction();
        RealmResults<RealmPrimeItem> results = realm.where(RealmPrimeItem.class).findAll();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String today = formatter.format(Calendar.getInstance().getTime());
        String lastDay;

        try {
            lastDay = results.get(results.size() - 1).getCalendar();
            if (lastDay.equals(today)) {
                results.last().setCalendar(today);
                results.last().setStep(step);
                results.last().setCalorie(calorie);
                results.last().setDistance(distance);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            RealmPrimeItem realmPrimeItem = realm.createObject(RealmPrimeItem.class);
            realmPrimeItem.setCalendar(today);
            realmPrimeItem.setStep(step);
            realmPrimeItem.setCalorie(calorie);
            realmPrimeItem.setDistance(distance);
        }

        realm.commitTransaction();
    }


    @DebugLog
    private void removeAllData() {
        realm.beginTransaction();
        realm.clear(RealmPrimeItem.class);
        realm.commitTransaction();
        sharedPreferences.edit().clear().apply();
    }


    @DebugLog
    @Override
    public void onDeviceSelect(final String name, final String address) {
        if (selectDeviceDialogFragment != null) {
            selectDeviceDialogFragment.dismiss();
        }
        saveUserData(name, address);
        registerBluetooth();
    }


    @Override
    public void onStepClick(int value) {
        historyFragment.setCurrentCounter(0);
        tabLayout.getTabAt(1).select();
    }


    @Override
    public void onCalorieClick(int value) {
        historyFragment.setCurrentCounter(1);
        tabLayout.getTabAt(1).select();
    }


    @Override
    public void onDistanceClick(int value) {
        historyFragment.setCurrentCounter(2);
        tabLayout.getTabAt(1).select();
    }


    private final GattCustomCallbacks.GattCallbacks gattCallbacks = new GattCustomCallbacks.GattCallbacks() {

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


        public void onDeviceDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Disconnected");

                    toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                    toolbarRssi.setText("--");
                    if (swipeRefreshLayout.isRefreshing()) {
                        registerBluetooth();
                    }
                }
            });
        }


        public void onServicesFound(final List<BluetoothGattService> services) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothGattService bluetoothGattService = services.get(4); // 0xFFF0
                    characteristicList = bluetoothGattService.getCharacteristics();
                    bluetoothGattCharacteristicForWrite = characteristicList.get(0); // 0xFFF2
                    bluetoothGattCharacteristicForNotify = characteristicList.get(1); // 0xFFF1

                    gattManager.setNotification(bluetoothGattCharacteristicForNotify, true);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onDataNotify(null); // BluetoothGattCharacteristic not use
                        }
                    }, 100); // Notify 콜백 메서드가 없으므로 강제로 기다린다.

                }
            });
        }


        public void onServicesNotFound() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dashboardFragment.setTextFail();
                }
            });
        }


        public void onDataNotify(@Nullable final BluetoothGattCharacteristic ch) {
            try {
                switch (listType) {
                    case INIT: // BluetoothGattCharacteristic is nullable

                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadTime);

                        listType = ListType.READ_TIME;
                        break;

                    case READ_TIME:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dashboardFragment.setTime(PrimeHelper.readTime(ch.getValue()));
                            }
                        });

                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadExerciseData);

                        listType = ListType.READ_STEP_DATA;
                        break;

                    case READ_STEP_DATA:

                        final Bundle bundle = PrimeHelper.readStep(ch.getValue(), userAge, userHeight);
                        final int step = bundle.getInt(PrimeHelper.KEY_STEP);
                        final int kcal = bundle.getInt(PrimeHelper.KEY_KCAL);
                        final int distance = bundle.getInt(PrimeHelper.KEY_DISTANCE);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dashboardFragment.setStepData(step, kcal, distance);

                                savePrimeData(step, kcal, distance);
                                loadPrimeData();
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


        public void onWriteFail() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dashboardFragment.setTextFail();
                }
            });
        }


        public void onRSSIUpdate(final int rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarRssi.setText(rssi + "db");
                }
            });
        }


        public void onRSSIMiss() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarRssi.setText("--");
                }
            });
        }
    };


    private class DashBoardAdapter extends FragmentStatePagerAdapter {

        private final String tabTitles[] = new String[]{"DASHBOARD", "HISTORY"};
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
                    return historyFragment;
                default:
                    return historyFragment;
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