package com.rainbow.kam.bt_scanner.activity.prime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.prime.setting.LogoFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.setting.SelectDeviceDialogFragment;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class PrimeSettingActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceSelectListener, View.OnClickListener {

    private static final String TAG = PrimeSettingActivity.class.getSimpleName();

    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;
    private SelectDeviceDialogFragment nursingSelectDialog;

    private MaterialDialog materialDialog;

    private String userName;
    private String userAge;
    private String userHeight;
    private String userWeight;
    private String userStep;
    private String userGender;

    private SharedPreferences.Editor editor;


    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_band_init);
        try {

            SharedPreferences sharedPreferences = getSharedPreferences(PrimeHelper.KEY, MODE_PRIVATE);
            editor = sharedPreferences.edit();

            if (sharedPreferences.getAll().isEmpty()) {
                throw new Exception("User Info is NULL");
            } else {
                editor.apply();
                complete();
            }

        } catch (Exception e) {
            setFragment();
            setDialog();
            setUserInput();
            setBtn();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BluetoothHelper.check(this);
            }
        }
    }


    private void setFragment() {
        final LogoFragment logoFragment = new LogoFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.nursing_start_frame, logoFragment).commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed()) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(logoFragment)
                            .commitAllowingStateLoss();
                }
            }
        }, 2000);
    }


    private void setDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .positiveText(R.string.dialog_accept).negativeText(R.string.dialog_fix)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        if (nursingSelectDialog == null) {
                            nursingSelectDialog = new SelectDeviceDialogFragment();
                        }
                        nursingSelectDialog.show(getSupportFragmentManager(), "DeviceDialog");
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).build();
    }


    private void setUserInput() {
        name = (TextInputLayout) findViewById(R.id.nursing_add_user_name);
        age = (TextInputLayout) findViewById(R.id.nursing_add_user_age);
        height = (TextInputLayout) findViewById(R.id.nursing_add_user_height);
        weight = (TextInputLayout) findViewById(R.id.nursing_add_user_weight);
        step = (TextInputLayout) findViewById(R.id.nursing_add_user_step);
        genderGroup = (RadioGroup) findViewById(R.id.gender_group);
    }


    private void setBtn() {
        FloatingActionButton accept = (FloatingActionButton) findViewById(R.id.nursing_accept_fab);
        accept.setOnClickListener(this);

        FloatingActionButton skip = (FloatingActionButton) findViewById(R.id.nursing_skip_fab);
        skip.setOnClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onActivityResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    @DebugLog
    private void onAccept() {
        userName = name.getEditText().getText().toString();
        userAge = age.getEditText().getText().toString();
        userHeight = height.getEditText().getText().toString();
        userWeight = weight.getEditText().getText().toString();
        userStep = step.getEditText().getText().toString();

        String userGender;
        switch (genderGroup.getCheckedRadioButtonId()) {
            case R.id.radio_man:
                userGender = getString(R.string.gender_man);
                break;
            case R.id.radio_woman:
                userGender = getString(R.string.gender_woman);
                break;
            default:
                userGender = getString(R.string.gender_man);
                break;
        }

        if (!checkTextInputLayout(findViewById(R.id.nursing_init_group))) {
            String dialogContent = "이름 : " + userName +
                    "\n성별 : " + userGender +
                    "\n나이 : " + userAge +
                    "\n키 : " + userHeight +
                    "\n몸무게 : " + userWeight +
                    "\n걸음너비 : " + userStep;

            materialDialog.setTitle(R.string.dialog_accept_ok);
            materialDialog.setContent(dialogContent);
            materialDialog.show();
        }
    }


    @DebugLog
    private void onSkip() {
        userName = getString(R.string.user_name_default);
        userAge = getString(R.string.user_age_default);
        userHeight = getString(R.string.user_height_default);
        userWeight = getString(R.string.user_weight_default);
        userStep = getString(R.string.user_step_default);
        userGender = getString(R.string.gender_man);

        materialDialog.setTitle(R.string.dialog_skip);
        materialDialog.setContent(R.string.dialog_skip_warning);
        materialDialog.show();
    }


    @DebugLog
    private void complete() {
        finish();
        startActivity(new Intent(PrimeSettingActivity.this, PrimeActivity.class));
    }


    @DebugLog
    private boolean checkTextInputLayout(View view) {
        boolean hasError = false;
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int length = viewGroup.getChildCount();
            for (int i = 0; i < length; i++) {
                View v = viewGroup.getChildAt(i);
                if (v instanceof TextInputLayout) {
                    TextInputLayout textInputLayout = (TextInputLayout) v;
                    if (TextUtils.isEmpty(textInputLayout.getEditText().getText().toString())) {
                        textInputLayout.setErrorEnabled(true);
                        textInputLayout.setError("다시 입력하세요");
                        hasError = true;
                    } else {
                        textInputLayout.setErrorEnabled(false);
                    }
                }
            }
        }
        return hasError;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nursing_accept_fab:
                onAccept();
                break;
            case R.id.nursing_skip_fab:
                onSkip();
                break;
        }

    }


    @DebugLog
    @Override
    public void onDeviceSelect(final String name, final String address) {

        editor.putString(PrimeHelper.KEY_NAME, userName);
        editor.putString(PrimeHelper.KEY_AGE, userAge);
        editor.putString(PrimeHelper.KEY_HEIGHT, userHeight);
        editor.putString(PrimeHelper.KEY_WEIGHT, userWeight);
        editor.putString(PrimeHelper.KEY_STEP, userStep);
        editor.putString(PrimeHelper.KEY_GENDER, userGender);
        editor.putString(PrimeHelper.KEY_DEVICE_NAME, name);
        editor.putString(PrimeHelper.KEY_DEVICE_ADDRESS, address);
        editor.commit();
        complete();
    }
}
