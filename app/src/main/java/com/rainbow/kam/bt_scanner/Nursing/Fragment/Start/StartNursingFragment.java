package com.rainbow.kam.bt_scanner.Nursing.Fragment.Start;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.Nursing.PatientRealmOBJ.Patient;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Nursing.Activity.StartNursingActivity;
import com.rainbow.kam.bt_scanner.Tools.Design.RippleView;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class StartNursingFragment extends Fragment implements View.OnClickListener, RippleView.OnRippleCompleteListener {

    Activity activity;
    public static Handler handler;
    private FragmentManager fm;
    private SelectNursingDeviceFragment dialogFragment;

//    private Animation animation;

    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;

    private String userName;
    private String userAge;
    private String userHeight;
    private String userWeight;
    private String userStep;
    private String gender;

    private Realm realm;
    private RealmAsyncTask transaction;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;
        if (StartNursingActivity.indexByStart == 0) {

            view = inflater.inflate(R.layout.fragment_nursing_start_splash, container, false);

//            LinearLayout pilotLayout = (LinearLayout) view.findViewById(R.id.nursing_start_pilot);

//            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.show_fab);
//            pilotLayout.startAnimation(animation);
//            StartNursingActivity.nursingHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                startNursingFab.setVisibility(View.VISIBLE);
//                startNursingFab.startAnimation(animation);
//
//                }
//            }, 200);

            StartNursingActivity.indexByStart++;

        } else {

            fm = getFragmentManager();

            view = inflater.inflate(R.layout.fragment_nursing_start_adduser, container, false);

            name = (TextInputLayout) view.findViewById(R.id.nursing_adduser_name);
            age = (TextInputLayout) view.findViewById(R.id.nursing_adduser_age);
            height = (TextInputLayout) view.findViewById(R.id.nursing_adduser_height);
            weight = (TextInputLayout) view.findViewById(R.id.nursing_adduser_weight);
            step = (TextInputLayout) view.findViewById(R.id.nursing_adduser_step);
            genderGroup = (RadioGroup) view.findViewById(R.id.gender_group);

            RippleView skip = (RippleView) view.findViewById(R.id.nursing_adduser_skip);
            skip.setOnRippleCompleteListener(this);

            StartNursingActivity.startNursingFab.setOnClickListener(this);


            handler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {
                    super.handleMessage(msg);


                    final Bundle callbackBundle = msg.getData();
                    Realm.removeDefaultConfiguration();
                    realm = Realm.getInstance(getActivity());
                    realm.beginTransaction();
                    realm.allObjects(Patient.class).clear();
                    transaction = realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Patient patient = realm.createObject(Patient.class);
                            patient.setName(userName);
                            patient.setAge(userAge);
                            patient.setHeight(userHeight);
                            patient.setWeight(userWeight);
                            patient.setStep(userStep);
                            patient.setGender(gender);
                            patient.setDeviceName(callbackBundle.getString("name"));
                            patient.setDeviceAddress(callbackBundle.getString("address"));
                        }
                    }, new Realm.Transaction.Callback() {
                        @Override
                        public void onSuccess() {
                            RealmResults<Patient> results = realm.where(Patient.class).equalTo("name", name.getEditText().getText().toString()).findAll();
//                            Log.e(StartNursingActivity.TAG, results.size() + " / " + results.get(0).getName() + " / " + results.get(0).getAge() + " / " + results.get(0).getHeight() + " / " + results.get(0).getWeight() + " / " + results.get(0).getStep() + " / " + results.get(0).getDeviceName() + " / " + results.get(0).getDeviceAddress());
                            Log.e(StartNursingActivity.TAG, " results.size : " + results.size());

                            dismissDeviceListDialog();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getContext(), "fail " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    realm.commitTransaction();


                }
            };
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
        if (transaction != null && !transaction.isCancelled()) {
            transaction.cancel();
        }
    }

    private void showAcceptDialog(String res) {
        new MaterialDialog.Builder(getActivity()).title("기입 정보가 확실한지 확인해주시기 바랍니다.")
                .content(res)
                .positiveText("확인").negativeText("수정")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        showDeviceListDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).show();
    }

    private void showSkipDialog() {
        new MaterialDialog.Builder(getActivity()).title("무시하기")
                .content("개인정보를 입력하지 않더라도 사용할 수 있으나 정확한 운동량 계산과 서버연동이 불가능하오니 이점 유의해두시기 바랍니다.")
                .positiveText("확인").negativeText("수정")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        showDeviceListDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).show();
    }

    private void showDeviceListDialog() {
        if (fm != null) {
            dialogFragment = new SelectNursingDeviceFragment();
            dialogFragment.show(fm, "DeviceDialog");
        }
    }

    private void dismissDeviceListDialog() {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        getActivity().finish();
    }

    @Override
    public void onClick(View v) {
        userName = name.getEditText().getText().toString();
        userAge = age.getEditText().getText().toString();
        userHeight = height.getEditText().getText().toString();
        userWeight = weight.getEditText().getText().toString();
        userStep = step.getEditText().getText().toString();
        gender = "";

        switch (genderGroup.getCheckedRadioButtonId()) {
            case R.id.radio_man:
                gender = "남성";
                break;
            case R.id.radio_woman:
                gender = "여성";
                break;
            default:
                gender = "남성";
                break;
        }


        if (userName == null || userName.equals("") || userName.length() <= 1) {
            name.setError("Name is missing");
        } else if (userAge == null || userAge.equals("") || userAge.length() <= 0) {
            age.setError("Age is missing");
        } else if (userHeight == null || userHeight.equals("") || userHeight.length() <= 0) {
            height.setError("Height is missing");
        } else if (userWeight == null || userWeight.equals("") || userWeight.length() <= 0) {
            weight.setError("weight is missing");
        } else if (userStep.equals("") || userStep.length() <= 0) {
            step.setError("step is missing");
        } else {
            name.setErrorEnabled(false);
            age.setErrorEnabled(false);
            height.setErrorEnabled(false);
            weight.setErrorEnabled(false);
            step.setErrorEnabled(false);

            String res = "이름 : " + userName +
                    "\n성별 : " + gender +
                    "\n나이 : " + userAge +
                    "\n키 : " + userHeight +
                    "\n몸무게 : " + userWeight +
                    "\n걸음너비 : " + userStep;
            showAcceptDialog(res);
        }
    }

    @Override
    public void onComplete(RippleView rippleView) {
        userName = "N/A";
        userAge = "20";
        userHeight = "170";
        userWeight = "60";
        userStep = "60";
        gender = "남성";
        showSkipDialog();
    }
}
