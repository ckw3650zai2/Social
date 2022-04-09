package com.example.social.utils;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.example.social.R;
import com.example.social.adapters.FragmentsTabAdapter;
import com.example.social.fragments.WeekdayFragment;
import com.example.social.model.Week;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.views.picker.RGBPickerView;

public class AlertDialogsHelper {
    //TODO: Rewrite Dialogs to and returning a dialog object, without activity

    public static void getEditSubjectDialog(com.example.social.utils.DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull Runnable runOnSafe, @NonNull final Week week) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subject_dialog);
        editTextHashs.put(R.string.subject, subject);
        final EditText type = alertLayout.findViewById(R.id.type_dialog);
//        editTextHashs.put(R.string.type, type);
        final EditText room = alertLayout.findViewById(R.id.room_dialog);
//        editTextHashs.put(R.string.room, room);
        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        final TextView from_hour = alertLayout.findViewById(R.id.from_hour);
        final TextView to_hour = alertLayout.findViewById(R.id.to_hour);
        final Button select_color = alertLayout.findViewById(R.id.select_color);
        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(week.getColor(), Color.WHITE, Color.BLACK));

        subject.setText(week.getSubject());
        type.setText(week.getType());
        room.setText(week.getRoom());
        from_time.setText(WeekUtils.localizeTime(activity, week.getFromTime()));
        to_time.setText(WeekUtils.localizeTime(activity, week.getToTime()));
        from_hour.setText("" + WeekUtils.getMatchingScheduleBegin(week.getFromTime(), activity));
        to_hour.setText("" + WeekUtils.getMatchingScheduleEnd(week.getToTime(), activity));
        select_color.setBackgroundColor(week.getColor() != 0 ? week.getColor() : Color.WHITE);

        from_time.setOnClickListener(v -> {
            int mHour = Integer.parseInt(week.getFromTime().substring(0, week.getFromTime().indexOf(":")));
            int mMinute = Integer.parseInt(week.getFromTime().substring(week.getFromTime().indexOf(":") + 1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        from_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setFromTime(newTime);
                        from_hour.setText("" + WeekUtils.getMatchingScheduleBegin(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleBegin(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute), activity);
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        to_time.setOnClickListener(v -> {
            int mHour = Integer.parseInt(week.getToTime().substring(0, week.getToTime().indexOf(":")));
            int mMinute = Integer.parseInt(week.getToTime().substring(week.getToTime().indexOf(":") + 1));
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute1) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        to_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setToTime(newTime);
                        to_hour.setText("" + WeekUtils.getMatchingScheduleEnd(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleEnd(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1), activity);
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        from_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Integer.parseInt(from_hour.getText().toString()));
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                        week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                        from_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        to_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Integer.parseInt(to_hour.getText().toString()));
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                        week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                        to_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor())
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (type.getText().toString().trim().isEmpty())
                                        type.setText(w.getType());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (type.getText().toString().trim().isEmpty())
                            type.setText(w.getType());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.edit_subject);
        alert.setCancelable(false);
        final Button cancel = alertLayout.findViewById(R.id.cancel);
        final Button save = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            type.getText().clear();
            room.getText().clear();
            from_time.setText(R.string.select_start_time);
            to_time.setText(R.string.select_end_time);
            from_hour.setText(R.string.lesson);
            to_hour.setText(R.string.lesson);
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            from_hour.setText(R.string.lesson);
            to_hour.setText(R.string.lesson);
            dialog.dismiss();
        });

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) || TextUtils.isEmpty(type.getText()) || TextUtils.isEmpty(room.getText())) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            } else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                week.setSubject(subject.getText().toString());
                week.setType(type.getText().toString());
                week.setRoom(room.getText().toString());
                week.setColor(buttonColor.getColor());
                dbHelper.updateWeek(week);
                runOnSafe.run();
                dialog.dismiss();
            }
        });
    }

    public static void getAddSubjectDialog(com.example.social.utils.DbHelper dbHelper, @NonNull final AppCompatActivity activity, @NonNull final View alertLayout, @NonNull final FragmentsTabAdapter adapter, @NonNull final ViewPager viewPager) {
        final HashMap<Integer, EditText> editTextHashs = new HashMap<>();
        final EditText subject = alertLayout.findViewById(R.id.subject_dialog);
        subject.requestFocus();
        editTextHashs.put(R.string.subject, subject);
        final EditText type = alertLayout.findViewById(R.id.type_dialog);
        editTextHashs.put(R.string.type, type);
        final EditText room = alertLayout.findViewById(R.id.room_dialog);
        editTextHashs.put(R.string.room, room);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.7f
        );

        final TextView from_time = alertLayout.findViewById(R.id.from_time);
        from_time.setLayoutParams(params);
        final TextView to_time = alertLayout.findViewById(R.id.to_time);
        to_time.setLayoutParams(params);
        final TextView from_hour = alertLayout.findViewById(R.id.from_hour);
        from_hour.setLayoutParams(params);
        final TextView to_hour = alertLayout.findViewById(R.id.to_hour);
        to_hour.setLayoutParams(params);

        if (PreferenceUtil.showTimes(activity)) {
            from_time.setVisibility(View.VISIBLE);
            to_time.setVisibility(View.VISIBLE);
            from_hour.setVisibility(View.GONE);
            to_hour.setVisibility(View.GONE);
        } else {
            from_time.setVisibility(View.GONE);
            to_time.setVisibility(View.GONE);
            from_hour.setVisibility(View.VISIBLE);
            to_hour.setVisibility(View.VISIBLE);
        }

        from_hour.setText(R.string.select_start_time);
        to_hour.setText(R.string.select_end_time);

        final Button select_color = alertLayout.findViewById(R.id.select_color);

        final Week week = new Week();

        from_time.setOnClickListener(v -> {
            int mHour, mMinute;
            try {
                String time = from_time.getText().toString();
                mHour = Integer.parseInt(time.substring(0, time.indexOf(":")));
                mMinute = Integer.parseInt(time.substring(time.indexOf(":") + 1));
            } catch (Exception ignore) {
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        from_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setFromTime(newTime);
                        from_hour.setText("" + WeekUtils.getMatchingScheduleBegin(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleBegin(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute), activity);
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, mHour, mMinute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        to_time.setOnClickListener(v -> {
            int hour, minute;
            try {
                String time = WeekUtils.getMatchingTimeEnd(WeekUtils.getMatchingScheduleBegin(from_time.getText().toString(), activity), activity);
                hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
                minute = Integer.parseInt(time.substring(time.indexOf(":") + 1));
            } catch (Exception ignore) {
                final Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                    (view, hourOfDay, minute1) -> {
                        String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        to_time.setText(WeekUtils.localizeTime(activity, newTime));
                        week.setToTime(newTime);
                        to_hour.setText("" + WeekUtils.getMatchingScheduleEnd(newTime, activity));
                        try {
                            int value = WeekUtils.getMatchingScheduleEnd(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1), activity);
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    }, hour, minute, DateFormat.is24HourFormat(activity));
            timePickerDialog.setTitle(R.string.choose_time);
            timePickerDialog.show();
        });

        from_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            try {
                numberPicker.setValue(Integer.parseInt(from_hour.getText().toString()));
            } catch (Exception ignore) {
            }
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                        week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                        from_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(to_hour.getText().toString()) < value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                                week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                                to_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        to_hour.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(activity);
            numberPicker.setMaxValue(15);
            numberPicker.setMinValue(1);
            try {
                numberPicker.setValue(Integer.parseInt(from_hour.getText().toString()) + 1);
            } catch (Exception ignore) {
            }
            new MaterialDialog.Builder(activity)
                    .customView(numberPicker, false)
                    .positiveText(R.string.select)
                    .onPositive((vi, w) -> {
                        int value = numberPicker.getValue();
                        to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(value, activity)));
                        week.setToTime(WeekUtils.getMatchingTimeEnd(value, activity));
                        to_hour.setText("" + value);
                        try {
                            if (Integer.parseInt(from_hour.getText().toString()) > value && PreferenceUtil.isIntelligentAutoFill(activity)) {
                                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(value, activity)));
                                week.setFromTime(WeekUtils.getMatchingTimeBegin(value, activity));
                                from_hour.setText("" + value);
                            }
                        } catch (Exception ignore) {
                        }
                    })
                    .show();
        });

        select_color.setOnClickListener(v -> new ColorPickerDialog()
                .withColor(((ColorDrawable) select_color.getBackground()).getColor()) // the default / initial color
                .withTitle(activity.getString(R.string.choose_color))
                .withTheme(PreferenceUtil.getGeneralTheme(activity))
                .withCornerRadius(16)
                .withAlphaEnabled(false)
                .withListener((dialog, color) -> {
                    // a color has been picked; use it
                    select_color.setBackgroundColor(color);
                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(color, Color.WHITE, Color.BLACK));
                })
                .clearPickers()
                .withPresets(ColorPalette.PRIMARY_COLORS)
                .withPicker(RGBPickerView.class)
                .show(activity.getSupportFragmentManager(), "colorPicker"));

        subject.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // the user is done typing.
                            //AutoFill other fields
                            for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                                if (w.getSubject().equalsIgnoreCase(v.getText().toString())) {
                                    if (type.getText().toString().trim().isEmpty())
                                        type.setText(w.getType());
                                    if (room.getText().toString().trim().isEmpty())
                                        room.setText(w.getRoom());
                                    select_color.setBackgroundColor(w.getColor());
                                    select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                                }
                            }

                            return true;
                        }
                    }
                    return false;
                }
        );
        subject.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                for (Week w : WeekUtils.getAllWeeks(dbHelper)) {
                    if (w.getSubject().equalsIgnoreCase(((EditText) v).getText().toString())) {
                        if (type.getText().toString().trim().isEmpty())
                            type.setText(w.getType());
                        if (room.getText().toString().trim().isEmpty())
                            room.setText(w.getRoom());
                        select_color.setBackgroundColor(w.getColor());
                        select_color.setTextColor(ColorPalette.pickTextColorBasedOnBgColorSimple(w.getColor(), Color.WHITE, Color.BLACK));
                    }
                }
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.add_subject);
        alert.setCancelable(false);
        Button cancel = alertLayout.findViewById(R.id.cancel);
        Button submit = alertLayout.findViewById(R.id.save);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();

        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

                String key = ((WeekdayFragment) adapter.getItem(viewPager.getCurrentItem())).getKey();
                ArrayList<Week> weeks = dbHelper.getWeek(key);
                int valueNew = 1;
                if (weeks.size() > 0) {
                    valueNew = WeekUtils.getMatchingScheduleEnd(weeks.get(weeks.size() - 1).getToTime(), activity) + 1;
                }
                from_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeBegin(valueNew, activity)));
                week.setFromTime(WeekUtils.getMatchingTimeBegin(valueNew, activity));
                from_hour.setText("" + valueNew);
                to_time.setText(WeekUtils.localizeTime(activity, WeekUtils.getMatchingTimeEnd(valueNew, activity)));
                week.setToTime(WeekUtils.getMatchingTimeEnd(valueNew, activity));
                to_hour.setText("" + valueNew);

                dialog.show();

        });

        cancel.setOnClickListener(v -> {
            subject.getText().clear();
            type.getText().clear();
            room.getText().clear();
            from_time.setText(R.string.select_start_time);
            to_time.setText(R.string.select_end_time);
            from_hour.setText(R.string.select_start_time);
            to_hour.setText(R.string.select_end_time);
            select_color.setBackgroundColor(Color.WHITE);
            subject.requestFocus();
            dialog.dismiss();
        });

        submit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(subject.getText()) /*|| TextUtils.isEmpty(type.getText()) || TextUtils.isEmpty(room.getText())*/) {
                for (Map.Entry<Integer, EditText> entry : editTextHashs.entrySet()) {
                    if (TextUtils.isEmpty(entry.getValue().getText())) {
                        entry.getValue().setError(activity.getResources().getString(entry.getKey()) + " " + activity.getResources().getString(R.string.field_error));
                        entry.getValue().requestFocus();
                    }
                }
            } else if (!from_time.getText().toString().matches(".*\\d+.*") || !to_time.getText().toString().matches(".*\\d+.*")) {
                Snackbar.make(alertLayout, R.string.time_error, Snackbar.LENGTH_LONG).show();
            } else {
                ColorDrawable buttonColor = (ColorDrawable) select_color.getBackground();
                week.setSubject(subject.getText().toString());
                week.setFragment(((WeekdayFragment) adapter.getItem(viewPager.getCurrentItem())).getKey());
                week.setType(type.getText().toString());
                week.setRoom(room.getText().toString());
                week.setColor(buttonColor.getColor());
                dbHelper.insertWeek(week);
                adapter.notifyDataSetChanged();
                cancel.performClick();
            }
        });
    }


    public static void getDeleteDialog(@NonNull Context context, @NonNull Runnable runnable, String deleteSubject) {
        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.are_you_sure))
                .content(context.getString(R.string.delete_content, deleteSubject))
                .positiveText(context.getString(R.string.yes))
                .onPositive((dialog, which) -> {
                    runnable.run();
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .negativeText(context.getString(R.string.no))
                .show();
    }
}
