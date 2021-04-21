package com.example.finalyearproject;

import android.content.Context;
import android.os.Bundle;
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

    private EditText enterTaskEditText;
    private Button saveButton;

    private Context context;

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

        SaveTask(taskModel);

        dismiss();
    }
    private void SaveTask(ToDoModel taskModel) {
        TaskActivity.taskList.add(taskModel);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_task_bottom_sheet_layout, container, false);

        saveButton = v.findViewById(R.id.saveTaskButton);
        enterTaskEditText = v.findViewById(R.id.enterTaskText);

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
