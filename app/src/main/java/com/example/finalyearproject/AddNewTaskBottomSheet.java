package com.example.finalyearproject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.core.content.ContextCompat;

import com.example.finalyearproject.activities.TaskActivity;
import com.example.finalyearproject.data.DateModel;
import com.example.finalyearproject.data.TaskModel;
import com.example.finalyearproject.data.TimeModel;
import com.example.finalyearproject.utils.NotificationReceiver;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Objects;

/**
 * The BottomSheet displayed for Creating or Modifying a Main Task
 */
public class AddNewTaskBottomSheet extends BottomSheetDialogFragment {

    private final TaskModel updateTaskModel = new TaskModel();
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


    /**
     * Formats the Date and Time into a single String
     *
     * @return Formatted Date and Time String
     */
    private String formatDateTime() {
        if (dateModel.FormatDate().equals("")) return "";
        return String.format("%s, %s", dateModel.FormatDate(), timeModel.FormatTime());
    }

    //==========================================================================================
    //                              Date and Time picker Listeners
    //==========================================================================================
    //region Handle  Date and Time picker Listeners

    /**
     * Button Listener used for Selecting the Date
     */
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

    /**
     * Button Listener used for Selecting Time
     */
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

    /**
     * Button Listener used for displaying the Date and Time picker Popup Dialog
     */
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

    /**
     * TextView Listener used for clearing the Date and Time String when clicked
     */
    private void DateTimeTextViewListener(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
        dialogBuilder.setTitle("Clear Date/Time?");
        dialogBuilder.setMessage("Are you sure you want to delete the Date and Time from the task?");

        dialogBuilder.setPositiveButton("Yes", (dialog, id_) -> dateTimeTextView.setText(""));
        dialogBuilder.setNegativeButton("No", (dialog, id_) -> dialog.dismiss());
        dialogBuilder.show();
    }
    //endregion


    //==========================================================================================
    //                                  Handle Creating and Updating Tasks
    //==========================================================================================
    //region Handle Creating and Updating Tasks

    /**
     * Button Listener used for Saving a Main Task
     */
    private void SaveButtonListener(View v) {
        String taskName = enterTaskEditText.getText().toString().trim();
        String details = enterTaskDetails.getText().toString().trim();

        if (taskName.isEmpty()) {
            Toast.makeText(context, "Task name cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        CreateNewTask(taskName, details);
        UpdateTask(taskName, details);
        dismiss();
    }

    /**
     * Saves a Task
     *
     * @param taskModel     the Main Task
     * @param isPushNewTask true if a new task is being created
     */
    private void SaveTask(TaskModel taskModel, boolean isPushNewTask) {
        if (isPushNewTask) {
            TaskActivity.PushToDatabase(taskModel);
            return;
        }

        // Get the key before updating
        String key = taskModel.getId();
        // Make changes to child with index value
        TaskActivity.reference.child(key).setValue(taskModel);
    }

    /**
     * Creates a new Task
     *
     * @param taskName the name of the task
     * @param details  the details of the task
     */
    private void CreateNewTask(String taskName, String details) {
        // Skip if we are not Creating a new Task...
        if (isUpdateTask) return;

        TaskModel taskModel = new TaskModel();

        taskModel.setTaskName(taskName);
        taskModel.setDetails(details);
        taskModel.setDate(dateModel.FormatDate());
        taskModel.setTime(timeModel.FormatTime());
        taskModel.setIsCompleted(false);

        SendNotification(taskModel);
        SaveTask(taskModel, true);
    }

    /**
     * Updates an existing Task
     *
     * @param taskName the name of the task
     * @param details  the details of the task
     */
    private void UpdateTask(String taskName, String details) {
        // Skip if we not Updating an existing Task
        if (!isUpdateTask) return;

        TaskModel taskModel = new TaskModel();
        String date = "";
        String time = "";

        // The Date and Time string (eg. 17/01/2021, 14:00)
        String dateTime = dateTimeTextView.getText().toString().trim();

        if (dateTime.length() > 0) {
            date = dateTime.split(",")[0];
            time = dateTime.split(",")[1];
        }

        taskModel.setId(updateTaskModel.getId());
        taskModel.setTaskName(taskName);
        taskModel.setDetails(details);
        taskModel.setDate(date);
        taskModel.setTime(time);
        taskModel.setIsCompleted(updateTaskModel.getIsCompleted());

        SendNotification(taskModel);
        SaveTask(taskModel, false);
    }

    /**
     * Created a Notification at the Date and Time specified by the user
     *
     * @param taskModel the Main Task
     */
    public void SendNotification(TaskModel taskModel) {
        // Skip if date and time not set
        if (dateModel.getYear() == -1) return;

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("taskName", taskModel.getTaskName());
        notificationIntent.putExtra("taskDetails", taskModel.getDetails());

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        AlarmManager manager = (AlarmManager) Objects.requireNonNull(getActivity()).getSystemService(Context.ALARM_SERVICE);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, dateModel.getDay());
        cal.set(Calendar.MONTH, dateModel.getMonth());
        cal.set(Calendar.YEAR, dateModel.getYear());
        cal.set(Calendar.HOUR_OF_DAY, timeModel.getHour());
        cal.set(Calendar.MINUTE, timeModel.getMinute());
        cal.set(Calendar.SECOND, 0);

        manager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
    }
    //endregion


    /**
     * Automatically fills in the data when editing a Task
     *
     * @param bundle a bundle used for passing data between Activities
     */
    private void AutoFillIfEditingTask(Bundle bundle) {
        // Only automatically fill details when in edit mode!
        if (bundle == null) return;

        isUpdateTask = true;
        int enabledColour = ContextCompat.getColor(context, R.color.primaryTextColour);

        createNewTaskTextView.setText(R.string.editTaskTextView);

        saveButton.setEnabled(true);
        saveButton.setTextColor(enabledColour);

        updateTaskModel.setId(bundle.getString("taskID"));
        updateTaskModel.setTaskName(bundle.getString("taskName"));
        updateTaskModel.setDetails(bundle.getString("taskDetails"));
        updateTaskModel.setDate(bundle.getString("taskDate"));
        updateTaskModel.setTime(bundle.getString("taskTime"));
        updateTaskModel.setIsCompleted(Boolean.parseBoolean(bundle.getString("taskCompleted")));


        String dateTimeString = String.format("%s, %s", updateTaskModel.getDate(), updateTaskModel.getTime());
        String dateTime = updateTaskModel.getDate().equals("") ? "" : dateTimeString;

        enterTaskEditText.setText(updateTaskModel.getTaskName());
        enterTaskDetails.setText(updateTaskModel.getDetails());
        dateTimeTextView.setText(dateTime);
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

        // Make button text gray to show it's disabled
        if (!isUpdateTask) saveButton.setTextColor(Color.GRAY);

        AutoFillIfEditingTask(getArguments());
        // Handle Date/Time pickers
        dateTimePickerButton.setOnClickListener(this::DateTimePickerButtonListener);
        // Handle clear Date and Time from Text View when clicked
        dateTimeTextView.setOnClickListener(this::DateTimeTextViewListener);

        enterTaskEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String taskName = enterTaskEditText.getText().toString().trim();

                int enabledColour = ContextCompat.getColor(context, R.color.primaryTextColour);
                int saveButtonColour = taskName.isEmpty() ? Color.GRAY : enabledColour;

                saveButton.setEnabled(!taskName.isEmpty());
                saveButton.setTextColor(saveButtonColour);
            }
        });
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
