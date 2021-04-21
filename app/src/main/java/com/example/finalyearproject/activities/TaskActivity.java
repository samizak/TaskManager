package com.example.finalyearproject.activities;

import android.os.Bundle;
import android.view.View;

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
}