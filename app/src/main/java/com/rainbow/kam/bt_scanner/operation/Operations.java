package com.rainbow.kam.bt_scanner.operation;

/**
 * Created by kam6512 on 2016-03-31.
 */
@SuppressWarnings("unused")
interface Operations {
    byte[] readTime();

    byte[] writeTime();

    byte[] readCurrentValue();

    byte[] readUserData();

    byte[] writeUserData(int height, int weight, int gender, int age);

    byte[] readDateBlockX6S();

    byte[] readHistoryX6S(int blockID, int hour);

    byte[] resetX6S(int flag);
}
