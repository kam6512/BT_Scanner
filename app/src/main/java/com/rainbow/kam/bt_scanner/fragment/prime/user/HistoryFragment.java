package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.HistoryAdapter;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class HistoryFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private Context context;

    private View view;
    private CircleCounter circleCounter;

    private LinearLayout dotsLayout;
    private final int dotsCount = 3;
    private int currentCount = 0;
    private TextView[] dots;

    int step, calorie, distance;

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter = new HistoryAdapter();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime_main_history, container, false);

        setCircleCounter();
        setDotsLayout();
        setRecyclerView();


        return view;
    }


    private void setCircleCounter() {
        circleCounter = (CircleCounter) view.findViewById(R.id.counter);
        circleCounter.setFirstColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setSecondColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setThirdColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
        circleCounter.setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));
        circleCounter.setOnClickListener(this);
        setCurrentCounter(0);
    }


    private void setDotsLayout() {

        dotsLayout = (LinearLayout) view.findViewById(R.id.pagerCountDots);
        dots = new TextView[dotsCount];
        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new TextView(context);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(30);
            dots[i].setTextColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
            dotsLayout.addView(dots[i]);
        }
    }


    private void setRecyclerView() {
        recyclerView = (RecyclerView) view.findViewById(R.id.history_recycler);
        RecyclerView.LayoutManager layoutManager = new WrappingLinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(historyAdapter);
    }


    public void setValue(int value) {
        circleCounter.setValues(value, value, value);
    }


    public void addHistory(RealmResults<RealmPrimeItem> results) {
        historyAdapter.add(results);
        step = results.last().getStep();
        calorie = results.last().getCalorie();
        distance = results.last().getDistance();
        setCurrentCounter(0);
    }


    public void setCurrentCounter(final int position) {
        currentCount = position;
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setTextColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
        }
        dots[position].setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));

        switch (position) {
            case 0:
                circleCounter.setRange(20000);
                circleCounter.setMetricText("걸음");
                setValue(step);
                break;

            case 1:
                circleCounter.setRange(100);
                circleCounter.setMetricText("칼로리");
                setValue(calorie);
                break;

            case 2:
                circleCounter.setRange(10000);
                circleCounter.setMetricText("거리");
                setValue(distance);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        if (currentCount == dotsCount - 1) {
            currentCount = -1;
        }
        setCurrentCounter(++currentCount);
    }


    class WrappingLinearLayoutManager extends LinearLayoutManager {

        public WrappingLinearLayoutManager(Context context) {
            super(context);
        }


        private int[] mMeasuredDimension = new int[2];


        @Override
        public boolean canScrollVertically() {
            return false;
        }


        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                              int widthSpec, int heightSpec) {
            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);

            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);

            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {
                if (getOrientation() == HORIZONTAL) {
                    measureScrapChild(recycler, i,
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            heightSpec,
                            mMeasuredDimension);

                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    measureScrapChild(recycler, i,
                            widthSpec,
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            mMeasuredDimension);

                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }

            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                    width = widthSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            setMeasuredDimension(width, height);
        }


        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                       int heightSpec, int[] measuredDimension) {

            View view = recycler.getViewForPosition(position);
            if (view.getVisibility() == View.GONE) {
                measuredDimension[0] = 0;
                measuredDimension[1] = 0;
                return;
            }
            // For adding Item Decor Insets to view
            super.measureChildWithMargins(view, 0, 0);
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(
                    widthSpec,
                    getPaddingLeft() + getPaddingRight() + getDecoratedLeft(view) + getDecoratedRight(view),
                    p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(
                    heightSpec,
                    getPaddingTop() + getPaddingBottom() + getDecoratedTop(view) + getDecoratedBottom(view),
                    p.height);
            view.measure(childWidthSpec, childHeightSpec);

            // Get decorated measurements
            measuredDimension[0] = getDecoratedMeasuredWidth(view) + p.leftMargin + p.rightMargin;
            measuredDimension[1] = getDecoratedMeasuredHeight(view) + p.bottomMargin + p.topMargin;
            recycler.recycleView(view);
        }
    }
}
