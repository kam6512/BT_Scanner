package com.rainbow.kam.bt_scanner.Nursing.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;

import com.rainbow.kam.bt_scanner.Nursing.Fragment.DashboardFragment;
import com.rainbow.kam.bt_scanner.Nursing.Fragment.StartNursingFragment;
import com.rainbow.kam.bt_scanner.R;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class MainNursingActivity extends AppCompatActivity {


    public static final String TAG = MainNursingActivity.class.getSimpleName();

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DashBoardAdapter dashBoardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);

        toolbar = (Toolbar) findViewById(R.id.nursing_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nursing_nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    menuItem.setChecked(true);
                    switch (menuItem.getItemId()) {
                        case R.id.nursing_dashboard:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_step:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_step", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_calorie:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_calorie", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_distance:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_distance", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_sleep:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_sleep", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_info_user:
                            Snackbar.make(coordinatorLayout, "nursing_info_user", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_info_prime:
                            Snackbar.make(coordinatorLayout, "nursing_info_prime", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_info_goal:
                            Snackbar.make(coordinatorLayout, "nursing_info_goal", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_about_dev:
                            Snackbar.make(coordinatorLayout, "nursing_about_dev", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_about_setting:
                            Snackbar.make(coordinatorLayout, "nursing_about_setting", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_about_about:
                            Snackbar.make(coordinatorLayout, "nursing_about_about", Snackbar.LENGTH_LONG).show();
                            return true;

                        default:
                            return true;
                    }
                }
            });
        }

        tabLayout = (TabLayout) findViewById(R.id.nursing_tabs);

        viewPager = (ViewPager) findViewById(R.id.nursing_viewpager);
        dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(dashBoardAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DashBoardAdapter extends FragmentStatePagerAdapter {

        final int PAGE_COUNT = 5;
        private String tabTitles[] = new String[]{"DASHBOARD", "STEP", "CALORIE", "DISTANCE", "SLEEP"};
        private Context context;

        public DashBoardAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return DashboardFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

}
