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
import android.util.Log;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-20.
 */
public class BluetoothHelper {

    public static final boolean IS_BUILD_VERSION_LM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final long SCAN_PERIOD = 5000;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int RESULT_OK = -1;

    public static final String KEY_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";

    public static final String BOND_NONE = "NOT BONDED";
    public static final String BOND_BONDING = "BONDING...";
    public static final String BOND_BONDED = "BONDED";

    public static final String DEVICE_TYPE_UNKNOWN = "UNKNOWN";
    public static final String DEVICE_TYPE_CLASSIC = "CLASSIC BLUETOOTH";
    public static final String DEVICE_TYPE_LE = "BLUETOOTH LOW ENERGY";
    public static final String DEVICE_TYPE_DUAL = "DUAL";


    @DebugLog
    @TargetApi(Build.VERSION_CODES.M)
    public static void CHECK_PERMISSIONS(Activity activity) {
        Log.d("BluetoothHelper", activity.getLocalClassName());
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(activity, "ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
            }
            activity.requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_ENABLE_BT);
        }
    }


    public static void BLUETOOTH_REQUEST(Activity activity) {//블루투스 가동여부

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    public static void ON_ACTIVITY_RESULT(int requestCode, int resultCode, Activity activity) {

        switch (requestCode) {
            case BluetoothHelper.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                    Toast.makeText(activity, R.string.bt_on, Toast.LENGTH_SHORT).show();
                } else {
                    //블루투스 에러
                    Toast.makeText(activity, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
                break;
        }
    }


    public static void ON_REQUEST_PERMISSIONS_RESULT(int requestCode,
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
/*
Process: com.rainbow.kam.bt_scanner, PID: 2344
java.lang.RuntimeException: Unable to start activity ComponentInfo{com.rainbow.kam.bt_scanner/com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity}: java.lang.ArrayIndexOutOfBoundsException: length=0; index=0
at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2416)
at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2476)
at android.app.ActivityThread.-wrap11(ActivityThread.java)
at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1344)
at android.os.Handler.dispatchMessage(Handler.java:102)
at android.os.Looper.loop(Looper.java:148)
at android.app.ActivityThread.main(ActivityThread.java:5417)
at java.lang.reflect.Method.invoke(Native Method)
at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:726)
at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:616)
Caused by: java.lang.ArrayIndexOutOfBoundsException: length=0; index=0
at com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper.ON_REQUEST_PERMISSIONS_RESULT(BluetoothHelper.java:86)
at com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity.onRequestPermissionsResult(PrimeActivity.java:251)
at android.app.Activity.requestPermissions(Activity.java:3823)
at com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper.CHECK_PERMISSIONS_aroundBody0(BluetoothHelper.java:51)
at com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper$AjcClosure1.run(BluetoothHelper.java:1)
at org.aspectj.runtime.reflect.JoinPointImpl.proceed(JoinPointImpl.java:149)
at hugo.weaving.internal.Hugo.logAndExecute(Hugo.java:39)
at com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper.CHECK_PERMISSIONS(BluetoothHelper.java:45)
at com.rainbow.kam.bt_scanner.fragment.prime.menu.SelectDeviceDialogFragment.onCreateView_aroundBody0(SelectDeviceDialogFragment.java:97)
at com.rainbow.kam.bt_scanner.fragment.prime.menu.SelectDeviceDialogFragment$AjcClosure1.run(SelectDeviceDialogFragment.java:1)
at org.aspectj.runtime.reflect.JoinPointImpl.proceed(JoinPointImpl.java:149)
at hugo.weaving.internal.Hugo.logAndExecute(Hugo.java:39)
at com.rainbow.kam.bt_scanner.fragment.prime.menu.SelectDeviceDialogFragment.onCreateView(SelectDeviceDialogFragment.java:94)
at android.support.v4.app.Fragment.performCreateView(Fragment.java:1962)
at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1067)
at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1248)
at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1230)
at android.support.v4.app.FragmentManagerImpl.dispatchActivityCreated(FragmentManager.java:2042)
at android.support.v4.app.FragmentController.dispatchActivityCreated(FragmentController.java:165)
at android.support.v4.app.FragmentActivity.onStart(FragmentActivity.java:543)
at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1237)
at android.app.Activity.performStart(Activity.java:6268)
at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2379)
at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2476) 
at android.app.ActivityThread.-wrap11(ActivityThread.java) 
at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1344) 
at android.os.Handler.dispatchMessage(Handler.java:102) 
at android.os.Looper.loop(Looper.java:148) 
at android.app.ActivityThread.main(ActivityThread.java:5417) 
at java.lang.reflect.Method.invoke(Native Method) 
*/
}
