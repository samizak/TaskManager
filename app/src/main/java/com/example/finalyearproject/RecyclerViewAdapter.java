package com.example.finalyearproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.RecyclerViewAdapter.MyViewHolder;
import com.example.finalyearproject.data.ToDoModel;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final Activity activity;
    private ArrayList<ToDoModel> taskList;

    public RecyclerViewAdapter(Activity activity, ArrayList<ToDoModel> taskList) {
        this.activity = activity;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View taskView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_row_layout, parent, false);
        return new MyViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel taskModel = taskList.get(position);
        String taskName = taskModel.getTaskName();

        // Set the Task Name
        holder.taskNameTextView.setText(taskName);

        // Checkbox listener
        holder.taskCompletedCheckBox.setOnCheckedChangeListener((checkbox, isChecked) ->{
            Toast.makeText(activity, "Task completed: " + isChecked, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskNameTextView;
        private final CheckBox taskCompletedCheckBox;

        public MyViewHolder(@NonNull View view) {
            super(view);

            taskNameTextView = view.findViewById(R.id.taskName);
            taskCompletedCheckBox = view.findViewById(R.id.taskCompleted);
        }
    }
}
