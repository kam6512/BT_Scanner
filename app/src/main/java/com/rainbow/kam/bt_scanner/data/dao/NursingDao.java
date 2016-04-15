package com.rainbow.kam.bt_scanner.data.dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.Device;
import com.rainbow.kam.bt_scanner.data.item.DateHistoryBlockItem;
import com.rainbow.kam.bt_scanner.data.item.UserMovementItem;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;

import java.lang.ref.WeakReference;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2016-02-11.
 */
public class NursingDao {

    private static final String KEY = "USER";

    private static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    private static final String KEY_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private enum USER_KEY {
        KEY_NAME("USER_NAME"),
        KEY_AGE("USER_AGE"),
        KEY_HEIGHT("USER_HEIGHT"),
        KEY_WEIGHT("USER_WEIGHT"),
        KEY_GENDER("USER_GENDER");

        private final String keyValue;


        USER_KEY(String keyValue) {
            this.keyValue = keyValue;
        }
    }

    private enum USER_DEF {
        DEF_NAME(R.string.user_name_default),
        DEF_AGE(R.string.user_age_default),
        DEF_HEIGHT(R.string.user_height_default),
        DEF_WEIGHT(R.string.user_weight_default);

        private final int defValue;


        USER_DEF(int defValue) {
            this.defValue = defValue;
        }
    }

    private enum GOAL_KEY {

        KEY_GOAL_STEP("STEP_GOAL"),
        KEY_GOAL_CALORIE("CALORIE_GOAL"),
        KEY_GOAL_DISTANCE("DISTANCE_GOAL");

        private final String keyValue;


        GOAL_KEY(String keyValue) {
            this.keyValue = keyValue;
        }
    }

    private enum GOAL_DEF {

        DEF_GOAL_STEP(R.string.goal_def_step),
        DEF_GOAL_CALORIE(R.string.goal_def_calorie),
        DEF_GOAL_DISTANCE(R.string.goal_def_distance);

        private final int defValue;


        GOAL_DEF(int defValue) {
            this.defValue = defValue;
        }
    }

    private static WeakReference<Context> weakReference;

    private static SharedPreferences sharedPreferences = null;
    private static SharedPreferences.Editor editor = null;
    private UserMovementItem lastItem;

    private String format;


    private NursingDao() {
        //SingleTone Yeah~
    }


    private static class PrimeDaoLoader {
        private static final NursingDao INSTANCE = new NursingDao();
    }


    public static NursingDao getInstance(Context instanceContext) {
        weakReference = new WeakReference<>(instanceContext);

        sharedPreferences = getDaoContext().getSharedPreferences(KEY, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        return PrimeDaoLoader.INSTANCE;
    }


    private static Context getDaoContext() {
        return weakReference.get();
    }


    public Device loadDeviceData() {
        String name = sharedPreferences.getString(KEY_DEVICE_NAME, null);
        String address = sharedPreferences.getString(KEY_DEVICE_ADDRESS, null);
        return new Device(name, address);
    }


    public UserVo loadUserData() {
        UserVo userVo = new UserVo();
        userVo.name = getUserValue(USER_KEY.KEY_NAME, USER_DEF.DEF_NAME);
        userVo.age = getUserValue(USER_KEY.KEY_AGE, USER_DEF.DEF_AGE);
        userVo.height = getUserValue(USER_KEY.KEY_HEIGHT, USER_DEF.DEF_HEIGHT);
        userVo.weight = getUserValue(USER_KEY.KEY_WEIGHT, USER_DEF.DEF_WEIGHT);
        userVo.gender = sharedPreferences.getBoolean(USER_KEY.KEY_GENDER.keyValue, true);
        return userVo;
    }


    public GoalVo loadGoalData() {
        GoalVo goalVo = new GoalVo();
        goalVo.stepGoal = getGoalValue(GOAL_KEY.KEY_GOAL_STEP, GOAL_DEF.DEF_GOAL_STEP);
        goalVo.calorieGoal = getGoalValue(GOAL_KEY.KEY_GOAL_CALORIE, GOAL_DEF.DEF_GOAL_CALORIE);
        goalVo.distanceGoal = getGoalValue(GOAL_KEY.KEY_GOAL_DISTANCE, GOAL_DEF.DEF_GOAL_DISTANCE);
        return goalVo;
    }


    public void saveDeviceData(Device device) {
        editor.putString(KEY_DEVICE_NAME, device.getName());
        editor.putString(KEY_DEVICE_ADDRESS, device.getAddress());
        editor.apply();
    }


    public void saveUserData(UserVo userVo) {
        putUserValue(USER_KEY.KEY_NAME, userVo.name);
        putUserValue(USER_KEY.KEY_AGE, userVo.age);
        putUserValue(USER_KEY.KEY_HEIGHT, userVo.height);
        putUserValue(USER_KEY.KEY_WEIGHT, userVo.weight);
        editor.putBoolean(USER_KEY.KEY_GENDER.keyValue, userVo.gender);
        editor.apply();
    }


    public void saveGoalData(GoalVo goalVo) {
        putGoalValue(GOAL_KEY.KEY_GOAL_STEP, goalVo.stepGoal);
        putGoalValue(GOAL_KEY.KEY_GOAL_CALORIE, goalVo.calorieGoal);
        putGoalValue(GOAL_KEY.KEY_GOAL_DISTANCE, goalVo.distanceGoal);
        editor.apply();
    }


    public void clearSharedPreferenceData() {
        editor.clear().apply();
    }


    public int matchingRealmItem(List<DateHistoryBlockItem> historyItemList) {
        if (lastItem != null) {
            String lastDate = lastItem.getCalendar();
            for (int i = 0; i < historyItemList.size(); i++) {
                if (lastDate.contentEquals(historyItemList.get(i).historyBlockCalendar)) {
                    return i;
                }
            }
        }
        return -1;
    }


    public boolean isAllDataEmpty() {
        return sharedPreferences.getAll().isEmpty();
    }


    @DebugLog
    public boolean isUserDataAvailable() {
        return sharedPreferences.contains(USER_KEY.KEY_NAME.keyValue);
    }


    private String getUserValue(USER_KEY userKey, USER_DEF userDef) {
        return getSharedPreferenceValue(userKey.keyValue, userDef.defValue);
    }


    private String getGoalValue(GOAL_KEY goalKey, GOAL_DEF goalDef) {
        return getSharedPreferenceValue(goalKey.keyValue, goalDef.defValue);
    }


    private String getSharedPreferenceValue(String key, int defValue) {
        return sharedPreferences.getString(key, getDaoContext().getString(defValue));
    }


    private void putUserValue(USER_KEY userKey, String userVoValue) {
        putSharedPreferenceValue(userKey.keyValue, userVoValue);
    }


    private void putGoalValue(GOAL_KEY goalKey, String goalVoValue) {
        putSharedPreferenceValue(goalKey.keyValue, goalVoValue);
    }


    private void putSharedPreferenceValue(String key, String value) {
        editor.putString(key, value);
    }
}
