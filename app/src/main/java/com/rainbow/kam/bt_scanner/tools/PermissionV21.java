package com.rainbow.kam.bt_scanner.tools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-20.
 */
public class PermissionV21 {

    public static final boolean isBuildVersionLM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;


    @DebugLog
    @TargetApi(Build.VERSION_CODES.M)
    public static void check(Activity activity) {
        final int REQUEST_ENABLE_BT = 1;
        Log.d("PermissionV21", activity.getLocalClassName());
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
}
