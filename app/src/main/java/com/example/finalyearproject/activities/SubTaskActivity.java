package com.example.finalyearproject.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.R;
import com.example.finalyearproject.RecyclerViewAdapterSubTask;
import com.example.finalyearproject.data.SubTaskModel;
import com.example.finalyearproject.data.TaskModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SubTaskActivity extends AppCompatActivity {

    private TaskModel taskModel;

    private RecyclerView recyclerView;
    private RecyclerViewAdapterSubTask recyclerViewAdapter;
    private TextView noDataTextView;
    private CollapsingToolbarLayout collapsingToolbarLayout;


    // Get Task from Firebase
    private void getTaskData(final String id) {
        TaskActivity.reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (Objects.requireNonNull(ds.getKey()).equals(id)) {
                        taskModel = ds.getValue(TaskModel.class);
                        break;
                    }
                }

                HideShowNoTaskMessage();
                taskModel.setSubTaskModel((taskModel.getSubTaskModel()));

                // Set the Title of the Toolbar to the Task Name
                collapsingToolbarLayout.setTitle(Objects.requireNonNull(taskModel).getTaskName());

                recyclerViewAdapter = new RecyclerViewAdapterSubTask(SubTaskActivity.this, taskModel);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SubTaskActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerViewAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TaskActivity.TAG, "onCancelled error is : " + error.getMessage());
            }
        });
    }

    private void HideShowNoTaskMessage() {
        int viewVisibility = taskModel.getSubTaskModel().size() == 0 ? View.VISIBLE : View.GONE;
        noDataTextView.setVisibility(viewVisibility);
    }

    private void fabListener(View v) {
        View dialogView = getLayoutInflater().inflate(R.layout.add_new_subtask, null);

        EditText subTaskNameEditText = dialogView.findViewById(R.id.subTaskNameEditText);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Sub Task");

        dialogBuilder.setPositiveButton("Save", null);
        dialogBuilder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v12 -> {
            String subTaskName = String.valueOf(subTaskNameEditText.getText());
            // Make sure the sub-task name is not empty!
            if (subTaskName.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Task name cannot be empty!", Toast.LENGTH_LONG).show();
                return;
            }

            // Get the key before pushing
            String key = TaskActivity.reference.push().getKey();
            // Set the id to the task model
            SubTaskModel subTaskModel = new SubTaskModel();
            subTaskModel.setId(key);
            subTaskModel.setTaskName(subTaskName);
            subTaskModel.setIsCompleted(false);

            TaskActivity.reference.child(taskModel.getId()).child("subTaskModel").child(Objects.requireNonNull(key)).setValue(subTaskModel);
            // Dismiss the popup
            alertDialog.dismiss();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_subtasks);
        setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView); // Sub-task recyclerview
        noDataTextView = findViewById(R.id.subTask_empty);
        FloatingActionButton fab = findViewById(R.id.addTask);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);

        Log.d(TaskActivity.TAG, "HERE");

        String taskID = getIntent().getStringExtra("taskID");
        getTaskData(taskID);

        // Fab Button for Adding new Sub Tasks
        fab.setOnClickListener(this::fabListener);
    }
}