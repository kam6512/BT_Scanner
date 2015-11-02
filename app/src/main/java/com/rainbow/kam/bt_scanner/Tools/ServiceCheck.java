package com.rainbow.kam.bt_scanner.Tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by kam6512 on 2015-07-17.
 */
public class ServiceCheck {
    public boolean BtnSVC_Run(Context context, String serviceName) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {

                return true;
            }
        }

        return false;
    }
}