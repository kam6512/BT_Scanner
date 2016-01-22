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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.rainbow.kam.bt_scanner.adapter.prime.HistoryAdapter;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.SelectDeviceDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.UserDataDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.PrimeFragment;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.design.CustomViewPager;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.helper.NestedRecyclerViewManager;
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
        DeviceAdapter.OnDeviceSelectListener {

    private static final String TAG = PrimeActivity.class.getSimpleName();

    private String userName;
    private String userAge;
    private String userHeight;
    private String userWeight;
    private String userStep;
    private String userGender;
    private String deviceAddress;


    private enum GattReadType {
        READ_TIME, READ_STEP_DATA
    }

    private GattReadType gattReadType;

    private FragmentManager fragmentManager;

    private UserDataDialogFragment userDataDialogFragment;
    private SelectDeviceDialogFragment selectDeviceDialogFragment;

    private PrimeFragment[] primeFragment = new PrimeFragment[3];

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView dateTextView, timeTextView;

    private Snackbar deviceSnackBar, userSnackBar;

    private GattManager gattManager;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private SharedPreferences sharedPreferences;
    private Realm realm;
    private HistoryAdapter historyAdapter = new HistoryAdapter();

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
            BluetoothHelper.checkPermissions(PrimeActivity.this);
        }

        sharedPreferences = getSharedPreferences(PrimeHelper.KEY, MODE_PRIVATE);
        realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());

        setFragments();
        setToolbar();
        setMaterialView();
        setViewPager();
        setRecyclerView();
        setCardView();
        setSnackBar();
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


    @DebugLog
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
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
//        item.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menu_prime_setting_device:
                disconnectDevice();
                selectDeviceDialogFragment.show(fragmentManager, "Device Select");
                return true;
            case R.id.menu_prime_setting_user:
                fragmentManager.beginTransaction().add(userDataDialogFragment, "user info").commit();
                return true;
            case R.id.menu_prime_setting_goal:
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
        drawerLayout = (DrawerLayout) findViewById(R.id.prime_drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.prime_coordinatorLayout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.prime_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void setViewPager() {
        PrimeAdapter primeAdapter = new PrimeAdapter(getSupportFragmentManager());

        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.prime_viewpager);
        viewPager.setAdapter(primeAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }


            @Override
            public void onPageSelected(int position) {
                historyAdapter.setCurrentIndex(position);
            }


            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.prime_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void setRecyclerView() {
        RecyclerView historyRecyclerView = (RecyclerView) findViewById(R.id.history_recycler);
        RecyclerView.LayoutManager layoutManager = new NestedRecyclerViewManager(this);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.setNestedScrollingEnabled(false);
        historyRecyclerView.setHasFixedSize(false);
        historyRecyclerView.setAdapter(historyAdapter);
    }


    private void setCardView() {
        CardView datetimeCard = (CardView) findViewById(R.id.prime_card_datetime);
        datetimeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRefreshLayout.post(postSwipeRefresh);
                onRefresh();
            }
        });
        dateTextView = (TextView) findViewById(R.id.prime_date);
        timeTextView = (TextView) findViewById(R.id.prime_time);
    }


    private void setFragments() {
        fragmentManager = getSupportFragmentManager();

        userDataDialogFragment = new UserDataDialogFragment();
        selectDeviceDialogFragment = new SelectDeviceDialogFragment();

        primeFragment[PrimeHelper.INDEX_STEP] = PrimeFragment.newInstance(PrimeHelper.INDEX_STEP);
        primeFragment[PrimeHelper.INDEX_CALORIE] = PrimeFragment.newInstance(PrimeHelper.INDEX_CALORIE);
        primeFragment[PrimeHelper.INDEX_DISTANCE] = PrimeFragment.newInstance(PrimeHelper.INDEX_DISTANCE);
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
        Log.i(TAG, "registerBluetooth");

        if (sharedPreferences.getAll().isEmpty()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (selectDeviceDialogFragment.isVisible()) {
                selectDeviceDialogFragment.dismiss();
            }
            deviceSnackBar.show();
            return;
        } else if (sharedPreferences.getString(PrimeHelper.KEY_NAME, getString(R.string.user_name_default)).equals(getString(R.string.user_name_default))) {
            userSnackBar.show();
        }

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
                gattReadType = GattReadType.READ_TIME;
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
        }
    }


    private void loadUserData() {
        this.userName = sharedPreferences.getString(PrimeHelper.KEY_NAME, getString(R.string.user_name_default));
        this.userAge = sharedPreferences.getString(PrimeHelper.KEY_AGE, getString(R.string.user_age_default));
        this.userHeight = sharedPreferences.getString(PrimeHelper.KEY_HEIGHT, getString(R.string.user_height_default));
        this.userWeight = sharedPreferences.getString(PrimeHelper.KEY_WEIGHT, getString(R.string.user_weight_default));
        this.userStep = sharedPreferences.getString(PrimeHelper.KEY_STEP_STRIDE, getString(R.string.user_step_default));
        this.userGender = sharedPreferences.getString(PrimeHelper.KEY_GENDER, getString(R.string.gender_man));
        this.deviceAddress = sharedPreferences.getString(PrimeHelper.KEY_DEVICE_ADDRESS, null);

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

        SimpleDateFormat formatter = new SimpleDateFormat("MM월 dd일");
        String today = formatter.format(Calendar.getInstance().getTime());

        if (results.isEmpty()) {
            RealmPrimeItem realmPrimeItem = realm.createObject(RealmPrimeItem.class);
            realmPrimeItem.setCalendar(today);
            realmPrimeItem.setStep(step);
            realmPrimeItem.setCalorie(calorie);
            realmPrimeItem.setDistance(distance);
        } else {
            String lastDay = results.get(results.size() - 1).getCalendar();
            if (lastDay.equals(today)) {
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


    private void setPrimeTotal() {
        RealmResults<RealmPrimeItem> results = realm.where(RealmPrimeItem.class).findAll();
        historyAdapter.add(results);

        int totalStep = 0, totalCalorie = 0, totalDistance = 0;
        for (RealmPrimeItem realmPrimeItem : results) {
            totalStep += realmPrimeItem.getStep();
            totalCalorie += realmPrimeItem.getCalorie();
            totalDistance += realmPrimeItem.getDistance();
        }
        primeFragment[PrimeHelper.INDEX_STEP].setCircleTotalValue(totalStep);
        primeFragment[PrimeHelper.INDEX_CALORIE].setCircleTotalValue(totalCalorie);
        primeFragment[PrimeHelper.INDEX_DISTANCE].setCircleTotalValue(totalDistance);
    }


    private void setFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                primeFragment[PrimeHelper.INDEX_STEP].setTextFail();
                primeFragment[PrimeHelper.INDEX_CALORIE].setTextFail();
                primeFragment[PrimeHelper.INDEX_DISTANCE].setTextFail();
//                    for (PrimeFragment aPrimeFragment : primeFragment) {
//                        aPrimeFragment.setTextFail();
//                    }
                String accessDenial = getString(R.string.prime_access_denial);
                dateTextView.setText(accessDenial);
                timeTextView.setText(accessDenial);
            }
        });
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
    public void onDeviceUnSelected() {
        registerBluetooth();
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
                            gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadTime);
                        }
                    }, 100); // Notify 콜백 메서드가 없으므로 강제로 기다린다
                }
            });
        }


        public void onServicesNotFound() {
            setFail();
        }


        public void onDataNotify(@Nullable final BluetoothGattCharacteristic ch) {
            try {
                switch (gattReadType) {
                    case READ_TIME:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar calendar = PrimeHelper.readTime(ch.getValue());
                                SimpleDateFormat date = new SimpleDateFormat("yy년 MM 월 dd일");
                                SimpleDateFormat time = new SimpleDateFormat("HH시 mm분");

                                dateTextView.setText(date.format(calendar.getTime()));
                                timeTextView.setText(time.format(calendar.getTime()));
                            }
                        });

                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadExerciseData);

                        gattReadType = GattReadType.READ_STEP_DATA;
                        break;

                    case READ_STEP_DATA:

                        final Bundle bundle = PrimeHelper.readValue(ch.getValue(), userAge, userHeight);
                        final int step = bundle.getInt(PrimeHelper.KEY_STEP);
                        final int calorie = bundle.getInt(PrimeHelper.KEY_CALORIE);
                        final int distance = bundle.getInt(PrimeHelper.KEY_DISTANCE);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                primeFragment[PrimeHelper.INDEX_STEP].setTextValue(step);
                                primeFragment[PrimeHelper.INDEX_CALORIE].setTextValue(calorie);
                                primeFragment[PrimeHelper.INDEX_DISTANCE].setTextValue(distance);

                                savePrimeData(step, calorie, distance);
                                setPrimeTotal();

                            }
                        });
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                setFail();
            }
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


        public void onRSSIMiss() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarRssi.setText("--");
                }
            });
        }
    };


    private class PrimeAdapter extends FragmentStatePagerAdapter {

        private final String tabTitles[] = new String[]{"도보량", "소모열량", "활동거리"};
        final int PAGE_COUNT = tabTitles.length;


        public PrimeAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return primeFragment[position];
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