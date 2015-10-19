package com.rainbow.kam.bt_scanner.Tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by kam6376 on 2015-07-17.
 */
public class ServiceCheck {
    public boolean BtnSVC_Run(Context context, String serviceName) {

        //���� �Ŵ��� �ʱ�ȭ
        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

        //���� ���� ����� ���鼭 ��ġ�ϴ� ���� Ŭ���� ������ ���� ����
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {

                return true;
            }
        }

        return false;
    }
}