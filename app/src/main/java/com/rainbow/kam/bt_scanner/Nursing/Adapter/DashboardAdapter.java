//package com.rainbow.kam.bt_scanner.Nursing.Adapter;
//
//import android.app.Activity;
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.rainbow.kam.bt_scanner.R;
//
//import java.util.ArrayList;
//
///**
// * Created by sion on 2015-11-23.
// */
//public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    //태그
//    private static final String TAG = "DashboardAdapter";
//
//    private ArrayList<DashboardItem> dashboardItems; //어댑터에 적용시킬 틀
//    private Context context; //컨택스트
//    private Activity activity; //액티비티
//    private View view; //SnackBar대비 뷰
//    Dash dash;
//
//    public DashboardAdapter(ArrayList<DashboardItem> dashboardItems, Activity activity, Context context, View view) { //초기화
//        this.dashboardItems = dashboardItems;
//        this.activity = activity;
//        this.context = context;
//        this.view = view;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//        Dash dash;
//        View root;
//        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//        root = layoutInflater.inflate(R.layout.dashboard_data_item, parent, false);
//        dash = new Dash(root);
//        return dash;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
////        //뷰홀더 적용
//
//
//        Log.e(TAG,"onBindViewHolder");
//        dash = (Dash) holder;
//        dash.bindViews(dashboardItems.get(position));
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return dashboardItems.size();
//    }
//
//
//    private class Dash extends RecyclerView.ViewHolder { //뷰 초기화
//
//
//        private TextView info, calendar;
//
//
//        public Dash(View itemView) {
//            super(itemView);
//
//            info = (TextView) itemView.findViewById(R.id.dashboard_info);
//            calendar = (TextView) itemView.findViewById(R.id.dashboard_calendar);
//
//        }
//
//        public void bindViews(DashboardItem dashboardItem) {
//            //각각의 뷰 속성 적용
//            info.setText(dashboardItem.getStep());
//            calendar.setText(dashboardItem.getCalendar());
//        }
//    }
//}
package com.rainbow.kam.bt_scanner.Nursing.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-23.
 */
public class DashboardAdapter extends BaseAdapter {
    //태그
    private static final String TAG = "DashboardAdapter";

    private ArrayList<DashboardItem> dashboardItems; //어댑터에 적용시킬 틀
    private Context context; //컨택스트
    private Activity activity; //액티비티
    private View view; //SnackBar대비 뷰


    public DashboardAdapter(ArrayList<DashboardItem> dashboardItems, Activity activity, Context context, View view) { //초기화
        this.dashboardItems = dashboardItems;
        this.activity = activity;
        this.context = context;
        this.view = view;
    }

    private Toast mToast;


    @Override
    public int getCount() {
        return dashboardItems.size();
    }

    @Override
    public DashboardItem getItem(int position) {
        return dashboardItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(parent.getContext(), R.layout.dashboard_data_item, null);
        ((TextView) convertView.findViewById(R.id.dashboard_info)).setText(String.valueOf(dashboardItems.get(position).getStep()));
        ((TextView) convertView.findViewById(R.id.dashboard_calendar)).setText(dashboardItems.get(position).getCalendar());

        return convertView;
    }
}
