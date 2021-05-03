package com.example.finalyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.RecyclerViewAdapterSubTask.MyViewHolder;
import com.example.finalyearproject.activities.SubTaskActivity;
import com.example.finalyearproject.activities.TaskActivity;
import com.example.finalyearproject.data.SubTaskModel;
import com.example.finalyearproject.data.TaskModel;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * This Class is used for Displaying and Updating the RecyclerView for Sub-Tasks
 */
public class RecyclerViewAdapterSubTask extends RecyclerView.Adapter<MyViewHolder> {

    private final Activity activity;
    private final TaskModel taskModel;
    private final ArrayList<SubTaskModel> taskList;
    private final ArrayList<SubTaskModel> selectedTasksList = new ArrayList<>();

    private boolean isEnable = false;
    private boolean isSelectAll = false;

    private ActionMode actionMode;


    public RecyclerViewAdapterSubTask(Activity activity, TaskModel taskModel) {
        this.activity = activity;
        this.taskModel = taskModel;
        // Convert HashMap to ArrayList
        this.taskList = new ArrayList<>(taskModel.getSubTaskModel().values());
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
        dialogBuilder.setMessage("Are you sure you want to delete the selected Sub-Tasks?");

        // Yes Button
        dialogBuilder.setPositiveButton("Yes", (dialog, id_) -> {
            for (int i = 0; i < selectedTasksList.size(); i++) {
                SubTaskModel subTaskModel = selectedTasksList.get(i);
                taskList.remove(subTaskModel);
                this.notifyDataSetChanged();

                DatabaseReference mPostReference = TaskActivity.reference.child(subTaskModel.getId()).child("subTaskModel").child(subTaskModel.getId());
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
     * Menu Button Listener, displays a new Dialog to edit task when clicked
     */
    private void EditTaskPopupListener(View v, int position) {
        SubTaskModel subTaskModel = taskList.get(position);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_new_subtask, null);

        EditText subTaskNameEditText = dialogView.findViewById(R.id.subTaskNameEditText);
        subTaskNameEditText.setText(subTaskModel.getTaskName());

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Sub Task");

        // Save Button
        dialogBuilder.setPositiveButton("Save", (dialog, id_) -> {
            // Get the key before updating
            String key = subTaskModel.getId();
            subTaskModel.setTaskName(String.valueOf(subTaskNameEditText.getText()));
            TaskActivity.reference.child(taskModel.getId()).child("subTaskModel").child(key).setValue(subTaskModel);
        });

        // Cancel Button
        dialogBuilder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
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
            SubTaskModel subTaskModel = taskList.get(position);
            taskList.remove(subTaskModel);
            this.notifyDataSetChanged();

            DatabaseReference mPostReference = TaskActivity.reference.child(taskModel.getId()).child("subTaskModel").child(subTaskModel.getId());
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
     * @param subTaskModel the Sub-Task
     * @param isChecked true if the checkbox is checked
     */
    private void TaskCompleteCheckboxListener(SubTaskModel subTaskModel, boolean isChecked) {
        // Update taskModel completion in database
        DatabaseReference reference = TaskActivity.reference.child(taskModel.getId()).child("subTaskModel").child(subTaskModel.getId());
        reference.child("isCompleted").setValue(isChecked);
    }
    //endregion

    //==========================================================================================
    //                               Handle Task Clicks
    //==========================================================================================
    //region Handle Task Clicks
    /**
     * Handles short clicks, used for opening Sub-Task Activity list
     */
    private void TaskClickedListener(@NonNull MyViewHolder holder, SubTaskModel taskModel) {
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
        SubTaskModel taskModel = taskList.get(holder.getAdapterPosition());

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
     * Add/Remove Strikethrough text when Sub-Task is checked/unchecked
     */
    private void HandleTaskChecked(@NonNull MyViewHolder holder, SubTaskModel subTaskModel) {
        holder.taskCompletedCheckBox.setChecked(subTaskModel.getIsCompleted());

        int getPaintFlags = holder.taskNameTextView.getPaintFlags();
        int strikeThroughTextFlag = Paint.STRIKE_THRU_TEXT_FLAG;
        int paintFlags = getPaintFlags & (~strikeThroughTextFlag);

        if (subTaskModel.getIsCompleted())
            paintFlags = getPaintFlags | strikeThroughTextFlag;

        holder.taskNameTextView.setPaintFlags(paintFlags);
    }

    /**
     * Checks if there are no sub-tasks completed. If so, uncheck the parent task
     */
    private void CheckNoSubTasksCompleted(TaskModel task) {
        int subTasksCompleted = 0;

        for (SubTaskModel subTaskModel : task.getSubTaskModel().values())
            if (subTaskModel.getIsCompleted())
                subTasksCompleted++;

        boolean isComplete = (subTasksCompleted == task.getSubTaskModel().size());

        // Update task completion in database
        DatabaseReference mPostReference = TaskActivity.reference.child(task.getId());
        mPostReference.child("isCompleted").setValue(isComplete);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View taskView = LayoutInflater.from(parent.getContext()).inflate(R.layout.subtask_layout, parent, false);
        return new MyViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SubTaskModel subTaskModel = taskList.get(position);
        String taskName = subTaskModel.getTaskName();
        // Set the Task Name
        holder.taskNameTextView.setText(taskName);

        HandleTaskChecked(holder, subTaskModel);
        CheckNoSubTasksCompleted(taskModel);

        // More options button Clicked
        holder.moreOptionsImageView.setOnClickListener(v -> MoreOptionsPopupListener(holder, v, position));
        // Task Complete checkbox checked/unchecked
        holder.taskCompletedCheckBox.setOnCheckedChangeListener((checkbox, isChecked) -> TaskCompleteCheckboxListener(subTaskModel, isChecked));
        // Short Task Click
        holder.itemView.setOnClickListener(v -> TaskClickedListener(holder, subTaskModel));
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
        private final CheckBox taskCompletedCheckBox;
        private final ImageView moreOptionsImageView;
        private final ImageView checkBoxMultiSelect;

        public MyViewHolder(@NonNull View view) {
            super(view);

            taskNameTextView = view.findViewById(R.id.taskName);
            taskCompletedCheckBox = view.findViewById(R.id.taskCompleted);
            moreOptionsImageView = view.findViewById(R.id.moreOptionsImageView);
            checkBoxMultiSelect = view.findViewById(R.id.itemSelectedCheckbox);
        }
    }
}
