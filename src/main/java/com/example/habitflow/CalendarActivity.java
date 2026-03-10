// CalendarActivity.java
package com.example.habitflow;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvSelectedDateStatus;
    private Spinner spinnerHabits;
    private DatabaseHelper db;
    private List<Habit> habitList;
    private Habit selectedHabit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = new DatabaseHelper(this);

        calendarView = findViewById(R.id.calendarView);
        tvSelectedDateStatus = findViewById(R.id.tvSelectedDateStatus);
        spinnerHabits = findViewById(R.id.spinnerHabits);

        loadHabitsIntoSpinner();

        // Default to first habit
        if (!habitList.isEmpty()) {
            selectedHabit = habitList.get(0);
            highlightCompletedDays(selectedHabit.getId());
        }

        spinnerHabits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHabit = habitList.get(position);
                highlightCompletedDays(selectedHabit.getId());
                updateDateStatus(calendarView.getDate());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            long selectedDateMillis = cal.getTimeInMillis();
            updateDateStatus(selectedDateMillis);
        });

        // Show today's status by default
        updateDateStatus(System.currentTimeMillis());
    }

    private void loadHabitsIntoSpinner() {
        habitList = db.getAllHabits();
        List<String> habitNames = new ArrayList<>();
        for (Habit h : habitList) {
            habitNames.add(h.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, habitNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHabits.setAdapter(adapter);
    }

    private void highlightCompletedDays(int habitId) {
        // Android CalendarView doesn't natively support decorators like MaterialCalendarView
        // For simple version → we just show info on click
        // Advanced alternative: use third-party library (see notes below)
        Toast.makeText(this, "Completed days highlighted in status on tap", Toast.LENGTH_SHORT).show();
    }

    private void updateDateStatus(long dateMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(new Date(dateMillis));

        if (selectedHabit == null) {
            tvSelectedDateStatus.setText("No habit selected");
            return;
        }

        boolean done = db.isChecked(selectedHabit.getId(), dateStr);

        String status = done
                ? "✓ Completed on " + dateStr
                : "Not completed on " + dateStr;

        tvSelectedDateStatus.setText(status);
        tvSelectedDateStatus.setTextColor(
                done ? getResources().getColor(android.R.color.holo_green_dark)
                        : getResources().getColor(android.R.color.holo_red_dark)
        );
    }
}