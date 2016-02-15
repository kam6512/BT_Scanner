package com.rainbow.kam.bt_scanner.tools.data.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.data.item.Migration;
import com.rainbow.kam.bt_scanner.tools.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.tools.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.tools.data.vo.UserVo;

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

    private static final String KEY_NAME = "USER_NAME";
    private static final String KEY_AGE = "USER_AGE";
    private static final String KEY_HEIGHT = "USER_HEIGHT";
    private static final String KEY_WEIGHT = "USER_WEIGHT";
    private static final String KEY_GENDER = "USER_GENDER";

    private static final int DEF_NAME = R.string.user_name_default;
    private static final int DEF_AGE = R.string.user_age_default;
    private static final int DEF_HEIGHT = R.string.user_height_default;
    private static final int DEF_WEIGHT = R.string.user_weight_default;


    private static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    private static final String KEY_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    private static final String KEY_GOAL_STEP = "STEP_GOAL";
    private static final String KEY_GOAL_CALORIE = "CALORIE_GOAL";
    private static final String KEY_GOAL_DISTANCE = "DISTANCE_GOAL";

    private static final int DEF_GOAL_STEP = R.string.goal_def_step;
    private static final int DEF_GOAL_CALORIE = R.string.goal_def_calorie;
    private static final int DEF_GOAL_DISTANCE = R.string.goal_def_distance;

    private static class PrimeDaoLoader {
        private static final PrimeDao INSTANCE = new PrimeDao();
    }

    private static Context context = null;

    private static SharedPreferences sharedPreferences = null;
    private static SharedPreferences.Editor editor;
    private static Realm realm = null;


    private PrimeDao() {
        if (PrimeDaoLoader.INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }


    public static PrimeDao getInstance(Context instanceContext) {

        if (context == null) {
            context = instanceContext;
            sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            realm = getRealm();
            Log.e("getInstance", "new INSTANCE");
        } else {
            Log.e("getInstance", "maintain INSTANCE");
        }

        return PrimeDaoLoader.INSTANCE;
    }


    public boolean isAllDataEmpty() {
        return sharedPreferences.getAll().isEmpty();
    }


    public boolean isUserDataEmpty() {
        return !sharedPreferences.contains(KEY_NAME);
    }


    private String getSharedPreferenceValue(String key, int defValue) {
        return sharedPreferences.getString(key, context.getString(defValue));
    }


    private boolean getSharedPreferenceValue(String key) {
        return sharedPreferences.getBoolean(key, true);
    }


    public UserVo loadUserData() {
        UserVo userVo = new UserVo();
        userVo.name = getSharedPreferenceValue(KEY_NAME, DEF_NAME);
        userVo.age = getSharedPreferenceValue(KEY_AGE, DEF_AGE);
        userVo.height = getSharedPreferenceValue(KEY_HEIGHT, DEF_HEIGHT);
        userVo.weight = getSharedPreferenceValue(KEY_WEIGHT, DEF_WEIGHT);
        userVo.gender = getSharedPreferenceValue(KEY_GENDER);
        return userVo;
    }


    public DeviceVo loadDeviceData() {
        DeviceVo deviceVo = new DeviceVo();
        deviceVo.name = sharedPreferences.getString(KEY_DEVICE_NAME, null);
        deviceVo.address = sharedPreferences.getString(KEY_DEVICE_ADDRESS, null);
        return deviceVo;
    }


    public GoalVo loadGoalData() {
        GoalVo goalVo = new GoalVo();
        goalVo.stepGoal = getSharedPreferenceValue(KEY_GOAL_STEP, DEF_GOAL_STEP);
        goalVo.calorieGoal = getSharedPreferenceValue(KEY_GOAL_CALORIE, DEF_GOAL_CALORIE);
        goalVo.distanceGoal = getSharedPreferenceValue(KEY_GOAL_DISTANCE, DEF_GOAL_DISTANCE);
        return goalVo;
    }


    public void saveUserData(UserVo userVo) {
        editor.putString(KEY_NAME, userVo.name);
        editor.putString(KEY_AGE, userVo.age);
        editor.putString(KEY_HEIGHT, userVo.height);
        editor.putString(KEY_WEIGHT, userVo.weight);
        editor.putBoolean(KEY_GENDER, userVo.gender);
        editor.apply();
    }


    public void saveDeviceData(DeviceVo deviceVo) {
        editor.putString(KEY_DEVICE_NAME, deviceVo.name);
        editor.putString(KEY_DEVICE_ADDRESS, deviceVo.address);
        editor.apply();
    }


    public void saveGoalData(GoalVo goalVo) {
        editor.putString(KEY_GOAL_STEP, goalVo.stepGoal);
        editor.putString(KEY_GOAL_CALORIE, goalVo.calorieGoal);
        editor.putString(KEY_GOAL_DISTANCE, goalVo.distanceGoal);
        editor.apply();
    }


    public void removeUserDeviceData() {
        editor.clear().apply();
    }


    public void removePrimeData() {
        realm.beginTransaction();
        realm.clear(RealmPrimeItem.class);
        realm.commitTransaction();
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
