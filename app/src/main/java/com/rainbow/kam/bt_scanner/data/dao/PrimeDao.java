package com.rainbow.kam.bt_scanner.data.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.item.Migration;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;
import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by kam6512 on 2016-02-11.
 */
public class PrimeDao {

    private static final String KEY = "USER";

    private static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    private static final String KEY_DEVICE_ADDRESS = "DEVICE_ADDRESS";

//    private static final String KEY_NAME = "USER_NAME";
//    private static final String KEY_AGE = "USER_AGE";
//    private static final String KEY_HEIGHT = "USER_HEIGHT";
//    private static final String KEY_WEIGHT = "USER_WEIGHT";
//    private static final String KEY_GENDER = "USER_GENDER";

//    private static final int DEF_NAME = R.string.user_name_default;
//    private static final int DEF_AGE = R.string.user_age_default;
//    private static final int DEF_HEIGHT = R.string.user_height_default;
//    private static final int DEF_WEIGHT = R.string.user_weight_default;

//    private static final String KEY_GOAL_STEP = "STEP_GOAL";
//    private static final String KEY_GOAL_CALORIE = "CALORIE_GOAL";
//    private static final String KEY_GOAL_DISTANCE = "DISTANCE_GOAL";

//    private static final int DEF_GOAL_STEP = R.string.goal_def_step;
//    private static final int DEF_GOAL_CALORIE = R.string.goal_def_calorie;
//    private static final int DEF_GOAL_DISTANCE = R.string.goal_def_distance;

    private enum USER_KEY {
        KEY_NAME("USER_NAME"),
        KEY_AGE("USER_AGE"),
        KEY_HEIGHT("USER_HEIGHT"),
        KEY_WEIGHT("USER_WEIGHT"),
        KEY_GENDER("USER_GENDER");

        private String keyValue;


        USER_KEY(String keyValue) {
            this.keyValue = keyValue;
        }
    }

    private enum USER_DEF {
        DEF_NAME(R.string.user_name_default),
        DEF_AGE(R.string.user_age_default),
        DEF_HEIGHT(R.string.user_height_default),
        DEF_WEIGHT(R.string.user_weight_default);

        private int defValue;


        USER_DEF(int defValue) {
            this.defValue = defValue;
        }
    }

    private enum GOAL_KEY {

        KEY_GOAL_STEP("STEP_GOAL"),
        KEY_GOAL_CALORIE("CALORIE_GOAL"),
        KEY_GOAL_DISTANCE("DISTANCE_GOAL");

        private String keyValue;


        GOAL_KEY(String keyValue) {
            this.keyValue = keyValue;
        }
    }

    private enum GOAL_DEF {

        DEF_GOAL_STEP(R.string.goal_def_step),
        DEF_GOAL_CALORIE(R.string.goal_def_calorie),
        DEF_GOAL_DISTANCE(R.string.goal_def_distance);

        private int defValue;


        GOAL_DEF(int defValue) {
            this.defValue = defValue;
        }
    }

    private static Context context = null;

    private static SharedPreferences sharedPreferences = null;
    private static SharedPreferences.Editor editor = null;
    private static Realm realm = null;


    private PrimeDao() {

    }


    private static class PrimeDaoLoader {
        private static final PrimeDao INSTANCE = new PrimeDao();
    }


    public static PrimeDao getInstance(Context instanceContext) {
        if (context == null) {
            context = instanceContext;
            sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            realm = getRealm();
        }
        return PrimeDaoLoader.INSTANCE;
    }


    public DeviceVo loadDeviceData() {
        DeviceVo deviceVo = new DeviceVo();
        deviceVo.name = sharedPreferences.getString(KEY_DEVICE_NAME, null);
        deviceVo.address = sharedPreferences.getString(KEY_DEVICE_ADDRESS, null);
        return deviceVo;
    }


    public UserVo loadUserData() {
        UserVo userVo = new UserVo();
        userVo.name = getUserValue(USER_KEY.KEY_NAME, USER_DEF.DEF_NAME);
        userVo.age = getUserValue(USER_KEY.KEY_AGE, USER_DEF.DEF_AGE);
        userVo.height = getUserValue(USER_KEY.KEY_HEIGHT, USER_DEF.DEF_HEIGHT);
        userVo.weight = getUserValue(USER_KEY.KEY_WEIGHT, USER_DEF.DEF_WEIGHT);
        userVo.gender = getSharedPreferenceValue(USER_KEY.KEY_GENDER);
        return userVo;
    }


    public GoalVo loadGoalData() {
        GoalVo goalVo = new GoalVo();
        goalVo.stepGoal = getGoalValue(GOAL_KEY.KEY_GOAL_STEP, GOAL_DEF.DEF_GOAL_STEP);
        goalVo.calorieGoal = getGoalValue(GOAL_KEY.KEY_GOAL_CALORIE, GOAL_DEF.DEF_GOAL_CALORIE);
        goalVo.distanceGoal = getGoalValue(GOAL_KEY.KEY_GOAL_DISTANCE, GOAL_DEF.DEF_GOAL_DISTANCE);
        return goalVo;
    }


    public void saveDeviceData(DeviceVo deviceVo) {
        editor.putString(KEY_DEVICE_NAME, deviceVo.name);
        editor.putString(KEY_DEVICE_ADDRESS, deviceVo.address);
        editor.apply();
    }


    public void saveUserData(UserVo userVo) {
        putUserValue(USER_KEY.KEY_NAME, userVo.name);
        putUserValue(USER_KEY.KEY_AGE, userVo.age);
        putUserValue(USER_KEY.KEY_HEIGHT, userVo.height);
        putUserValue(USER_KEY.KEY_WEIGHT, userVo.weight);
        putSharedPreferenceValue(USER_KEY.KEY_GENDER, userVo.gender);
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


    public List<RealmPrimeItem> loadPrimeListData() {
        return realm.where(RealmPrimeItem.class).findAll();
    }


    public void savePrimeData(RealmPrimeItem realmPrimeItem) {
        int step = realmPrimeItem.getStep();
        int calorie = realmPrimeItem.getCalorie();
        int distance = realmPrimeItem.getDistance();

        String format = context.getString(R.string.prime_save_date_format);
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        String today = formatter.format(Calendar.getInstance().getTime());

        List<RealmPrimeItem> results = loadPrimeListData();

        realm.beginTransaction();

        if (results.isEmpty()) {
            RealmPrimeItem newItem = realm.createObject(RealmPrimeItem.class);
            newItem.setCalendar(today);
            newItem.setStep(step);
            newItem.setCalorie(calorie);
            newItem.setDistance(distance);
        } else {
            RealmPrimeItem lastItem = results.get(results.size() - 1);
            if (lastItem.getCalendar().equals(today)) {
                if (lastItem.getStep() > step) {
                    step += lastItem.getStep();
                    calorie += lastItem.getCalorie();
                    distance += lastItem.getDistance();
                }
                lastItem.setCalendar(today);
                lastItem.setStep(step);
                lastItem.setCalorie(calorie);
                lastItem.setDistance(distance);
            } else {
                RealmPrimeItem newItem = realm.createObject(RealmPrimeItem.class);
                newItem.setCalendar(today);
                newItem.setStep(step);
                newItem.setCalorie(calorie);
                newItem.setDistance(distance);
            }
        }

        realm.commitTransaction();
    }


    public void removePrimeData() {
        realm.beginTransaction();
        realm.clear(RealmPrimeItem.class);
        realm.commitTransaction();
    }


    public boolean isAllDataEmpty() {
        return sharedPreferences.getAll().isEmpty();
    }


    public boolean isUserDataEmpty() {
        return !sharedPreferences.contains(USER_KEY.KEY_NAME.keyValue);
    }


    private String getUserValue(USER_KEY userKey, USER_DEF userDef) {
        return getSharedPreferenceValue(userKey.keyValue, userDef.defValue);
    }


    private String getGoalValue(GOAL_KEY goalKey, GOAL_DEF goalDef) {
        return getSharedPreferenceValue(goalKey.keyValue, goalDef.defValue);
    }


    private String getSharedPreferenceValue(String key, int defValue) {
        return sharedPreferences.getString(key, context.getString(defValue));
    }


    private boolean getSharedPreferenceValue(USER_KEY userKey) {
        return sharedPreferences.getBoolean(userKey.keyValue, true);
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


    private void putSharedPreferenceValue(USER_KEY userKey, boolean value) {
        editor.putBoolean(userKey.keyValue, value);
    }


    private static Realm getRealm() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(context).migration(new Migration()).build();

        try {
            return Realm.getInstance(realmConfiguration);
        } catch (RealmMigrationNeededException e) {
            try {
                Realm.deleteRealm(realmConfiguration);
                //Realm file has been deleted.
                return Realm.getInstance(realmConfiguration);
            } catch (Exception ex) {
                throw ex;
                //No Realm file to remove.
            }
        }
    }
}
