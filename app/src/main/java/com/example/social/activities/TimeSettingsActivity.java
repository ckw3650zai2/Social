package com.example.social.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.social.R;
import com.example.social.fragments.TimeSettingsFragment;
import com.example.social.utils.PreferenceUtil;

public class TimeSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_time);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, new TimeSettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        PreferenceUtil.setStartActivityShown(this, true);
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
