package com.rainbow.kam.bt_scanner.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.rainbow.kam.bt_scanner.Adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.R;

/**
 * Created by sion on 2015-10-22.
 */
public class DetailActivity extends AppCompatActivity {
    ExpandableListView expandableListView;
    SimpleExpandableListAdapter simpleExpandableListAdapter;
    ExpandableListView.OnChildClickListener onChildClickListener;
    int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        position =getIntent().getIntExtra("position",0);

        this.simpleExpandableListAdapter = DeviceAdapter.simpleExpandableListAdapter[position];
        this.onChildClickListener = DeviceAdapter.onChildClickListener[position];

        expandableListView = (ExpandableListView) findViewById(R.id.gatt_services_list);
        if (simpleExpandableListAdapter == null || onChildClickListener == null) {
            Log.e("DetailActivity", "hasNull");
        } else {
            expandableListView.setAdapter(simpleExpandableListAdapter);
            simpleExpandableListAdapter.notifyDataSetChanged();
            expandableListView.setOnChildClickListener(onChildClickListener);
        }
    }
}
