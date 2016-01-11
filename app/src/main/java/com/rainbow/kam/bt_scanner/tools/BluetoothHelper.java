package com.rainbow.kam.bt_scanner.tools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-20.
 */
public class BluetoothHelper {

    public static final boolean isBuildVersionLM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int RESULT_OK = -1;


    @DebugLog
    @TargetApi(Build.VERSION_CODES.M)
    public static void check(Context context) {
        Activity activity = (Activity) context;
        final int REQUEST_ENABLE_BT = 1;
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


    public static void initBluetoothOn(Context context) {//블루투스 가동여부
        Activity activity = (Activity) context;
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    public static void onActivityResult(int requestCode, int resultCode, Context context) {
        Activity activity = (Activity) context;
        switch (requestCode) {
            case BluetoothHelper.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                    Snackbar.make(activity.getWindow().getDecorView(), R.string.bt_on, Snackbar.LENGTH_SHORT).show();
                } else {
                    //블루투스 에러
                    Toast.makeText(activity, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
                break;
        }
    }


    public static void onRequestPermissionsResult(int requestCode,
                                                  @NonNull int[] grantResults, Context context) {
        Activity activity = (Activity) context;
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(activity.getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();
            } else {

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivityForResult(myAppSettings, 0);

                Toast.makeText(activity, R.string.permission_request, Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        } else {
            Toast.makeText(activity, R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }
    }

}
