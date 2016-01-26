package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;
import com.rainbow.kam.bt_scanner.tools.view.CircleCounter;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private int index;

    private Context context;
    private View view;
    private CardView cardView;
    private TextView labelTextView, valueTextView;
    private ImageView cardImageView;
    private CircleCounter circleCounter;

    private final int[] label = {R.string.prime_total_step, R.string.prime_total_calorie, R.string.prime_total_distance};
    private final int[] cardImage = {R.drawable.step_wallpaper, R.drawable.calorie_wallpaper, R.drawable.distance_wallpaper};
    private final int[] unit = {R.string.prime_step, R.string.prime_calorie, R.string.prime_distance};

    private final String[] keyRange = {PrimeHelper.KEY_GOAL_STEP, PrimeHelper.KEY_GOAL_CALORIE, PrimeHelper.KEY_GOAL_DISTANCE};
    private final int[] defRange = {R.string.goal_def_step, R.string.goal_def_calorie, R.string.goal_def_distance};

    private SharedPreferences sharedPreferences;


    public static PrimeFragment newInstance(int index) {
        PrimeFragment primeFragment = new PrimeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PrimeHelper.KEY_INDEX, index);
        primeFragment.setArguments(bundle);
        return primeFragment;
    }


    private int getIndex() {
        return getArguments().getInt(PrimeHelper.KEY_INDEX);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            this.context = context;
            sharedPreferences = context.getSharedPreferences(PrimeHelper.KEY, Context.MODE_PRIVATE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime, container, false);

        index = getIndex();
        setCardView();
        setCircleCounterView();
        setResource();
        setCircleCounterGoalRange();

        return view;
    }


    private void setCardView() {
        cardView = (CardView) view.findViewById(R.id.prime_card);
        labelTextView = (TextView) view.findViewById(R.id.prime_label);
        valueTextView = (TextView) view.findViewById(R.id.prime_value);
        cardImageView = (ImageView) view.findViewById(R.id.prime_card_image);
    }


    private void setCircleCounterView() {
        circleCounter = (CircleCounter) view.findViewById(R.id.counter);
        circleCounter.setFirstColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setSecondColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setThirdColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
        circleCounter.setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));
    }


    private void setResource() {
        labelTextView.setText(getString(label[index]));
        cardImageView.setImageResource(cardImage[index]);
        circleCounter.setMetricText(getString(unit[index]));
    }


    @DebugLog
    public void setCircleCounterGoalRange() {
        circleCounter.setRange(sharedPreferences.getString(keyRange[index], getString(defRange[index])));
        circleCounter.resetRange();
    }


    public void setTextTotalValue(int totalValue) {
        valueTextView.setText(String.valueOf(totalValue) + getString(unit[index]));
    }


    public void setCircleValue(int value) {
        circleCounter.setValues(value, value, value);
    }


    public void setTextFail() {
        if (isVisible()) {
            valueTextView.setText(getString(R.string.prime_access_denial));
        }
    }


    public void setCardTransition(int y) {
        cardView.setTranslationY(y);
    }

}
