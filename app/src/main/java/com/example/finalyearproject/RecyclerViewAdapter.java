package com.example.finalyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.RecyclerViewAdapter.MyViewHolder;
import com.example.finalyearproject.activities.SubTaskActivity;
import com.example.finalyearproject.activities.TaskActivity;
import com.example.finalyearproject.data.SubTaskModel;
import com.example.finalyearproject.data.TaskModel;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This Class is used for Displaying and Updating the RecyclerView for MainTasks
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> implements Filterable {

    private final Activity activity;
    private final ArrayList<TaskModel> taskList;
    private final ArrayList<TaskModel> selectedTasksList = new ArrayList<>();
    private final ArrayList<TaskModel> filteredTaskList; // Used for search filtering

    private boolean isEnable = false;
    private boolean isSelectAll = false;

    private ActionMode actionMode;


    public RecyclerViewAdapter(Activity activity, ArrayList<TaskModel> taskList) {
        this.activity = activity;
        this.taskList = taskList;

        filteredTaskList = new ArrayList<>(taskList);
    }

    //==========================================================================================
    //                                  Multi-Select Menu
    //==========================================================================================
    //region Multi-Select Menu

    /**
     * This method is called when the Select-All Button is pressed
     */
    private void SelectAllButtonAction() {
        isSelectAll = !isSelectAll;
        selectedTasksList.clear();

        // Add all items to the Selected Tasks List
        if (isSelectAll) selectedTasksList.addAll(taskList);
        // Set the title to the number of selected Tasks
        if (actionMode != null) actionMode.setTitle("Selected " + selectedTasksList.size());

        notifyDataSetChanged();
    }

    /**
     * This method is called when the Delete Button is pressed
     */
    private void DeleteMenuButtonAction(View view, ActionMode mode) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
        dialogBuilder.setTitle("Delete?");
        dialogBuilder.setMessage("Are you sure you want to delete the selected Tasks?");

        // Yes Button
        dialogBuilder.setPositiveButton("Yes", (dialog, id) -> {
            for (int i = 0; i < selectedTasksList.size(); i++) {
                TaskModel taskModel = selectedTasksList.get(i);
                taskList.remove(taskModel);
                this.notifyDataSetChanged();

                DatabaseReference mPostReference = TaskActivity.reference.child(taskModel.getId());
                mPostReference.removeValue();
            }

            mode.finish();
        });

        // No Button
        dialogBuilder.setNegativeButton("No", (dialog, id_) -> mode.finish());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
    //endregion


    //==========================================================================================
    //                              More Options Popup functions
    //==========================================================================================
    //region More Options Popup functions

    /**
     * ImageView Listener, used for displaying the MoreOptions popup Menu
     */
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

    /**
     * Menu Button Listener, displays the BottomSheet to edit task when clicked
     */
    private void EditTaskPopupListener(View v, int position) {
        TaskModel taskModel = taskList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("taskID", taskModel.getId());
        bundle.putString("taskName", taskModel.getTaskName());
        bundle.putString("taskDetails", taskModel.getDetails());
        bundle.putString("taskDate", taskModel.getDate());
        bundle.putString("taskTime", taskModel.getTime());
        bundle.putString("taskCompleted", String.valueOf(taskModel.getIsCompleted()));

        AddNewTaskBottomSheet addNewTaskBottomSheet = new AddNewTaskBottomSheet();
        addNewTaskBottomSheet.setArguments(bundle);
        addNewTaskBottomSheet.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "addNewTaskBottomSheet");
    }

    /**
     * Menu Button Listener, displays the delete Dialog to delete the task when clicked
     */
    private void DeleteTaskPopupListener(View v, int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
        dialogBuilder.setTitle("Delete?");
        dialogBuilder.setMessage("Are you sure you want to delete the selected Tasks?");

        // Yes Button
        dialogBuilder.setPositiveButton("Yes", (dialog, id_) -> {
            TaskModel taskModel = taskList.get(position);
            taskList.remove(taskModel);
            this.notifyDataSetChanged();

            DatabaseReference mPostReference = TaskActivity.reference.child(taskModel.getId());
            mPostReference.removeValue();
        });

        // No Button
        dialogBuilder.setNegativeButton("No", (dialog, id_) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
    //endregion


    //==========================================================================================
    //                      Handle Task Check box Checked/Unchecked
    //==========================================================================================
    //region Handle Task Check box Checked/Unchecked

    /**
     * Checkbox Listener, marks a task as complete or incomplete when checked or unchecked
     *
     * @param checkbox  the checkbox
     * @param taskModel the Main Task
     * @param isChecked true if the checkbox is checked
     */
    private void TaskCompleteCheckboxListener(CompoundButton checkbox, TaskModel taskModel, boolean isChecked) {
        if (isChecked) MarkSubTasksAsComplete(checkbox, taskModel);
        else MarkSubTasksAsIncomplete(checkbox, taskModel);

        // Update taskModel completion in database
        DatabaseReference mPostReference = TaskActivity.reference.child(taskModel.getId());
        mPostReference.child("isCompleted").setValue(isChecked);
    }

    /**
     * Marks all sub-tasks as complete when Main Task is checked
     *
     * @param checkbox  the checkbox
     * @param taskModel the Main Task
     */
    private void MarkSubTasksAsComplete(CompoundButton checkbox, TaskModel taskModel) {
        View view = checkbox.getRootView();
        int incompleteSubTaskCount = 0;

        for (SubTaskModel subTaskModel : taskModel.getSubTaskModel().values())
            if (!subTaskModel.getIsCompleted())
                incompleteSubTaskCount++;

        if (incompleteSubTaskCount == 0) return;

        String message = "Do you want to mark all sub task as complete?";

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
        dialogBuilder.setTitle(incompleteSubTaskCount + " incomplete sub tasks");
        dialogBuilder.setMessage(message);

        // Don't dismiss when pressed outside of AlertDialog
        dialogBuilder.setCancelable(false);

        // Yes Button
        dialogBuilder.setPositiveButton("Yes", (dialog, id_) -> {
            HashMap<String, SubTaskModel> subTasks = new HashMap<>();
            for (SubTaskModel subTaskModel : taskModel.getSubTaskModel().values()) {
                subTaskModel.setIsCompleted(true);
                subTasks.put(subTaskModel.getId(), subTaskModel);
            }

            taskModel.setSubTaskModel(subTasks);

            // Update task completion in database
            DatabaseReference reference = TaskActivity.reference.child(taskModel.getId()).child("subTaskModel");
            reference.setValue(subTasks);
        });

        // No Button
        dialogBuilder.setNegativeButton("No", (dialog, id_) -> checkbox.setChecked(false));

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Marks all sub-tasks as incomplete when Main Task is unchecked
     *
     * @param checkbox  the checkbox
     * @param taskModel the Main Task
     */
    private void MarkSubTasksAsIncomplete(CompoundButton checkbox, TaskModel taskModel) {
        View view = checkbox.getRootView();
        int completeSubTaskCount = 0;

        for (SubTaskModel subTaskModel : taskModel.getSubTaskModel().values())
            if (subTaskModel.getIsCompleted())
                completeSubTaskCount++;

        if (completeSubTaskCount == 0) return;

        String title = completeSubTaskCount + " completed sub tasks";
        String message = "Do you want to mark all sub task as incomplete?";

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);

        // Don't dismiss when pressed outside of AlertDialog
        dialogBuilder.setCancelable(false);

        // Yes Button
        dialogBuilder.setPositiveButton("Yes", (dialog, id_) -> {
            HashMap<String, SubTaskModel> subTasks = new HashMap<>();
            for (SubTaskModel subTaskModel : taskModel.getSubTaskModel().values()) {
                subTaskModel.setIsCompleted(false);
                subTasks.put(subTaskModel.getId(), subTaskModel);
            }

            taskModel.setSubTaskModel(subTasks);

            // Update task completion in database
            DatabaseReference reference = TaskActivity.reference.child(taskModel.getId()).child("subTaskModel");
            reference.setValue(subTasks);
        });

        // No Button
        dialogBuilder.setNegativeButton("No", (dialog, id_) -> checkbox.setChecked(true));

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
    //endregion


    //==========================================================================================
    //                               Handle Task Clicks
    //==========================================================================================
    //region Handle Task Clicks

    /**
     * Handles short clicks, used for opening Sub-Task Activity list
     */
    private void TaskClickedListener(@NonNull MyViewHolder holder, TaskModel taskModel) {
        if (isEnable) {
            HandleMultiSelectTasks(holder);
            return;
        }

        // Open Sub Task Activity when taskModel clicked
        Intent intent = new Intent(activity, SubTaskActivity.class);
        intent.putExtra("taskID", taskModel.getId());
        activity.startActivity(intent);
    }

    /**
     * Handles Long Clicks, for selecting multiple Tasks
     */
    private boolean TaskLongClickedListener(@NonNull MyViewHolder holder, View view) {
        if (isEnable) {
            HandleMultiSelectTasks(holder);
            return false;
        }

        ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.multi_select_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                isEnable = true;
                HandleMultiSelectTasks(holder);
                mode.setTitle("Selected " + selectedTasksList.size());
                return true;
            }

            // Menu shown when Task is held
            // Delete Button and Select All Button
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_selectAll) SelectAllButtonAction();
                else if (id == R.id.menu_delete) DeleteMenuButtonAction(view, mode);

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isEnable = false;
                isSelectAll = false;

                selectedTasksList.clear();
                notifyDataSetChanged();

                holder.moreOptionsImageView.setVisibility(View.VISIBLE);
            }
        };

        actionMode = ((AppCompatActivity) view.getContext()).startActionMode(callback);
        return true;
    }

    /**
     * Handles Select All Tasks button
     */
    private void HandleMultiSelectAllTasks(@NonNull MyViewHolder holder) {
        int checkBoxVisibility = View.VISIBLE;
        int moreOptionsVisibility = View.GONE;
        int itemViewBgColor = Color.LTGRAY;

        if (!isSelectAll) {
            checkBoxVisibility = View.GONE;
            moreOptionsVisibility = View.VISIBLE;
            itemViewBgColor = Color.TRANSPARENT;
        }

        holder.checkBoxMultiSelect.setVisibility(checkBoxVisibility);
        holder.moreOptionsImageView.setVisibility(moreOptionsVisibility);
        holder.itemView.setBackgroundColor(itemViewBgColor);
    }

    /**
     * Handles Multi Select Tasks click
     */
    private void HandleMultiSelectTasks(@NonNull MyViewHolder holder) {
        TaskModel taskModel = taskList.get(holder.getAdapterPosition());

        int checkBoxVisibility = View.VISIBLE;
        int moreOptionsVisibility = View.GONE;
        int itemViewBgColor = Color.LTGRAY;

        if (holder.checkBoxMultiSelect.getVisibility() == View.GONE)
            selectedTasksList.add(taskModel);

        else {
            checkBoxVisibility = View.GONE;
            moreOptionsVisibility = View.VISIBLE;
            itemViewBgColor = Color.TRANSPARENT;

            selectedTasksList.remove(taskModel);
        }

        holder.checkBoxMultiSelect.setVisibility(checkBoxVisibility);
        holder.moreOptionsImageView.setVisibility(moreOptionsVisibility);
        holder.itemView.setBackgroundColor(itemViewBgColor);

        if (actionMode != null)
            actionMode.setTitle("Selected " + selectedTasksList.size());
    }
    //endregion


    /**
     * Add/Remove Strikethrough text when Task is checked/unchecked
     */
    private void HandleTaskChecked(@NonNull MyViewHolder holder, TaskModel taskModel) {
        holder.taskCompletedCheckBox.setChecked(taskModel.getIsCompleted());

        int getPaintFlags = holder.taskNameTextView.getPaintFlags();
        int strikeThroughTextFlag = Paint.STRIKE_THRU_TEXT_FLAG;
        int paintFlags = getPaintFlags & (~strikeThroughTextFlag);

        if (taskModel.getIsCompleted())
            paintFlags = getPaintFlags | strikeThroughTextFlag;

        holder.taskNameTextView.setPaintFlags(paintFlags);
    }


    /**
     * Method used for Filtering Tasks
     *
     * @return search Filter
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<TaskModel> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                // Creates new List of Tasks that contains the filter pattern
                for (TaskModel taskModel : filteredTaskList)
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
                    taskList.add((TaskModel) obj);

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
        TaskModel taskModel = taskList.get(position);
        String taskName = taskModel.getTaskName();
        // Set the Task Name
        holder.taskNameTextView.setText(taskName);

        HandleTaskChecked(holder, taskModel);

        // Show/Hide Description Text View
        if (taskModel.getDetails() == null || taskModel.getDetails().trim().length() == 0)
            holder.taskDetailTextView.setVisibility(View.GONE);
        else holder.taskDetailTextView.setText(taskModel.getDetails());

        // Show/Hide Date/Time Text View
        if (taskModel.getDate() == null || taskModel.getDate().trim().length() == 0)
            holder.taskDateTimeTextView.setVisibility(View.GONE);
        else
            holder.taskDateTimeTextView.setText(String.format("%s, %s", taskModel.getDate(), taskModel.getTime()));

        // More options button Clicked
        holder.moreOptionsImageView.setOnClickListener(v -> MoreOptionsPopupListener(holder, v, position));
        // Task Complete checkbox checked/unchecked
        holder.taskCompletedCheckBox.setOnCheckedChangeListener((checkbox, isChecked) -> TaskCompleteCheckboxListener(checkbox, taskModel, isChecked));
        // Short Task Click
        holder.itemView.setOnClickListener(v -> TaskClickedListener(holder, taskModel));
        // Long Task Click
        holder.itemView.setOnLongClickListener(view -> TaskLongClickedListener(holder, view));
        // Handle Select all Tasks
        HandleMultiSelectAllTasks(holder);

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskNameTextView;
        private final TextView taskDetailTextView;
        private final TextView taskDateTimeTextView;
        private final ImageView moreOptionsImageView;
        private final ImageView checkBoxMultiSelect;
        private final CheckBox taskCompletedCheckBox;

        public MyViewHolder(@NonNull View view) {
            super(view);

            taskNameTextView = view.findViewById(R.id.taskName);
            taskDetailTextView = view.findViewById(R.id.taskDetail);
            taskDateTimeTextView = view.findViewById(R.id.taskDateTime);
            moreOptionsImageView = view.findViewById(R.id.moreOptionsImageView);
            checkBoxMultiSelect = view.findViewById(R.id.itemSelectedCheckbox);
            taskCompletedCheckBox = view.findViewById(R.id.taskCompleted);
        }
    }
}
