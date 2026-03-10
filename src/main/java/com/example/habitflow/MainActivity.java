// MainActivity.java
package com.example.habitflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HabitAdapter habitAdapter;
    private List<Habit> habitList;
    private DatabaseHelper db;
    private EditText editTextHabitName;
    private Button buttonAddHabit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewHabits);
        editTextHabitName = findViewById(R.id.editTextHabitName);
        buttonAddHabit = findViewById(R.id.buttonAddHabit);

        habitList = db.getAllHabits();
        habitAdapter = new HabitAdapter(habitList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(habitAdapter);

        buttonAddHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextHabitName.getText().toString();
                if (!name.isEmpty()) {
                    db.addHabit(new Habit(name));
                    refreshHabits();
                    editTextHabitName.setText("");
                    Toast.makeText(MainActivity.this, "Habit added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simple logout - in real app, clear session
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    public void refreshHabits() {
        habitList.clear();
        habitList.addAll(db.getAllHabits());
        habitAdapter.notifyDataSetChanged();
    }
}