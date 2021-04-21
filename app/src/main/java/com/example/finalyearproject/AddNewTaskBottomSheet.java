package com.example.finalyearproject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalyearproject.activities.TaskActivity;
import com.example.finalyearproject.data.ToDoModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddNewTaskBottomSheet extends BottomSheetDialogFragment {

    private final ToDoModel updateTaskModel = new ToDoModel();

    private EditText enterTaskEditText;
    private Button saveButton;

    private Context context;

    private boolean isUpdateTask = false;

    private String tempTaskName;

    private void SaveButtonListener(View v) {
        String taskName = enterTaskEditText.getText().toString().trim();

        // Show Error message if Task Name is empty
        if (taskName.isEmpty()) {
            Toast.makeText(context, "Task name cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        // Else, Push the task to the list
        ToDoModel taskModel = new ToDoModel();
        taskModel.setTaskName(taskName);
        taskModel.setIsCompleted(false);

        CreateNewTask(taskName);
        UpdateTask(taskName);

        dismiss();
    }
    private void SaveTask(ToDoModel taskModel, boolean isPushNewTask) {
        if (isPushNewTask) {
            TaskActivity.taskList.add(taskModel);
            return;
        }

        for(ToDoModel toDoModel : TaskActivity.taskList){
            if(taskModel.getTaskName().equals(tempTaskName)){
                toDoModel.setTaskName(taskModel.getTaskName());
                toDoModel.setIsCompleted(taskModel.getIsCompleted());
            }
        }
    }

    private void CreateNewTask(String taskName) {
        // Skip if we are not Creating a new Task...
        if (isUpdateTask) return;

        ToDoModel taskModel = new ToDoModel();

        taskModel.setTaskName(taskName);
        taskModel.setIsCompleted(false);

        SaveTask(taskModel, true);
    }

    private void UpdateTask(String taskName) {
        // Skip if we not Updating an existing Task
        if (!isUpdateTask) return;

        ToDoModel taskModel = new ToDoModel();
        tempTaskName = taskName;

        taskModel.setId(updateTaskModel.getId());
        taskModel.setTaskName(taskName);
        taskModel.setIsCompleted(updateTaskModel.getIsCompleted());

        SaveTask(taskModel, false);
    }


    // Automatically fill data if editing a Task
    private void AutoFillIfEditingTask(Bundle bundle) {
        // Only automatically fill details when in edit mode!
        if (bundle == null) return;

        isUpdateTask = true;

        updateTaskModel.setId(bundle.getString("taskID"));
        updateTaskModel.setTaskName(bundle.getString("taskName"));
        updateTaskModel.setIsCompleted(Boolean.parseBoolean(bundle.getString("taskCompleted")));

        enterTaskEditText.setText(updateTaskModel.getTaskName());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_task_bottom_sheet_layout, container, false);

        saveButton = v.findViewById(R.id.saveTaskButton);
        enterTaskEditText = v.findViewById(R.id.enterTaskText);

        AutoFillIfEditingTask(getArguments());


        // Handle Saving the Task data
        saveButton.setOnClickListener(this::SaveButtonListener);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
