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
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;

import static android.support.v7.widget.RecyclerView.OnScrollListener;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeFragment extends Fragment
        implements ViewTreeObserver.OnGlobalLayoutListener,
        NestedScrollView.OnScrollChangeListener,
        ViewPager.OnPageChangeListener {

    private Context context;
    private View view;

    private static final int INDEX_STEP = PrimeHelper.INDEX_STEP;
    private static final int INDEX_CALORIE = PrimeHelper.INDEX_CALORIE;
    private static final int INDEX_DISTANCE = PrimeHelper.INDEX_DISTANCE;

    private int index;

    private final int[] total = new int[3];

    private final int[] unit = {R.string.prime_step, R.string.prime_calorie, R.string.prime_distance};

    private CardView totalCardView;
    private TextView labelTextView, totalTextView;
    private final int[] label = {R.string.prime_total_step, R.string.prime_total_calorie, R.string.prime_total_distance};
    private ImageView cardImageView;
    private final int[] cardImage = {R.drawable.step_wallpaper, R.drawable.calorie_wallpaper, R.drawable.distance_wallpaper};
    private final Drawable[] cardImageDrawable = new Drawable[cardImage.length];

    private ViewPager viewPager;
    private final PrimeCircleFragment[] primeCircleFragments = new PrimeCircleFragment[3];

    private CardView chartCardView;
    private LineChartView chart;
    private String[] chartLabels;
    private float[][] chartValues;

    private TextView updateTextView;

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;

    private int scrollHeight, viewPagerHeight;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            this.context = context;
            cardImageDrawable[0] = ContextCompat.getDrawable(context, cardImage[0]);
            cardImageDrawable[1] = ContextCompat.getDrawable(context, cardImage[1]);
            cardImageDrawable[2] = ContextCompat.getDrawable(context, cardImage[2]);
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
        viewPagerHeight = viewPager.getHeight();
        scrollHeight = totalCardView.getHeight() + viewPagerHeight + 100;
        historyRecyclerView.getLayoutParams().height = view.getHeight() - chartCardView.getHeight();
    }


    private void setTotalCardView() {
        totalCardView = (CardView) view.findViewById(R.id.prime_card);
        labelTextView = (TextView) view.findViewById(R.id.prime_label);
        labelTextView.setText(getString(label[index]));
        totalTextView = (TextView) view.findViewById(R.id.prime_value);

        cardImageView = (ImageView) view.findViewById(R.id.prime_card_image);
        cardImageView.setImageDrawable(cardImageDrawable[index]);
    }


    private void setFragments() {
        primeCircleFragments[INDEX_STEP] = PrimeCircleFragment.newInstance(INDEX_STEP);
        primeCircleFragments[INDEX_CALORIE] = PrimeCircleFragment.newInstance(INDEX_CALORIE);
        primeCircleFragments[INDEX_DISTANCE] = PrimeCircleFragment.newInstance(INDEX_DISTANCE);
    }


    private void setNestedScrollView() {
        NestedScrollView nestedScrollView = (NestedScrollView) view.findViewById(R.id.prime_nested);
        nestedScrollView.setOnScrollChangeListener(this);
    }


    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY > viewPagerHeight) {
            if (scrollY > scrollHeight) {
                setChartTransition(scrollY - scrollHeight);
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
        labelTextView.setText(getString(label[index]));
        totalTextView.setText(String.valueOf(total[index]) + getString(unit[index]));
        cardImageView.setImageDrawable(cardImageDrawable[index]);
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
        chartCardView = (CardView) view.findViewById(R.id.prime_tab_graph);

        chart = (LineChartView) view.findViewById(R.id.chart1);
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


    public void setRealmPrimeItemValue(RealmResults<RealmPrimeItem> results) {
        total[0] = 0;
        total[1] = 0;
        total[2] = 0;

        int length = results.size();
        chartLabels = new String[length];
        chartValues = new float[3][length];

        for (int i = 0; i < length; i++) {
            RealmPrimeItem realmPrimeItem = results.get(i);
            total[0] += realmPrimeItem.getStep();
            total[1] += realmPrimeItem.getCalorie();
            total[2] += realmPrimeItem.getDistance();

            chartLabels[i] = realmPrimeItem.getCalendar();
            chartValues[0][i] = realmPrimeItem.getStep();
            chartValues[1][i] = realmPrimeItem.getCalorie() * 45;
            chartValues[2][i] = realmPrimeItem.getDistance();
        }


        totalTextView.setText(String.valueOf(total[index]) + getString(unit[index]));
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


    public void setUpdateValue(String updateValue) {
        updateTextView.setText(updateValue);
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


    public void setCircleValue(Bundle bundle) {
        primeCircleFragments[INDEX_STEP].setCircleValue(bundle.getInt(PrimeHelper.KEY_STEP));
        primeCircleFragments[INDEX_CALORIE].setCircleValue(bundle.getInt(PrimeHelper.KEY_CALORIE));
        primeCircleFragments[INDEX_DISTANCE].setCircleValue(bundle.getInt(PrimeHelper.KEY_DISTANCE));
    }


    private class PrimeAdapter extends FragmentStatePagerAdapter {

        private final int tabTitles[] = new int[]{R.string.prime_step_title, R.string.prime_calorie_title, R.string.prime_distance_title};
        private final int PAGE_COUNT = tabTitles.length;


        public PrimeAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return primeCircleFragments[position];
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
