package com.rainbow.kam.bt_scanner.NotInUse;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.rainbow.kam.bt_scanner.adapter.detail.DetailAdapter;
import com.rainbow.kam.bt_scanner.adapter.detail.ServiceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BLEGattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class DetailServiceFragmentDeprecated extends Fragment {

    private Activity activity;

    private View view;

    private RecyclerView recyclerView;
    private DetailAdapter adapter;
    private ArrayList<ServiceItem> serviceItemArrayList = new ArrayList<ServiceItem>();
    private Animation animation;
    private View animView;
    private int position;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("service", "onCreateView");
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_detail_service, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.detail_service_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new DetailAdapter(serviceItemArrayList, activity, activity, true);
        recyclerView.setAdapter(adapter);
        return view;
    }

    public void clearAdapter() {
        adapter.clearList(0);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void addService(BluetoothGattService bluetoothGattService) {
//        for (int i = 0; i < serviceItemArrayList.size(); i++) {
        Log.e("frag", "add Service");
//            if (serviceItemArrayList.get(i).getBluetoothGattService() == null) {
        String uuid = bluetoothGattService.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BLEGattAttributes.resolveServiceName(uuid);
        String type = (bluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? "primary" : "Secondary";
        serviceItemArrayList.add(new ServiceItem(name, uuid, type, bluetoothGattService));
//            }
//        }
        adapter = new DetailAdapter(serviceItemArrayList, activity, activity, true);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public BluetoothGattService getService(int index) {
        return serviceItemArrayList.get(index).getBluetoothGattService();
    }

    public void noti() {
        adapter.notifyDataSetChanged();
    }

    public void startTransition(int position) {
        recyclerView.smoothScrollToPosition(0);
        DetailAdapter.ServiceViewHolder transitionViewHolder = adapter.getServiceViewHolder();
        animView = transitionViewHolder.getView();
        this.position = position;
        Log.e("animation", "start position : " + position);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setInterpolator(new AccelerateInterpolator());
        Bundle bundle = transitionViewHolder.getParams();
        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, (-1.0f * position));
//        animation = new TranslateAnimation(bundle.getFloat("X"), recyclerView.getX(), bundle.getFloat("Y"), recyclerView.getY());
        animation.setDuration(1000);
        animation.setFillAfter(false);
        animView.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resetTransition();
            }
        }, animation.getDuration());
    }

    public void resetTransition() {
        try {
            DetailAdapter.ServiceViewHolder transitionViewHolder = adapter.getServiceViewHolder();
            animView = transitionViewHolder.getView();
            Log.e("animation", "reset position : " + position);
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.setInterpolator(new AccelerateInterpolator());
            Bundle bundle = transitionViewHolder.getParams();
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, (-1.0f * position),
                    Animation.RELATIVE_TO_SELF, 0.0f);
//        animation = new TranslateAnimation(bundle.getFloat("X"), recyclerView.getX(), bundle.getFloat("Y"), recyclerView.getY());
            animation.setDuration(1000);
            animation.setFillAfter(true);
            animView.startAnimation(animation);
        } catch (Exception e) {
            return;
        }

    }
}
