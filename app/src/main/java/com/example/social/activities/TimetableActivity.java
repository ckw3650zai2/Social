package com.example.social.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.example.social.R;
import com.example.social.adapters.FragmentsTabAdapter;
import com.example.social.fragments.WeekdayFragment;
import com.example.social.profiles.ProfileManagement;
import com.example.social.utils.AlertDialogsHelper;
import com.example.social.utils.DbHelper;
import com.example.social.utils.PreferenceUtil;

import java.util.Calendar;
import java.util.List;


public class TimetableActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentsTabAdapter adapter;
    private ViewPager viewPager;

    private static final int showNextDayAfterSpecificHour = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralThemeNoActionBar(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        ProfileManagement.initProfiles(this);

        initAll();
    }


    private void initAll() {
        initSpinner();

        setupWeeksTV();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerview = navigationView.getHeaderView(0);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setupFragments();
        setupCustomDialog();
    }

    private boolean dontfire = true;

    private void initSpinner() {
        //Set Profiles
        Spinner parentSpinner = findViewById(R.id.profile_spinner);

        if (ProfileManagement.isMoreThanOneProfile()) {
            parentSpinner.setVisibility(View.VISIBLE);
            dontfire = true;
            List<String> list = ProfileManagement.getProfileListNames();
            list.add(getString(R.string.profiles_edit));
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            parentSpinner.setAdapter(dataAdapter);
            parentSpinner.setSelection(ProfileManagement.getSelectedProfilePosition());
            parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                    if (dontfire) {
                        dontfire = false;
                        return;
                    }

                    String item = parent.getItemAtPosition(position).toString();
                    if (item.equals(getString(R.string.profiles_edit))) {
                        Intent intent = new Intent(getBaseContext(), com.example.social.activities.ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //Change profile position
                        ProfileManagement.setSelectedProfile(position);
                        startActivity(new Intent(getBaseContext(), TimetableActivity.class));
                        finish();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            parentSpinner.setVisibility(View.GONE);
        }
    }

    private void setupWeeksTV() {
        TextView weekView = findViewById(R.id.main_week_tV);
        if (PreferenceUtil.isTwoWeeksEnabled(this)) {
            weekView.setVisibility(View.VISIBLE);
            if (PreferenceUtil.isEvenWeek(this, Calendar.getInstance()))
                weekView.setText(R.string.even_week);
            else
                weekView.setText(R.string.odd_week);
        } else
            weekView.setVisibility(View.GONE);
    }

    private void setupFragments() {
        adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        WeekdayFragment mondayFragment = new WeekdayFragment(WeekdayFragment.KEY_MONDAY_FRAGMENT);
        WeekdayFragment tuesdayFragment = new WeekdayFragment(WeekdayFragment.KEY_TUESDAY_FRAGMENT);
        WeekdayFragment wednesdayFragment = new WeekdayFragment(WeekdayFragment.KEY_WEDNESDAY_FRAGMENT);
        WeekdayFragment thursdayFragment = new WeekdayFragment(WeekdayFragment.KEY_THURSDAY_FRAGMENT);
        WeekdayFragment fridayFragment = new WeekdayFragment(WeekdayFragment.KEY_FRIDAY_FRAGMENT);
        WeekdayFragment saturdayFragment = new WeekdayFragment(WeekdayFragment.KEY_SATURDAY_FRAGMENT);
        WeekdayFragment sundayFragment = new WeekdayFragment(WeekdayFragment.KEY_SUNDAY_FRAGMENT);

        boolean startOnSunday = PreferenceUtil.isWeekStartOnSunday(this);
        boolean showWeekend = PreferenceUtil.isSevenDays(this);

        if (!startOnSunday) {
            adapter.addFragment(mondayFragment, getResources().getString(R.string.monday));
            adapter.addFragment(tuesdayFragment, getResources().getString(R.string.tuesday));
            adapter.addFragment(wednesdayFragment, getResources().getString(R.string.wednesday));
            adapter.addFragment(thursdayFragment, getResources().getString(R.string.thursday));
            adapter.addFragment(fridayFragment, getResources().getString(R.string.friday));

            if (showWeekend) {
                adapter.addFragment(saturdayFragment, getResources().getString(R.string.saturday));
                adapter.addFragment(sundayFragment, getResources().getString(R.string.sunday));
            }
        } else {
            adapter.addFragment(sundayFragment, getResources().getString(R.string.sunday));
            adapter.addFragment(mondayFragment, getResources().getString(R.string.monday));
            adapter.addFragment(tuesdayFragment, getResources().getString(R.string.tuesday));
            adapter.addFragment(wednesdayFragment, getResources().getString(R.string.wednesday));
            adapter.addFragment(thursdayFragment, getResources().getString(R.string.thursday));

            if (showWeekend) {
                adapter.addFragment(fridayFragment, getResources().getString(R.string.friday));
                adapter.addFragment(saturdayFragment, getResources().getString(R.string.saturday));
            }
        }


        viewPager.setAdapter(adapter);

        int day = getFragmentChoosingDay();
        if (startOnSunday) {
            viewPager.setCurrentItem(day - 1, true);
        } else {
            viewPager.setCurrentItem(day == 1 ? 6 : day - 2, true);
        }

        tabLayout.setupWithViewPager(viewPager);
    }

    private int getFragmentChoosingDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        //If its after 20 o'clock, show the next day
        if (hour >= showNextDayAfterSpecificHour) {
            day++;
        }

        if (day > 7) { //Calender.Saturday
            day = day - 7; //1 = Calendar.Sunday, 2 = Calendar.Monday etc.
        }

        boolean startOnSunday = PreferenceUtil.isWeekStartOnSunday(this);
        boolean showWeekend = PreferenceUtil.isSevenDays(this);

        //If Saturday/Sunday are hidden, switch to Monday
        if ((!startOnSunday && !showWeekend) && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) {
            day = Calendar.MONDAY;
        } else if ((startOnSunday && !showWeekend) && (day == Calendar.FRIDAY || day == Calendar.SATURDAY)) {
            day = Calendar.SUNDAY;
        }

        return day;
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
        AlertDialogsHelper.getAddSubjectDialog(new DbHelper(this), TimetableActivity.this, alertLayout, adapter, viewPager);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        ProfileManagement.resetSelectedProfile();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.timetable_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(TimetableActivity.this, com.example.social.activities.SettingsActivity.class);
            startActivity(settings);
            finish();
        } else if (item.getItemId() == R.id.action_profiles) {
            Intent intent = new Intent(getBaseContext(), com.example.social.activities.ProfileActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.settings) {
            Intent settings = new Intent(TimetableActivity.this, com.example.social.activities.SettingsActivity.class);
            startActivity(settings);
            finish();
        } else if (itemId == R.id.summary) {
            Intent teacher = new Intent(TimetableActivity.this, com.example.social.activities.SummaryActivity.class);
            startActivity(teacher);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
