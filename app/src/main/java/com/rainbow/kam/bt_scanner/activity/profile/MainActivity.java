package com.rainbow.kam.bt_scanner.activity.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.nursing.NursingActivity;
import com.rainbow.kam.bt_scanner.adapter.device.DeviceAdapter;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.fragment.device.DeviceListFragment;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener {


    public static final String KEY_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";

    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);

        if (BluetoothHelper.IS_BUILD_VERSION_LM) {
            BluetoothHelper.requestBluetoothPermission(this);
        }

        setToolbar();
        setMaterialDesignView();
        setOtherView();
        setDeviceListFragment();
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(new Intent(this, NursingActivity.class));
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onRequestEnableResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    private void setMaterialDesignView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }


    private void setOtherView() {
//        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
//        fabSearch.setOnClickListener(this);
    }


    private void setDeviceListFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DeviceListFragment deviceListFragment = new DeviceListFragment();
        fragmentManager.beginTransaction().replace(R.id.profile_fragment_frame, deviceListFragment).commit();

    }


    @Override
    public void onDeviceSelect(DeviceVo deviceVo) {
        Intent intent = new Intent(this, DeviceProfileActivity.class);
        intent.putExtra(KEY_DEVICE_NAME, deviceVo.name);
        intent.putExtra(KEY_DEVICE_ADDRESS, deviceVo.address);
        startActivity(intent);
    }


    @Override
    public void onDeviceUnSelected() {
        finish();
    }

}

