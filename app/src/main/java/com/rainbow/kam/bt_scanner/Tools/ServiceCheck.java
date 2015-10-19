package com.rainbow.kam.bt_scanner.Tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by kam6376 on 2015-07-17.
 */
public class ServiceCheck {
    public boolean BtnSVC_Run(Context context, String serviceName) {

        //서비스 매니저 초기화
        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

        //현재 서비스 목록을 돌면서 일치하는 서비스 클리스 네임을 비교해 리턴
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {

                return true;
            }
        }

        return false;
    }
}