package com.rainbow.kam.bt_scanner.tools.helper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;

/**
 * Created by kam6512 on 2015-11-20.
 */
public class BluetoothHelper {

    public static final boolean IS_BUILD_VERSION_LM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final int SCAN_PERIOD = 5000;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int RESULT_OK = -1;

    public static final String KEY_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";

    public static final String BOND_NONE = "NOT BONDED";
    private static final String BOND_BONDING = "BONDING...";
    private static final String BOND_BONDED = "BONDED";

    public static final SparseArray<String> BOND_LIST = new SparseArray<>();

    public static final String DEVICE_TYPE_UNKNOWN = "UNKNOWN";
    private static final String DEVICE_TYPE_CLASSIC = "CLASSIC BLUETOOTH";
    private static final String DEVICE_TYPE_LE = "BLUETOOTH LOW ENERGY";
    private static final String DEVICE_TYPE_DUAL = "DUAL";

    public static final SparseArray<String> TYPE_LIST = new SparseArray<>();


    @TargetApi(Build.VERSION_CODES.M)
    public static void checkPermissions(Activity activity) {
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_ENABLE_BT);
        }
    }


    public static void onRequestPermissionsResult(int requestCode,
                                                  @NonNull int[] grantResults, Activity activity) {
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
            if (grantResults.length != 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity, R.string.permission_thanks, Toast.LENGTH_SHORT).show();
                } else {
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivityForResult(myAppSettings, 0);

                    Toast.makeText(activity, R.string.permission_request, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
            }
        } else {
            Toast.makeText(activity, R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }
    }


    public static void bluetoothRequest(Activity activity) {//블루투스 가동여부
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    public static void onActivityResult(int requestCode, int resultCode, Activity activity) {
        switch (requestCode) {
            case BluetoothHelper.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(activity, R.string.bt_on, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
                break;
        }
    }


    static {
        BOND_LIST.put(10, BOND_NONE);
        BOND_LIST.put(11, BOND_BONDING);
        BOND_LIST.put(12, BOND_BONDED);

        TYPE_LIST.put(0, DEVICE_TYPE_UNKNOWN);
        TYPE_LIST.put(1, DEVICE_TYPE_CLASSIC);
        TYPE_LIST.put(2, DEVICE_TYPE_LE);
        TYPE_LIST.put(3, DEVICE_TYPE_DUAL);
    }
}
