package com.example.finalyearproject.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.AddNewTaskBottomSheet;
import com.example.finalyearproject.R;
import com.example.finalyearproject.RecyclerViewAdapter;
import com.example.finalyearproject.data.ToDoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TaskActivity extends AppCompatActivity {

    public static ArrayList<ToDoModel> taskList = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;


    private void fabListener(View view) {
        AddNewTaskBottomSheet addNewTaskBottomSheet = new AddNewTaskBottomSheet();
        addNewTaskBottomSheet.show(getSupportFragmentManager(), "addNewTaskBottomSheet");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tasks);

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.addTask);

        ToDoModel toDoModel1 = new ToDoModel();
        toDoModel1.setTaskName("Task 1");
        toDoModel1.setIsCompleted(false);

        taskList.add(toDoModel1);


        recyclerViewAdapter = new RecyclerViewAdapter(TaskActivity.this, taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(TaskActivity.this));
        recyclerView.setAdapter(recyclerViewAdapter);

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