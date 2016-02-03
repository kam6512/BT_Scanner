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
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeFragment extends Fragment
        implements ViewTreeObserver.OnGlobalLayoutListener,
        NestedScrollView.OnScrollChangeListener,
        ViewPager.OnPageChangeListener {

    private Context context;
    private View view;

    private int index;

//    private final int[] total = new int[3];

    private List<PrimeCircleFragment> primeCircleFragments;
    private List<String> units;
    private List<String> totalLabels;
    private List<Drawable> totalCardImageDrawable;


    private CardView totalCardView;
    private TextView labelTextView, totalTextView;

    private ImageView cardImageView;


    private ViewPager viewPager;


    private CardView chartCardView;
    private LineChartView chart;
    private String[] chartLabels;
    private float[][] chartValues;

    private TextView updateTextView;

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;

    private int stickyChartCardScroll, stickyTotalCardScroll;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            this.context = context;
            units = Arrays.asList(getString(R.string.prime_step), getString(R.string.prime_calorie), getString(R.string.prime_distance));
            totalLabels = Arrays.asList(getString(R.string.prime_total_step), getString(R.string.prime_total_calorie), getString(R.string.prime_total_distance));
            totalCardImageDrawable = Arrays.asList(
                    ContextCompat.getDrawable(context, R.drawable.step_wallpaper),
                    ContextCompat.getDrawable(context, R.drawable.calorie_wallpaper),
                    ContextCompat.getDrawable(context, R.drawable.distance_wallpaper));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime, container, false);

        setTotalCardView();
        setFragments();
        setNestedScrollView();
        setViewPager();
        setChartView();
        setRecyclerView();

        return view;
    }


    @DebugLog
    public void onViewCreated(final View view, Bundle saved) {
        super.onViewCreated(view, saved);
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    @Override
    public void onGlobalLayout() {

        totalCardView.getLayoutParams().height = totalCardView.getWidth() / 3;
        viewPager.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.circle_counter_size);
        chart.getLayoutParams().height = chart.getWidth() / 2;

        stickyTotalCardScroll = viewPager.getHeight();
        stickyChartCardScroll = totalCardView.getHeight() + stickyTotalCardScroll;
        historyRecyclerView.getLayoutParams().height = view.getHeight() - chartCardView.getHeight();
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
                PrimeCircleFragment.newInstance(0),
                PrimeCircleFragment.newInstance(1),
                PrimeCircleFragment.newInstance(2)
        );
    }


    private void setNestedScrollView() {
        NestedScrollView nestedScrollView = (NestedScrollView) view.findViewById(R.id.prime_nested);
        nestedScrollView.setOnScrollChangeListener(this);
    }


    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY > stickyTotalCardScroll) {
            if (scrollY > stickyChartCardScroll) {
                setChartTransition(scrollY - stickyChartCardScroll);
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
        PrimeAdapter primeAdapter = new PrimeAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(primeAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.prime_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public void onPageSelected(int position) {
        index = position;
        labelTextView.setText(totalLabels.get(index));
        totalTextView.setText(RealmPrimeItem.getTotalValue(index) + units.get(index));
        cardImageView.setImageDrawable(totalCardImageDrawable.get(index));
        historyAdapter.setCurrentIndex(index);
//                chart.dismiss();
//                setChartValues();
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

        updateTextView = (TextView) view.findViewById(R.id.prime_update);
    }


    private void setRecyclerView() {
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.history_recycler);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        historyRecyclerView.setLayoutManager(linearLayoutManager);
        historyRecyclerView.setHasFixedSize(true);
        historyAdapter = new HistoryAdapter(context);
        historyRecyclerView.setAdapter(historyAdapter);
        historyRecyclerView.setFocusable(true);
    }


    public void setRealmPrimeValue(RealmResults<RealmPrimeItem> results) {


        int length = results.size();
        chartLabels = new String[length];
        chartValues = new float[3][length];

        for (int i = 0; i < length; i++) {
            RealmPrimeItem realmPrimeItem = results.get(i);

            chartLabels[i] = realmPrimeItem.getCalendar();
            chartValues[0][i] = realmPrimeItem.getStep();
            chartValues[1][i] = realmPrimeItem.getCalorie() * 45;
            chartValues[2][i] = realmPrimeItem.getDistance();
        }


        totalTextView.setText(RealmPrimeItem.getTotalValue(index) + units.get(index));
        setCircleValue(results.last());
        setChartValues();
        historyAdapter.add(results);
    }


    private void setChartValues() {
        LineSet dataSet = new LineSet(chartLabels, chartValues[index]);
        dataSet.setColor(ContextCompat.getColor(context, R.color.chart_line))
                .setFill(ContextCompat.getColor(context, R.color.chart_fill))
                .setDotsColor(ContextCompat.getColor(context, R.color.chart_dots))
                .setThickness(4);

        chart.addData(dataSet);
        chart.show();
    }


    public void setUpdateValue(Calendar calendar) {
        final SimpleDateFormat update = new SimpleDateFormat("최근 업데이트 : dd일  HH : mm");
        updateTextView.setText(update.format(calendar.getTime()));
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


    @DebugLog
    public void setCircleCounterGoalRange() {
        for (PrimeCircleFragment primeCircleFragment : primeCircleFragments) {
            primeCircleFragment.setCircleCounterGoalRange();
        }
    }


    public void setCircleValue(RealmPrimeItem realmPrimeItem) {
        for (PrimeCircleFragment primeCircleFragment : primeCircleFragments) {
            primeCircleFragment.setCircleValue(realmPrimeItem);
        }
    }


    private class PrimeAdapter extends FragmentStatePagerAdapter {

        private final int tabTitles[] = new int[]{R.string.prime_step_title, R.string.prime_calorie_title, R.string.prime_distance_title};
        private final int PAGE_COUNT = tabTitles.length;


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
            return getString(tabTitles[position]);
        }
    }

}
