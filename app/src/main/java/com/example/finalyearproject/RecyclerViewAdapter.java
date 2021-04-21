package com.example.finalyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.RecyclerViewAdapter.MyViewHolder;
import com.example.finalyearproject.data.ToDoModel;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> implements Filterable {

    private final Activity activity;
    private ArrayList<ToDoModel> taskList;
    private final ArrayList<ToDoModel> filteredTaskList; // Used for search filtering

    public RecyclerViewAdapter(Activity activity, ArrayList<ToDoModel> taskList) {
        this.activity = activity;
        this.taskList = taskList;

        filteredTaskList = new ArrayList<>(taskList);
    }

    private void MoreOptionsPopupListener(@NonNull MyViewHolder holder, View v, int position) {
        PopupMenu popup = new PopupMenu(v.getContext(), holder.moreOptionsImageView);
        popup.inflate(R.menu.task_row_more_options_popup);
        popup.show();

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.taskEdit) EditTaskPopupListener(v, position);
            else if (item.getItemId() == R.id.deleteTask) DeleteTaskPopupListener(v, position);
            return true;
        });
    }

    private void EditTaskPopupListener(View v, int position) {
        ToDoModel taskModel = taskList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("taskID", taskModel.getId());
        bundle.putString("taskName", taskModel.getTaskName());
        bundle.putString("isCompleted", String.valueOf(taskModel.getIsCompleted()));

        AddNewTaskBottomSheet addNewTaskBottomSheet = new AddNewTaskBottomSheet();
        addNewTaskBottomSheet.setArguments(bundle);
        addNewTaskBottomSheet.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "addNewTaskBottomSheet");
    }

    private void DeleteTaskPopupListener(View v, int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
        dialogBuilder.setTitle("Delete?");
        dialogBuilder.setMessage("Are you sure you want to delete the selected Tasks?");

        // Yes Button
        dialogBuilder.setPositiveButton("Yes", (dialog, id_) -> {
            ToDoModel taskModel = taskList.get(position);
            taskList.remove(taskModel);
            this.notifyDataSetChanged();

            taskList.remove(taskModel);
        });

        // No Button
        dialogBuilder.setNegativeButton("No", (dialog, id_) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<ToDoModel> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ToDoModel taskModel : filteredTaskList)
                    if (taskModel.getTaskName().toLowerCase().contains(filterPattern))
                        filteredList.add(taskModel);

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                taskList.clear();
                Iterable<?> arrayList = (Iterable<?>) results.values;

                for (Object obj : arrayList)
                    taskList.add((ToDoModel) obj);

                notifyDataSetChanged();
            }
        };
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

        // More options button Clicked
        holder.moreOptionsImageView.setOnClickListener(v -> MoreOptionsPopupListener(holder, v, position));

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

        private final ImageView moreOptionsImageView;

        private final CheckBox taskCompletedCheckBox;

        public MyViewHolder(@NonNull View view) {
            super(view);

            taskNameTextView = view.findViewById(R.id.taskName);

            moreOptionsImageView = view.findViewById(R.id.moreOptionsImageView);

            taskCompletedCheckBox = view.findViewById(R.id.taskCompleted);
        }
    }
}
