package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;
import com.rainbow.kam.bt_scanner.adapter.prime.HistoryAdapter;
import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        NestedScrollView.OnScrollChangeListener,
        ViewPager.OnPageChangeListener {

    private Context context;
    private View view;

    private int index;
    private int totalStep, totalCalorie, totalDistance;

    private List<PrimeCircleFragment> primeCircleFragments;
    private List<String> units;
    private List<String> totalLabels;
    private List<Drawable> totalCardImageDrawable;

    private SwipeRefreshLayout swipeRefreshLayout;

    private CardView totalCardView;
    private TextView labelTextView, totalTextView;
    private ImageView cardImageView;

    private ViewPager viewPager;

    private CardView chartCardView;
    private LineChartView chart;
    private LineSet dataSet;

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;

    private int stickyChartCardScrollValue, stickyTotalCardScrollValue;

    private final Runnable postSwipeRefresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };

    private OnRefreshListener onRefreshListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        units = Arrays.asList(getString(R.string.prime_step), getString(R.string.prime_calorie), getString(R.string.prime_distance));
        totalLabels = Arrays.asList(getString(R.string.prime_total_step), getString(R.string.prime_total_calorie), getString(R.string.prime_total_distance));
        totalCardImageDrawable = Arrays.asList(
                ContextCompat.getDrawable(context, R.drawable.step_wallpaper),
                ContextCompat.getDrawable(context, R.drawable.calorie_wallpaper),
                ContextCompat.getDrawable(context, R.drawable.distance_wallpaper));
        onRefreshListener = (OnRefreshListener) context;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime, container, false);

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
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                totalCardView.getLayoutParams().height = totalCardView.getWidth() / 3;
                chart.getLayoutParams().height = chart.getWidth() / 2;
                historyRecyclerView.getLayoutParams().height = view.getHeight() - chartCardView.getHeight();

                stickyTotalCardScrollValue = viewPager.getHeight();
                stickyChartCardScrollValue = totalCardView.getHeight() + stickyTotalCardScrollValue;
            }
        });
    }


    private void setTotalCardView() {
        totalCardView = (CardView) view.findViewById(R.id.prime_card);
        labelTextView = (TextView) view.findViewById(R.id.prime_label);
        labelTextView.setText(totalLabels.get(index));
        totalTextView = (TextView) view.findViewById(R.id.prime_value);

        cardImageView = (ImageView) view.findViewById(R.id.prime_card_image);
        cardImageView.setImageDrawable(totalCardImageDrawable.get(index));
    }


    private void setFragments() {
        primeCircleFragments = Arrays.asList(
                PrimeCircleFragment.newInstance(PrimeActivity.INDEX_STEP),
                PrimeCircleFragment.newInstance(PrimeActivity.INDEX_CALORIE),
                PrimeCircleFragment.newInstance(PrimeActivity.INDEX_DISTANCE)
        );
    }


    private void setMaterialViews() {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.prime_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        NestedScrollView nestedScrollView = (NestedScrollView) view.findViewById(R.id.prime_nested);
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
        viewPager = (ViewPager) view.findViewById(R.id.prime_viewpager);
        viewPager.setAdapter(new PrimeAdapter(getActivity().getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.prime_tabs);
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
        chartCardView = (CardView) view.findViewById(R.id.prime_tab_chart);

        chart = (LineChartView) view.findViewById(R.id.prime_chart);
        chart.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(context, R.color.chart_label))
                .setXAxis(true)
                .setYAxis(false);
    }


    private void setRecyclerView() {
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.history_recycler);
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


    public void setPrimeValue(List<RealmPrimeItem> results) {

        setValueEmpty();

        int length = results.size();

        String[] chartLabels = new String[length];
        float[] chartValues = new float[length];


        for (int i = 0; i < length; i++) {
            RealmPrimeItem realmPrimeItem = results.get(i);

            chartLabels[i] = realmPrimeItem.getCalendar();
            chartValues[i] = (realmPrimeItem.getStep()
                    + realmPrimeItem.getCalorie()
                    + realmPrimeItem.getDistance()) / 3;

            totalStep += realmPrimeItem.getStep();
            totalCalorie += realmPrimeItem.getCalorie();
            totalDistance += realmPrimeItem.getDistance();
        }

        String total = getTotalValue(index) + units.get(index);
        totalTextView.setText(total);
        labelTextView.setText(totalLabels.get(index));


        setCircleCounterValue(results.get(length - 1));
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
            dataSet.setColor(ContextCompat.getColor(context, R.color.chart_line))
                    .setFill(ContextCompat.getColor(context, R.color.chart_fill))
                    .setDotsColor(ContextCompat.getColor(context, R.color.chart_dots))
                    .setThickness(5);
            chart.addData(dataSet);
            chart.show();
        }
    }


    public void setTextFail() {
        if (isVisible()) {
            totalTextView.setText(getString(R.string.prime_access_denial));
        }
    }


    private void setTotalCardTransition(int scrollY) {
        totalCardView.setTranslationY(scrollY);
    }


    private void setChartTransition(int scrollY) {
        chartCardView.setTranslationY(scrollY);
    }


    public void setCircleCounterGoalRange(GoalVo goalVo) {
        for (PrimeCircleFragment primeCircleFragment : primeCircleFragments) {
            primeCircleFragment.setCircleCounterGoalRange(goalVo);
        }
    }


    private void setCircleCounterNone() {
        for (PrimeCircleFragment primeCircleFragment : primeCircleFragments) {
            primeCircleFragment.setNoneValue();
        }
    }


    private void setCircleCounterValue(RealmPrimeItem realmPrimeItem) {
        for (PrimeCircleFragment primeCircleFragment : primeCircleFragments) {
            primeCircleFragment.setCircleValue(realmPrimeItem);
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


        private final List<String> tabTitles = Arrays.asList(getString(R.string.prime_step_title),
                getString(R.string.prime_calorie_title), getString(
                        R.string.prime_distance_title));
        private final int PAGE_COUNT = tabTitles.size();


        public PrimeAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return primeCircleFragments.get(position);
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



