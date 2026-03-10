// HabitAdapter.java (For RecyclerView)
package com.example.habitflow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {
    private List<Habit> habitList;
    private Context context;
    private DatabaseHelper db;

    public HabitAdapter(List<Habit> habitList, Context context) {
        this.habitList = habitList;
        this.context = context;
        this.db = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.textViewHabitName.setText(habit.getName());

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isChecked = db.isChecked(habit.getId(), today);
        holder.checkBoxDone.setChecked(isChecked);

        holder.checkBoxDone.setOnCheckedChangeListener(null); // Prevent reuse issues
        holder.checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            db.addOrUpdateCheckin(habit.getId(), today, isChecked1);
            Toast.makeText(context, isChecked1 ? "Marked done" : "Marked undone", Toast.LENGTH_SHORT).show();
        });

        holder.itemView.setOnClickListener(v -> showEditDeleteDialog(habit));
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    private void showEditDeleteDialog(Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit or Delete Habit");

        final EditText input = new EditText(context);
        input.setText(habit.getName());
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = input.getText().toString();
            if (!newName.isEmpty()) {
                habit.setName(newName);
                db.updateHabit(habit);
                notifyDataSetChanged();
                Toast.makeText(context, "Habit updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            db.deleteHabit(habit.getId());
            habitList.remove(habit);
            notifyDataSetChanged();
            Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewHabitName;
        CheckBox checkBoxDone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHabitName = itemView.findViewById(R.id.textViewHabitName);
            checkBoxDone = itemView.findViewById(R.id.checkBoxDone);
        }
    }
}