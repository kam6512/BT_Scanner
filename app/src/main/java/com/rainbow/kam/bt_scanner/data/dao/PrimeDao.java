package com.rainbow.kam.bt_scanner.data.dao;

import android.content.Context;
import android.content.Intent;
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

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2016-02-11.
 */
public class PrimeDao {

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


    public RealmResults<RealmPrimeItem> loadPrimeResultData() {
        return realm.where(RealmPrimeItem.class).findAll();
    }


    public List<RealmPrimeItem> loadPrimeListData() {
        return loadPrimeResultData();
    }


    public void savePrimeData(final RealmPrimeItem realmPrimeItem) {

        final int step = realmPrimeItem.getStep();
        final int calorie = realmPrimeItem.getCalorie();
        final int distance = realmPrimeItem.getDistance();

        final String format = context.getString(R.string.prime_save_date_format);
        final SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        final String today = formatter.format(Calendar.getInstance().getTime());


        final Observable<RealmPrimeItem> observable = Observable.just(realmPrimeItem);
        observable.onBackpressureBuffer().subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.computation());
        observable.subscribe(new Subscriber<RealmPrimeItem>() {
            @Override
            public void onCompleted() {

            }


            @Override
            public void onError(Throwable e) {

            }


            @Override
            public void onNext(RealmPrimeItem realmPrimeItem) {
                realm.beginTransaction();
                final RealmResults<RealmPrimeItem> results = loadPrimeResultData();

                if (results.isEmpty()) {
                    RealmPrimeItem newItem = realm.createObject(RealmPrimeItem.class);
                    newItem.setCalendar(today);
                    newItem.setStep(step);
                    newItem.setCalorie(calorie);
                    newItem.setDistance(distance);
                } else {
                    RealmPrimeItem lastItem = results.last();
                    if (lastItem.getCalendar().equals(today)) {
                        int lastStep = 0;
                        int lastCalorie = 0;
                        int lastDistance = 0;
                        if (lastItem.getStep() > step) {
                            lastStep = lastItem.getStep();
                            lastCalorie = lastItem.getCalorie();
                            lastDistance = lastItem.getDistance();
                        }
                        lastItem.setCalendar(today);
                        lastItem.setStep(step + lastStep);
                        lastItem.setCalorie(calorie + lastCalorie);
                        lastItem.setDistance(distance + lastDistance);
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
        });
    }


    public void overWritePrimeData(RealmPrimeItem realmPrimeItem, boolean isOverWriteAllData) {

        final Observable<RealmPrimeItem> observable = Observable.just(realmPrimeItem);
        observable.onBackpressureBuffer().subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.computation());
        observable.subscribe(new Subscriber<RealmPrimeItem>() {

            @Override
            public void onCompleted() {
            }


            @Override
            public void onError(Throwable e) {

            }


            @Override
            public void onNext(RealmPrimeItem realmPrimeItem) {
                RealmResults<RealmPrimeItem> results = loadPrimeResultData();
                realm.beginTransaction();
                if (isOverWriteAllData) {
                    for (RealmPrimeItem item : results) {
                        int distance = realmPrimeItem.getDistance();
                        item.setDistance(distance);
                    }
                } else {
                    RealmPrimeItem lastItem = results.last();
                    int distance = realmPrimeItem.getDistance();
                    lastItem.setDistance(distance);
                }
                realm.commitTransaction();
            }
        });
    }


    public void removePrimeData() {
        final Observable<Class> observable = Observable.just(RealmPrimeItem.class);
        observable.onBackpressureBuffer().subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.computation());
        observable.subscribe(new Subscriber<Class>() {

            @Override
            public void onCompleted() {

            }


            @Override
            public void onError(Throwable e) {
            }


            @Override
            public void onNext(Class aClass) {
                realm.beginTransaction();
                realm.clear(RealmPrimeItem.class);
                realm.commitTransaction();
            }
        });
    }


    public boolean isAllDataEmpty() {
        return sharedPreferences.getAll().isEmpty();
    }


    @DebugLog
    public boolean isUserDataAvailable() {
        return sharedPreferences.contains(USER_KEY.KEY_NAME.keyValue);
    }


    public boolean isPrimeDataAvailable() {
        return realm.where(RealmPrimeItem.class).findAll().size() != 0;
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


    private void putUserValue(USER_KEY userKey, String userVoValue) {
        putSharedPreferenceValue(userKey.keyValue, userVoValue);
    }


    private void putGoalValue(GOAL_KEY goalKey, String goalVoValue) {
        putSharedPreferenceValue(goalKey.keyValue, goalVoValue);
    }


    private void putSharedPreferenceValue(String key, String value) {
        editor.putString(key, value);
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
