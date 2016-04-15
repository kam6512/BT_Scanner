package com.rainbow.kam.bt_scanner.ui.fragment.nursing.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.ui.adapter.nursing.HistoryAdapter;
import com.rainbow.kam.bt_scanner.data.item.UserMovementItem;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class NursingFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        NestedScrollView.OnScrollChangeListener,
        ViewPager.OnPageChangeListener {

    private Context context;

    public final static int INDEX_STEP = 0;
    public final static int INDEX_CALORIE = 1;
    public final static int INDEX_DISTANCE = 2;

    private int index;
    private int totalStep, totalCalorie, totalDistance;

    private List<NursingCircleFragment> nursingCircleFragments;

    private List<String> units;
    @BindString(R.string.nursing_step) String stepUnit;
    @BindString(R.string.nursing_calorie) String calorieUnit;
    @BindString(R.string.nursing_distance) String distanceUnit;

    private List<String> totalLabels;
    @BindString(R.string.nursing_total_step) String totalStepLabel;
    @BindString(R.string.nursing_total_calorie) String totalCalorieLabel;
    @BindString(R.string.nursing_total_distance) String totalDistanceLabel;

    private List<Drawable> totalCardImageDrawable;
    @BindDrawable(R.drawable.step_wallpaper) Drawable stepWallpaper;
    @BindDrawable(R.drawable.calorie_wallpaper) Drawable calorieWallpaper;
    @BindDrawable(R.drawable.distance_wallpaper) Drawable distanceWallpaper;

    @Bind(R.id.nursing_swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.nursing_nested) NestedScrollView nestedScrollView;

    @Bind(R.id.nursing_card) CardView totalCardView;
    @Bind(R.id.nursing_label) TextView labelTextView;
    @Bind(R.id.nursing_value) TextView totalTextView;
    @Bind(R.id.nursing_card_image) ImageView cardImageView;

    @Bind(R.id.nursing_viewpager) ViewPager viewPager;

    @Bind(R.id.nursing_tabs) TabLayout tabLayout;
    private List<String> tabTitles;
    @BindString(R.string.nursing_step_title) String stepTitle;
    @BindString(R.string.nursing_calorie_title) String calorieTitle;
    @BindString(R.string.nursing_distance_title) String distanceTitle;

    @Bind(R.id.nursing_tab_chart) CardView chartCardView;
    @Bind(R.id.nursing_chart) LineChartView chart;
    @BindColor(R.color.chart_label) int chartLabelColor;
    private LineSet dataSet;
    @BindColor(R.color.chart_line) int chartLineColor;
    @BindColor(R.color.chart_fill) int chartBackGroundColor;
    @BindColor(R.color.chart_dots) int chartDotsColor;

    @Bind(R.id.history_recycler) RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;

    private int stickyChartCardScrollValue, stickyTotalCardScrollValue;

    private final Runnable postSwipeRefresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };

    private OnRefreshListener onRefreshListener;

    @BindString(R.string.nursing_access_denial) String denialLabel;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        onRefreshListener = (OnRefreshListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_nursing, container, false);
        ButterKnife.bind(this, view);

        units = Arrays.asList(stepUnit, calorieUnit, distanceUnit);
        totalLabels = Arrays.asList(totalStepLabel, totalCalorieLabel, totalDistanceLabel);

        totalCardImageDrawable = Arrays.asList(stepWallpaper, calorieWallpaper, distanceWallpaper);

        tabTitles = Arrays.asList(stepTitle, calorieTitle, distanceTitle);

        setTotalCardView();
        setFragments();
        setMaterialViews();
        setViewPager();
        setChartView();
        setRecyclerView();

        return view;
    }


    public void onViewCreated(final View view, Bundle saved) {
        super.onViewCreated(view, saved);
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            totalCardView.getLayoutParams().height = totalCardView.getWidth() / 3;
            chart.getLayoutParams().height = chart.getWidth() / 2;
            historyRecyclerView.getLayoutParams().height = view.getHeight() - chartCardView.getHeight();

            stickyTotalCardScrollValue = viewPager.getHeight();
            stickyChartCardScrollValue = totalCardView.getHeight() + stickyTotalCardScrollValue;
        });
    }


    private void setTotalCardView() {
        labelTextView.setText(totalLabels.get(index));
        cardImageView.setImageDrawable(totalCardImageDrawable.get(index));
    }


    private void setFragments() {
        nursingCircleFragments = Arrays.asList(
                NursingCircleFragment.newInstance(INDEX_STEP),
                NursingCircleFragment.newInstance(INDEX_CALORIE),
                NursingCircleFragment.newInstance(INDEX_DISTANCE)
        );
    }


    private void setMaterialViews() {
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        nestedScrollView.setOnScrollChangeListener(this);
    }


    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY > stickyTotalCardScrollValue) {
            if (scrollY > stickyChartCardScrollValue) {
                setChartTransition(scrollY - stickyChartCardScrollValue);
            } else {
                setChartTransition(totalCardView.getVerticalScrollbarPosition());
            }
        } else {
            setTotalCardTransition(scrollY);
            if (chartCardView.getTranslationY() != 0) {
                setChartTransition(0);
            }
        }
    }


    private void setViewPager() {
        viewPager.setAdapter(new PrimeAdapter(getActivity().getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(this);

        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public void onPageSelected(int position) {
        index = position;
        String total = getTotalValue(index) + units.get(index);
        totalTextView.setText(total);
        labelTextView.setText(totalLabels.get(index));
        cardImageView.setImageDrawable(totalCardImageDrawable.get(index));
        historyAdapter.setCurrentIndex(index);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }


    @Override
    public void onPageScrollStateChanged(int state) {
    }


    private void setChartView() {
        chart.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(chartLabelColor)
                .setXAxis(true)
                .setYAxis(false);
    }


    private void setRecyclerView() {
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        historyRecyclerView.setHasFixedSize(true);
        historyRecyclerView.setFocusable(true);
        historyAdapter = new HistoryAdapter(context);
        historyRecyclerView.setAdapter(historyAdapter);
    }


    public boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }


    public void setRefreshing(boolean refresh) {
        if (refresh) {
            swipeRefreshLayout.post(postSwipeRefresh);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    @Override
    public void onRefresh() {
        onRefreshListener.onRefresh();
    }


    public void setValueEmpty() {
        totalStep = 0;
        totalCalorie = 0;
        totalDistance = 0;
        totalTextView.setText("0");
        labelTextView.setText("정보 없음");
        setCircleCounterNone();
        historyAdapter.setEmptyList();
    }



    public void setHistoryValue(List<UserMovementItem> results) {

        setValueEmpty();

        int length = results.size();

        String[] chartLabels = new String[length];
        float[] chartValues = new float[length];


        for (int i = 0; i < length; i++) {
            UserMovementItem userMovementItem = results.get(i);

            chartLabels[i] = userMovementItem.getCalendar();
            chartValues[i] = (userMovementItem.getStep()
                    + userMovementItem.getCalorie()
                    + userMovementItem.getDistance()) / 3;

            totalStep += userMovementItem.getStep();
            totalCalorie += userMovementItem.getCalorie();
            totalDistance += userMovementItem.getDistance();
        }

        String total = getTotalValue(index) + units.get(index);
        totalTextView.setText(total);
        labelTextView.setText(totalLabels.get(index));


//        setCircleCounterValue(results.get(length - 1));
        setChartValues(chartLabels, chartValues);
        historyAdapter.setHistoryList(results);
    }


    private void setChartValues(String[] chartLabels, float[] chartValues) {
        int weekCount = 7;
        if (chartValues.length > weekCount && chartLabels.length > weekCount) {
            chartLabels = Arrays.copyOfRange(chartLabels, chartLabels.length - weekCount, chartLabels.length);
            chartValues = Arrays.copyOfRange(chartValues, chartValues.length - weekCount, chartValues.length);
        }

        if (dataSet != null) {
            dataSet.updateValues(chartValues);
            chart.notifyDataUpdate();
        } else {
            dataSet = new LineSet(chartLabels, chartValues);
            dataSet.setColor(chartLineColor)
                    .setFill(chartBackGroundColor)
                    .setDotsColor(chartDotsColor)
                    .setThickness(5);
            chart.addData(dataSet);
            chart.show();
        }
    }


    public void setTextFail() {
        if (isVisible()) {
            totalTextView.setText(denialLabel);
        }
    }


    private void setTotalCardTransition(int scrollY) {
        totalCardView.setTranslationY(scrollY);
    }


    private void setChartTransition(int scrollY) {
        chartCardView.setTranslationY(scrollY);
    }


    public void setCircleCounterGoalRange(GoalVo goalVo) {
        for (NursingCircleFragment nursingCircleFragment : nursingCircleFragments) {
            nursingCircleFragment.setCircleCounterGoalRange(goalVo);
        }
    }


    private void setCircleCounterNone() {
        for (NursingCircleFragment nursingCircleFragment : nursingCircleFragments) {
            nursingCircleFragment.setNoneValue();
        }
    }


    public void setCircleCounterValue(UserMovementItem userMovementItem) {
        for (NursingCircleFragment nursingCircleFragment : nursingCircleFragments) {
            nursingCircleFragment.setCircleValue(userMovementItem);
        }
    }


    private int getTotalValue(int index) {
        switch (index) {
            case 0:
                return totalStep;
            case 1:
                return totalCalorie;
            case 2:
                return totalDistance;
            default:
                return totalStep;
        }
    }


    private class PrimeAdapter extends FragmentStatePagerAdapter {


        private final int PAGE_COUNT = tabTitles.size();


        public PrimeAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return nursingCircleFragments.get(position);
        }


        @Override
        public int getCount() {
            return PAGE_COUNT;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}



