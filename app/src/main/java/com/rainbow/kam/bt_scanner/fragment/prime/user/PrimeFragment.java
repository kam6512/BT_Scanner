package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;
import com.rainbow.kam.bt_scanner.adapter.prime.HistoryAdapter;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;
import com.rainbow.kam.bt_scanner.tools.view.CustomViewPager;
import com.rainbow.kam.bt_scanner.tools.view.NestedRecyclerViewManager;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private static final int INDEX_STEP = PrimeHelper.INDEX_STEP;
    private static final int INDEX_CALORIE = PrimeHelper.INDEX_CALORIE;
    private static final int INDEX_DISTANCE = PrimeHelper.INDEX_DISTANCE;
    private int index;

    private Context context;
    private View view;

    private final PrimeCircleFragment[] primeCircleFragments = new PrimeCircleFragment[3];

    private CardView cardView;
    private TextView labelTextView, valueTextView;
    private ImageView cardImageView;

    int[] total = new int[3];

    private final int[] label = {R.string.prime_total_step, R.string.prime_total_calorie, R.string.prime_total_distance};
    private final int[] cardImage = {R.drawable.step_wallpaper, R.drawable.calorie_wallpaper, R.drawable.distance_wallpaper};
    private final Drawable[] cardImageDrawable = new Drawable[cardImage.length];
    private final int[] unit = {R.string.prime_step, R.string.prime_calorie, R.string.prime_distance};


    private HistoryAdapter historyAdapter;


    private LineChartView chart;
    private final String[] mLabels = {"Jan", "Fev", "Mar", "Apr", "Jun", "May", "Jul", "Aug", "Sep"};
    private final float[][] mValues = {{3.5f, 4.7f, 4.3f, 8f, 6.5f, 9.9f, 7f, 8.3f, 7.0f},
            {4.5f, 2.5f, 2.5f, 9f, 4.5f, 9.5f, 5f, 8.3f, 1.8f}};


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            this.context = context;
            historyAdapter = new HistoryAdapter(context);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime, container, false);


        cardImageDrawable[0] = getResources().getDrawable(cardImage[0]);
        cardImageDrawable[1] = getResources().getDrawable(cardImage[1]);
        cardImageDrawable[2] = getResources().getDrawable(cardImage[2]);

        setCardView();
        setFragments();
        setResource();
        setViewPager();
        setNestedScrollView();
        setRecyclerView();


        chart = (LineChartView) view.findViewById(R.id.chart1);

        LineSet dataset = new LineSet(mLabels, mValues[0]);
        dataset.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setThickness(4);
        chart.addData(dataset);
        chart.show();
        return view;
    }


    private void setCardView() {
        cardView = (CardView) view.findViewById(R.id.prime_card);
        labelTextView = (TextView) view.findViewById(R.id.prime_label);
        valueTextView = (TextView) view.findViewById(R.id.prime_value);
        cardImageView = (ImageView) view.findViewById(R.id.prime_card_image);
    }


    private void setFragments() {
        primeCircleFragments[INDEX_STEP] = PrimeCircleFragment.newInstance(INDEX_STEP);
        primeCircleFragments[INDEX_CALORIE] = PrimeCircleFragment.newInstance(INDEX_CALORIE);
        primeCircleFragments[INDEX_DISTANCE] = PrimeCircleFragment.newInstance(INDEX_DISTANCE);
    }


    private void setViewPager() {
        CustomViewPager viewPager = (CustomViewPager) view.findViewById(R.id.prime_viewpager);
        PrimeAdapter primeAdapter = new PrimeAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(primeAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }


            @Override
            public void onPageSelected(int position) {
                historyAdapter.setCurrentIndex(position);
                index = position;
                valueTextView.setText(String.valueOf(total[index]) + getString(unit[index]));
                setResource();
            }


            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.prime_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void setNestedScrollView() {
        NestedScrollView pagerNestedScrollView = (NestedScrollView) view.findViewById(R.id.prime_nested);
        pagerNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                setCardTransition(scrollY);

            }
        });
    }


    private void setRecyclerView() {
        RecyclerView historyRecyclerView = (RecyclerView) view.findViewById(R.id.history_recycler);
        RecyclerView.LayoutManager layoutManager = new NestedRecyclerViewManager(context);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.setNestedScrollingEnabled(false);
        historyRecyclerView.setHasFixedSize(false);
        historyRecyclerView.setAdapter(historyAdapter);
    }


    private void setResource() {
        labelTextView.setText(getString(label[index]));
//        cardImageView.setImageResource(cardImage[index]);
        cardImageView.setImageDrawable(cardImageDrawable[index]);
    }


    public void setTextTotalValue(RealmResults<RealmPrimeItem> results) {
        historyAdapter.add(results);
        total[0] = 0;
        total[1] = 0;
        total[2] = 0;

        for (RealmPrimeItem realmPrimeItem : results) {
            total[0] += realmPrimeItem.getStep();
            total[1] += realmPrimeItem.getCalorie();
            total[2] += realmPrimeItem.getDistance();
        }
        valueTextView.setText(String.valueOf(total[index]) + getString(unit[index]));
    }


    public void setTextFail() {
        if (isVisible()) {
            valueTextView.setText(getString(R.string.prime_access_denial));
        }
    }


    public void setCardTransition(int y) {
        cardView.setTranslationY(y);
    }


    @DebugLog
    public void setCircleCounterGoalRange() {
        for (PrimeCircleFragment primeCircleFragment : primeCircleFragments) {
            primeCircleFragment.setCircleCounterGoalRange();
        }
    }


    public void setCircleValue(Bundle bundle) {

        final int step = bundle.getInt(PrimeHelper.KEY_STEP);
        final int calorie = bundle.getInt(PrimeHelper.KEY_CALORIE);
        final int distance = bundle.getInt(PrimeHelper.KEY_DISTANCE);
        primeCircleFragments[INDEX_STEP].setCircleValue(step);
        primeCircleFragments[INDEX_CALORIE].setCircleValue(calorie);
        primeCircleFragments[INDEX_DISTANCE].setCircleValue(distance);
    }


    private class PrimeAdapter extends FragmentStatePagerAdapter {

        private final int tabTitles[] = new int[]{R.string.prime_step_title, R.string.prime_calorie_title, R.string.prime_distance_title};
        final int PAGE_COUNT = tabTitles.length;


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
