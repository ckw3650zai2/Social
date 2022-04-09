package com.example.social.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.social.R;
import com.example.social.activities.SettingsActivity;

import java.util.Calendar;


public class PreferenceUtil {

    private static boolean getBooleanSettings(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    @StyleRes
    public static int getGeneralTheme(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getThemeResFromPrefValue(sharedPref.getString("theme", "switch"), context);
    }

    @StyleRes
    private static int getThemeResFromPrefValue(@NonNull String themePrefValue, @NonNull Context context) {
        switch (themePrefValue) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return R.style.AppTheme_Dark;
            case "switch":
                int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        return R.style.AppTheme_Dark;
                    default:
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        return R.style.AppTheme_Light;
                }
            case "light":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return R.style.AppTheme_Light;
        }
    }

    @StyleRes
    public static int getGeneralThemeNoActionBar(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getThemeResFromPrefValueNoActionBar(sharedPref.getString("theme", "switch"), context);
    }

    @StyleRes
    private static int getThemeResFromPrefValueNoActionBar(@NonNull String themePrefValue, @NonNull Context context) {
        switch (themePrefValue) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return R.style.AppTheme_Dark_NoActionBar;
            case "switch":
                int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        return R.style.AppTheme_Dark_NoActionBar;
                    default:
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        return R.style.AppTheme_Light_NoActionBar;
                }
            case "light":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return R.style.AppTheme_Light_NoActionBar;
        }
    }

    public static boolean isDark(@NonNull Context context) {
        int theme = getGeneralTheme(context);
        switch (theme) {
            case R.style.AppTheme_Dark:
                return true;
            case R.style.AppTheme_Light:
            default:
                return false;
        }
    }

    public static int getTextColorPrimary(@NonNull Context context) {
        return getThemeColor(android.R.attr.textColorPrimary, context);
    }

    public static int getTextColorSecondary(Context context) {
        return getThemeColor(android.R.attr.textColorSecondary, context);
    }

    public static int getPrimaryColor(@NonNull Context context) {
        return getThemeColor(R.attr.colorPrimary, context);
    }


    private static int getThemeColor(int themeAttributeId, @NonNull Context context) {
        try {
            TypedValue outValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            boolean wasResolved = theme.resolveAttribute(themeAttributeId, outValue, true);
            if (wasResolved) {
                return ContextCompat.getColor(context, outValue.resourceId);
            } else {
                return Color.BLACK;
            }
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    public static boolean isSevenDays(Context context) {
        return getBooleanSettings(context, SettingsActivity.KEY_SEVEN_DAYS_SETTING, false);
    }

    public static boolean isWeekStartOnSunday(Context context) {
        return getBooleanSettings(context, SettingsActivity.KEY_START_WEEK_ON_SUNDAY, false);
    }

    public static boolean isSummaryLibrary1(Context context) {
        return getBooleanSettings(context, "summary_lib", !showTimes(context));
    }

    public static void setSummaryLibrary(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("summary_lib", value).commit();
    }

    public static void setStartTime(Context context, @NonNull int... times) {
        if (times.length != 3) {
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("start_hour", times[0]);
        editor.putInt("start_minute", times[1]);
        editor.putInt("start_second", times[2]);
        editor.commit();
    }

    @NonNull
    public static int[] getStartTime(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return new int[]{sharedPref.getInt("start_hour", 8), sharedPref.getInt("start_minute", 0), sharedPref.getInt("start_second", 0)};
    }

    public static void setPeriodLength(Context context, int length) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("period_length", length).apply();
    }

    public static int getPeriodLength(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("period_length", 60);
    }

    public static boolean hasStartActivityBeenShown(Context context) {
        return getBooleanSettings(context, "start_activity", false);
    }

    public static void setStartActivityShown(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("start_activity", value).commit();
    }

    public static boolean showTimes(Context context) {
        return getBooleanSettings(context, "show_times", true);
    }

    //Even, odd weeks
    public static boolean isTwoWeeksEnabled(Context context) {
        return getBooleanSettings(context, "two_weeks", false);
    }

    public static void setTermStart(Context context, int year, int month, int day) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt("term_year", year);
        editor.putInt("term_month", month);
        editor.putInt("term_day", day);
        editor.commit();
    }

    @NonNull
    public static Calendar getTermStart(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calendar = Calendar.getInstance();
        int year = sharedPref.getInt("term_year", -999999999);

        if (year == -999999999) {
            setTermStart(context, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            return getTermStart(context);
        }

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, sharedPref.getInt("term_month", 0));
        calendar.set(Calendar.DAY_OF_MONTH, sharedPref.getInt("term_day", 0));

        return calendar;
    }

    public static boolean isEvenWeek(Context context, @NonNull Calendar now) {
        if (isTwoWeeksEnabled(context)) {
            return WeekUtils.isEvenWeek(getTermStart(context), now, isWeekStartOnSunday(context));
        } else
            return true;
    }


    public static boolean isIntelligentAutoFill(Context context) {
        return getBooleanSettings(context, "auto_fill", true);
    }

}
