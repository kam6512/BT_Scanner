package com.rainbow.kam.bt_scanner.operation;


import com.rainbow.kam.bt_scanner.data.DEVICE_TYPE;

import java.util.Random;

/**
 * Created by kam6512 on 2016-03-28.
 */
public class Operator implements Operations {

    public final static String READ_TIME = "READ TIME";
    public final static String WRITE_TIME = "WRITE TIME";
    public final static String READ_CURRENT_VALUE = "READ CURRENT VALUE";
    public final static String READ_USER_INFO = "READ USER INFO";
    public final static String WRITE_USER_INFO = "WRITE USER INFO";
    public final static String READ_RECODED_DATE = "READ RECODED DATE";
    public final static String READ_HISTORY = "READ HISTORY";
    public final static String RESET = "RESET";

    public final static String READ_BATTERY_VALUE = "READ BATTERY VALUE";

    private static Operator operator;

    private final Random random = new Random();


    public static Operator getOperator(DEVICE_TYPE type) {
        switch (type) {
            case Prime:
                operator = new Prime();
            case X6S:
                operator = new X6S();
        }
        return operator;
    }


    public byte[] getOperationBytes(String currentCommand) {
        byte[] requestData;
        switch (currentCommand) {
            case Operator.READ_TIME:
                requestData = operator.readTime();
                break;
            case Operator.WRITE_TIME:
                requestData = operator.writeTime();
                break;
            case Operator.READ_CURRENT_VALUE:
                requestData = operator.readCurrentValue();
                break;
            case Operator.READ_USER_INFO:
                requestData = operator.readUserData();
                break;
            case Operator.WRITE_USER_INFO:
                requestData = operator.writeUserData(
                        random.nextInt(61) + 140,   // 140 cm ~ 200 cm
                        random.nextInt(71) + 40,    // 40 Kg ~ 110 Kg
                        random.nextInt(2),  // 0 (Male) / 1 (Female)
                        random.nextInt(101) // 0 ~ 100 ¼¼ (¸¸)
                );
                break;
            case Operator.READ_RECODED_DATE:
                requestData = operator.readDateBlockX6S();
                break;
            case Operator.READ_HISTORY:
                requestData = operator.readHistoryX6S(
                        random.nextInt(7) + 1,
                        random.nextInt(23));
//                requestData = operator.readHistoryX6S(7,12);
                break;
            case Operator.RESET:
//                requestData = operator.resetX6S(1);
                requestData = operator.resetX6S(2);
//                requestData = operator.resetX6S(3);
                break;
            default:
                requestData = operator.readTime();
                break;
        }
        return requestData;
    }


    @Override public byte[] readTime() {
        return new byte[0];
    }


    @Override public byte[] writeTime() {
        return new byte[0];
    }


    @Override public byte[] readCurrentValue() {
        return new byte[0];
    }


    @Override public byte[] readUserData() {
        return new byte[0];
    }


    @Override public byte[] writeUserData(int height, int weight, int gender, int age) {
        return new byte[0];
    }


    @Override public byte[] readDateBlockX6S() {
        return new byte[0];
    }


    @Override public byte[] readHistoryX6S(int blockID, int hour) {
        return new byte[0];
    }


    @Override public byte[] resetX6S(int flag) {
        return new byte[0];
    }
}

