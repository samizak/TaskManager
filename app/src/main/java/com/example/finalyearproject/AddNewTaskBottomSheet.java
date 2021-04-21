package com.example.finalyearproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalyearproject.activities.TaskActivity;
import com.example.finalyearproject.data.DateModel;
import com.example.finalyearproject.data.TimeModel;
import com.example.finalyearproject.data.ToDoModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class AddNewTaskBottomSheet extends BottomSheetDialogFragment {

    private final ToDoModel updateTaskModel = new ToDoModel();
    private final DateModel dateModel = new DateModel();
    private final TimeModel timeModel = new TimeModel();

    private TextView dateViewText;
    private TextView timeViewText;
    private TextView dateTimeTextView;
    private TextView createNewTaskTextView;

    private EditText enterTaskEditText;
    private EditText enterTaskDetails;
    private Button saveButton;
    private boolean isUpdateTask = false;

    private Context context;
    private String tempTaskName;


    private String formatDateTime() {
        if (dateModel.FormatDate().equals("")) return "";
        return String.format("%s, %s", dateModel.FormatDate(), timeModel.FormatTime());
    }

    //==========================================================================================
    //                              Date and Time picker Listeners
    //==========================================================================================
    private void SelectDateButtonListener(View v) {
        // Get Current Date
        final Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
            dateModel.setDay(dayOfMonth);
            dateModel.setMonth(monthOfYear);
            dateModel.setYear(year);

            dateViewText.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year));
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void SelectTimeButtonListener(View v) {
        // Get Current Time
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        @SuppressLint("SetTextI18n")
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            timeModel.setMinute(minute);
            timeModel.setHour(hourOfDay);

            timeViewText.setText(timeModel.FormatTime());
        }, mHour, mMinute, true);

        timePickerDialog.show();
    }

    private void DateTimePickerButtonListener(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        LayoutInflater inflater2 = this.getLayoutInflater();
        View dialogView = inflater2.inflate(R.layout.date_time_picker, null);

        ImageButton selectDateButton = dialogView.findViewById(R.id.selectDateButton);
        ImageButton selectTimeButton = dialogView.findViewById(R.id.selectTimeButton);
        dateViewText = dialogView.findViewById(R.id.DateViewText);
        timeViewText = dialogView.findViewById(R.id.TimeViewText);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Set Date and Time");
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        // Handle Date and Time picker
        positiveButton.setOnClickListener(discard -> {
            boolean dateViewEmpty = (dateViewText.getText().toString().trim().length() == 0);
            boolean timeViewEmpty = (timeViewText.getText().toString().trim().length() == 0);

            dateViewText.setError(null);
            timeViewText.setError(null);
            dateViewText.clearFocus();
            timeViewText.clearFocus();

            if (dateViewEmpty) {
                dateViewText.requestFocus();
                dateViewText.setError("Please Choose a Date!");
            }
            if (timeViewEmpty) {
                timeViewText.requestFocus();
                timeViewText.setError("Please Choose a Time!");
            }

            if (!dateViewEmpty && !timeViewEmpty) {
                dateTimeTextView.setText(formatDateTime());
                alertDialog.dismiss();
            }
        });

        // Select Date Button pressed
        selectDateButton.setOnClickListener(this::SelectDateButtonListener);
        // Select Time Button pressed
        selectTimeButton.setOnClickListener(this::SelectTimeButtonListener);
    }

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
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        saveButton = v.findViewById(R.id.saveTaskButton);
        enterTaskEditText = v.findViewById(R.id.enterTaskText);
        enterTaskDetails = v.findViewById(R.id.enterTaskDetails);
        dateTimeTextView = v.findViewById(R.id.DateTimeTextView);
        createNewTaskTextView = v.findViewById(R.id.createNewTaskTextView);
        ImageButton dateTimePickerButton = v.findViewById(R.id.dateTimePickerButton);

        AutoFillIfEditingTask(getArguments());
        // Handle Date/Time pickers
        dateTimePickerButton.setOnClickListener(this::DateTimePickerButtonListener);


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
