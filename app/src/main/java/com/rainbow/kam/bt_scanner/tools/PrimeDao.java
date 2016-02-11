package com.rainbow.kam.bt_scanner.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2016-02-11.
 */
public class PrimeDao {
    private Context context;

    private SharedPreferences sharedPreferences;
    private Realm realm;


    public PrimeDao(Context context) {
        this.context = context;

        sharedPreferences = context.getSharedPreferences(PrimeHelper.KEY, Context.MODE_PRIVATE);
        realm = Realm.getInstance(new RealmConfiguration.Builder(context).build());
    }


    public boolean isUserDeviceDataEmpty() {
        return sharedPreferences.getAll().isEmpty();
    }


    public boolean isUserDataEmpty() {
        return !sharedPreferences.contains(PrimeHelper.KEY_NAME);
    }


    public UserData loadUserData() {
        return new UserData();
    }


    public DeviceData loadDeviceData() {
        return new DeviceData();
    }


    public void saveUserData(String name, String age, String height, String weight, boolean gender) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_NAME, name);
        editor.putString(PrimeHelper.KEY_AGE, age);
        editor.putString(PrimeHelper.KEY_HEIGHT, height);
        editor.putString(PrimeHelper.KEY_WEIGHT, weight);
        editor.putBoolean(PrimeHelper.KEY_GENDER, gender);
        editor.apply();
    }


    public void saveDeviceData(String name, String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_DEVICE_NAME, name);
        editor.putString(PrimeHelper.KEY_DEVICE_ADDRESS, address);
        editor.apply();
    }


    public void removeUserDeviceData() {
        sharedPreferences.edit().clear().apply();
    }


    public RealmResults<RealmPrimeItem> loadPrimeListData() {
        return realm.where(RealmPrimeItem.class).findAll();
    }


    public void savePrimeData(RealmPrimeItem realmPrimeItem) {
        int step = realmPrimeItem.getStep();
        int calorie = realmPrimeItem.getCalorie();
        int distance = realmPrimeItem.getDistance();

        String format = context.getString(R.string.prime_save_date_format);
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        String today = formatter.format(Calendar.getInstance().getTime());

        RealmResults<RealmPrimeItem> results = realm.where(RealmPrimeItem.class).findAll();

        realm.beginTransaction();

        if (results.isEmpty()) {
            RealmPrimeItem newRealmPrimeItem = realm.createObject(RealmPrimeItem.class);
            newRealmPrimeItem.setCalendar(today);
            newRealmPrimeItem.setStep(step);
            newRealmPrimeItem.setCalorie(calorie);
            newRealmPrimeItem.setDistance(distance);
        } else {
            RealmPrimeItem lastRealmPrimeItem = results.last();
            if (lastRealmPrimeItem.getCalendar().equals(today)) {
                if (lastRealmPrimeItem.getStep() > step || lastRealmPrimeItem.getCalorie() > calorie || lastRealmPrimeItem.getDistance() > distance) {
                    step += lastRealmPrimeItem.getStep();
                    calorie += lastRealmPrimeItem.getCalorie();
                    distance += lastRealmPrimeItem.getDistance();
                }
                lastRealmPrimeItem.setCalendar(today);
                lastRealmPrimeItem.setStep(step);
                lastRealmPrimeItem.setCalorie(calorie);
                lastRealmPrimeItem.setDistance(distance);
            } else {
                RealmPrimeItem newRealmPrimeItem = realm.createObject(RealmPrimeItem.class);
                newRealmPrimeItem.setCalendar(today);
                newRealmPrimeItem.setStep(step);
                newRealmPrimeItem.setCalorie(calorie);
                newRealmPrimeItem.setDistance(distance);
            }
        }

        realm.commitTransaction();

        RealmPrimeItem.setTotalValue(results);
    }


    public PrimeData loadPrimeData() {
        return new PrimeData();
    }


    public void saveGoalData(String step, String calorie, String distance) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_GOAL_STEP, step);
        editor.putString(PrimeHelper.KEY_GOAL_CALORIE, calorie);
        editor.putString(PrimeHelper.KEY_GOAL_DISTANCE, distance);
        editor.apply();
    }


    public class UserData {
        private String name, age, height, weight;
        private boolean gender;


        public UserData() {
            name = sharedPreferences.getString(PrimeHelper.KEY_NAME, context.getString(R.string.user_name_default));
            age = sharedPreferences.getString(PrimeHelper.KEY_AGE, context.getString(R.string.user_age_default));
            height = sharedPreferences.getString(PrimeHelper.KEY_HEIGHT, context.getString(R.string.user_height_default));
            weight = sharedPreferences.getString(PrimeHelper.KEY_WEIGHT, context.getString(R.string.user_weight_default));
            gender = sharedPreferences.getBoolean(PrimeHelper.KEY_GENDER, true);
        }


        public String getName() {
            return name;
        }


        public String getAge() {
            return age;
        }


        public String getHeight() {
            return height;
        }


        public String getWeight() {
            return weight;
        }


        public boolean isGender() {
            return gender;
        }
    }

    public class DeviceData {
        private String name, address;


        public DeviceData() {
            name = sharedPreferences.getString(PrimeHelper.KEY_DEVICE_NAME, null);
            address = sharedPreferences.getString(PrimeHelper.KEY_DEVICE_ADDRESS, null);
        }


        public String getName() {
            return name;
        }


        public String getAddress() {
            return address;
        }

    }

    public class PrimeData {
        private String step, calorie, distance;


        public PrimeData() {
            step = sharedPreferences.getString(PrimeHelper.KEY_GOAL_STEP, context.getString(R.string.goal_def_step));
            calorie = sharedPreferences.getString(PrimeHelper.KEY_GOAL_CALORIE, context.getString(R.string.goal_def_calorie));
            distance = sharedPreferences.getString(PrimeHelper.KEY_GOAL_DISTANCE, context.getString(R.string.goal_def_distance));
        }


        public String getStep() {
            return step;
        }


        public String getCalorie() {
            return calorie;
        }


        public String getDistance() {
            return distance;
        }
    }
}
