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
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
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
import com.rainbow.kam.bt_scanner.fragment.prime.menu.GoalDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.SelectDeviceDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.UserDataDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.PrimeFragment;
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
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener,
        GoalDialogFragment.OnSaveGoalListener {

    private final String TAG = getClass().getSimpleName();

    private enum GattReadType {
        READ_TIME, READ_STEP_DATA
    }

    private GattReadType gattReadType;

    private FragmentManager fragmentManager;

    private UserDataDialogFragment userDataDialogFragment;
    private SelectDeviceDialogFragment selectDeviceDialogFragment;
    private GoalDialogFragment goalDialogFragment;

    private PrimeFragment primeFragment;

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GattManager gattManager;

    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private String userAge, userHeight, deviceAddress;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Realm realm;


    private final Handler handler = new Handler();
    private final Runnable postSwipeRefresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_prime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothHelper.checkPermissions(this);
        }

        initDB();
        setFragments();
        setToolbar();
        setMaterialView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerBluetooth();

    }


    @Override
    public void onPause() {
        super.onPause();
        disconnectDevice();
    }


    @Override
    public void onRefresh() {
        if (gattManager != null && gattManager.isConnected()) {
            disconnectDevice();
        } else {
            registerBluetooth();
        }
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
            case R.id.menu_prime_setting_device:
                disconnectDevice();
                selectDeviceDialogFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
                return true;
            case R.id.menu_prime_setting_user:
                userDataDialogFragment.show(fragmentManager, getString(R.string.prime_setting_user_tag));
                return true;
            case R.id.menu_prime_setting_goal:
                goalDialogFragment.show(fragmentManager, getString(R.string.prime_setting_goal_tag));
                return true;
            case R.id.menu_prime_about_dev:
                startActivity(new Intent(PrimeActivity.this, MainActivity.class));
                return true;
            case R.id.menu_prime_about_setting:
                removeAllData();
                registerBluetooth();
                return true;
            case R.id.menu_prime_about_about:
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
            super.onBackPressed();
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


    private void initDB() {
        sharedPreferences = getSharedPreferences(PrimeHelper.KEY, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());
    }


    private void setFragments() {
        fragmentManager = getSupportFragmentManager();

        primeFragment = new PrimeFragment();
        fragmentManager.beginTransaction().replace(R.id.prime_fragment_frame, primeFragment).commit();

        userDataDialogFragment = new UserDataDialogFragment();
        selectDeviceDialogFragment = new SelectDeviceDialogFragment();
        goalDialogFragment = new GoalDialogFragment();
    }


    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.prime_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbarRssi = (TextView) findViewById(R.id.prime_toolbar_rssi);
        toolbarBluetoothFlag = (ImageView) findViewById(R.id.prime_toolbar_bluetoothFlag);
    }


    private void setMaterialView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.prime_drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.prime_coordinatorLayout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.prime_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void registerBluetooth() {
        if (checkDataAvailable()) {
            gattManager = new GattManager(this, gattCallbacks);
            if (gattManager.isBluetoothAvailable()) {
                loadUserData();
                connectDevice();
            } else {
                BluetoothHelper.bluetoothRequest(this);
            }
        }
    }


    private boolean checkDataAvailable() {
        if (sharedPreferences.getAll().isEmpty()) {
            swipeRefreshLayout.setRefreshing(false);
            showDeviceSnackBar();
            return false;
        } else if (!sharedPreferences.contains(PrimeHelper.KEY_NAME)) {
            showUserSettingSnackBar();
        }
        return true;
    }


    private void showDeviceSnackBar() {
        Snackbar.make(coordinatorLayout, getString(R.string.prime_setting_device), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.prime_setting_device_action), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDeviceDialogFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
            }
        }).show();
    }


    private void showUserSettingSnackBar() {
        Snackbar.make(coordinatorLayout, getString(R.string.prime_setting_user), Snackbar.LENGTH_LONG).setAction(getString(R.string.prime_setting_user_action), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDataDialogFragment.show(fragmentManager, getString(R.string.prime_setting_user_tag));
            }
        }).show();
    }


    @DebugLog
    private void connectDevice() {
        try {
            gattManager.connect(deviceAddress);
            gattReadType = GattReadType.READ_TIME;
            swipeRefreshLayout.post(postSwipeRefresh);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, getString(R.string.bt_fail), Toast.LENGTH_SHORT).show();
        }
    }


    @DebugLog
    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        }
    }


    private void loadUserData() {
        this.userAge = sharedPreferences.getString(PrimeHelper.KEY_AGE, getString(R.string.user_age_default));
        this.userHeight = sharedPreferences.getString(PrimeHelper.KEY_HEIGHT, getString(R.string.user_height_default));
        this.deviceAddress = sharedPreferences.getString(PrimeHelper.KEY_DEVICE_ADDRESS, null);
    }


    @DebugLog
    private void saveDeviceData(String name, String address) {
        editor.putString(PrimeHelper.KEY_DEVICE_NAME, name);
        editor.putString(PrimeHelper.KEY_DEVICE_ADDRESS, address);
        editor.apply();
    }


    @DebugLog
    private void savePrimeData(RealmPrimeItem realmPrimeItem) {
        int step = realmPrimeItem.getStep();
        int calorie = realmPrimeItem.getCalorie();
        int distance = realmPrimeItem.getDistance();

        SimpleDateFormat formatter = new SimpleDateFormat("MM월 dd일");
        String today = formatter.format(Calendar.getInstance().getTime());

        RealmResults<RealmPrimeItem> results = realm.where(RealmPrimeItem.class).findAll();

        realm.beginTransaction();

        if (results.isEmpty()) {
            RealmPrimeItem newRealmPrimeItem = realm.createObject(RealmPrimeItem.class);
            newRealmPrimeItem.setCalendar(today);
            newRealmPrimeItem.setStep(step);
            newRealmPrimeItem.setCalorie(calorie);
            newRealmPrimeItem.setDistance(distance);
        } else {
            String lastDay = results.get(results.size() - 1).getCalendar();
            if (lastDay.equals(today)) {
                RealmPrimeItem lastRealmPrimeItem = results.last();
                lastRealmPrimeItem.setCalendar(today);
                lastRealmPrimeItem.setStep(step);
                lastRealmPrimeItem.setCalorie(calorie);
                lastRealmPrimeItem.setDistance(distance);
            } else {
                RealmPrimeItem newRealmPrimeItem = realm.createObject(RealmPrimeItem.class);
                newRealmPrimeItem.setCalendar(today);
                newRealmPrimeItem.setStep(step);
                newRealmPrimeItem.setCalorie(calorie);
                newRealmPrimeItem.setDistance(distance);
            }
        }

        realm.commitTransaction();

        RealmPrimeItem.setTotalValue(results);
    }


    @DebugLog
    private void removeAllData() {
//        realm.beginTransaction();
//        realm.clear(RealmPrimeItem.class);
//        realm.commitTransaction();
        sharedPreferences.edit().clear().apply();
    }


    private void setFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                primeFragment.setTextFail();

            }
        });
    }


    @DebugLog
    @Override
    public void onDeviceSelect(final String name, final String address) {
        selectDeviceDialogFragment.dismiss();
        saveDeviceData(name, address);
        registerBluetooth();
    }


    @DebugLog
    @Override
    public void onDeviceUnSelected() {
        registerBluetooth();
    }


    @Override
    public void onSaveGoal() {
        goalDialogFragment.dismiss();
        primeFragment.setCircleCounterGoalRange();
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
            List<BluetoothGattCharacteristic> characteristicList = services.get(4).getCharacteristics();
            gattManager.setNotification(characteristicList.get(1), true);
            bluetoothGattCharacteristicForWrite = characteristicList.get(0); // 0xFFF2


            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadTime);
                }
            }, 100); // Notify 콜백 메서드가 없으므로 강제로 기다린다
        }


        public void onServicesNotFound() {
            setFail();
        }


        public void onDataNotify(@Nullable final BluetoothGattCharacteristic ch) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (gattReadType) {
                            case READ_TIME:
                                primeFragment.setUpdateValue(PrimeHelper.readTime(ch.getValue()));

                                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadExerciseData);
                                gattReadType = GattReadType.READ_STEP_DATA;

                                break;

                            case READ_STEP_DATA:

                                savePrimeData(PrimeHelper.readValue(ch.getValue(), userAge, userHeight));
                                primeFragment.setRealmPrimeValue(realm.where(RealmPrimeItem.class).findAll());

                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataNotify : " + e.getMessage());
                        setFail();
                    }
                }
            });
        }


        public void onWriteFail() {
            setFail();
        }


        public void onRSSIUpdate(final int rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarRssi.setText(rssi + "db");
                }
            });
        }
    };
}