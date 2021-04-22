package com.example.finalyearproject.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.AddNewTaskBottomSheet;
import com.example.finalyearproject.R;
import com.example.finalyearproject.RecyclerViewAdapter;
import com.example.finalyearproject.data.TaskModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class TaskActivity extends AppCompatActivity {

    public static final String TAG = "test";
    public static final String FILENAME = "data";
    public static DatabaseReference reference;
    public static final ArrayList<TaskModel> taskList = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private TextView noDataTextView;

    //private ImportExportData importExportData;


    private void fabListener(View view) {
        AddNewTaskBottomSheet addNewTaskBottomSheet = new AddNewTaskBottomSheet();
        addNewTaskBottomSheet.show(getSupportFragmentManager(), "addNewTaskBottomSheet");
    }

    //==========================================================================================
    //                                  Database Manager
    //==========================================================================================
    public static void PushToDatabase(TaskModel taskModel) {
        // Only create a new key if the Task does not have an ID.
        boolean isNewTask = taskModel.getId().length() == 0;
        // Get the Key from the Task by default, even if empty
        String key = taskModel.getId();

        if (isNewTask) {
            // Get the key before pushing
            key = reference.push().getKey();
            // Set the id to the task model
            taskModel.setId(key);
        }

        // Create a child with index value
        reference.child(Objects.requireNonNull(key)).setValue(taskModel);
    }

    private void GetTaskDataFromFirebase() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the task list so it can be updated with new data
                taskList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    TaskModel taskModel = ds.getValue(TaskModel.class);
                    taskList.add(taskModel);
                }

                recyclerViewAdapter = new RecyclerViewAdapter(TaskActivity.this, taskList);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TaskActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerViewAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tasks);

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.addTask);

        reference = FirebaseDatabase.getInstance().getReference().child("Tasks");

        GetTaskDataFromFirebase();

        // Fab Button for Adding new Tasks
        fab.setOnClickListener(this::fabListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_layout_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        MenuItem hideCompletedTasksMenu = menu.findItem(R.id.hideCompletedTasks);
        MenuItem importExportTaskMenu = menu.findItem(R.id.importExportTasks);
        MenuItem signOutMenu = menu.findItem(R.id.signOut);

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerViewAdapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }
}