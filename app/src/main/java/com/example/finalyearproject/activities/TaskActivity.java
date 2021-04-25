package com.example.finalyearproject.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject.AddNewTaskBottomSheet;
import com.example.finalyearproject.R;
import com.example.finalyearproject.RecyclerViewAdapter;
import com.example.finalyearproject.data.TaskModel;
import com.example.finalyearproject.utils.ImportExportData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private final ArrayList<TaskModel> taskList = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private TextView noDataTextView;
    private ImportExportData importExportData;


    private void fabListener(View view) {
        AddNewTaskBottomSheet addNewTaskBottomSheet = new AddNewTaskBottomSheet();
        addNewTaskBottomSheet.show(getSupportFragmentManager(), "addNewTaskBottomSheet");
    }

    private void HideShowNoTaskMessage() {
        int viewVisibility = taskList.size() == 0 ? View.VISIBLE : View.GONE;
        noDataTextView.setVisibility(viewVisibility);
    }

    //==========================================================================================
    //                                  Database Manager
    //==========================================================================================
    //region Handle Database
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
                HideShowNoTaskMessage();

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
    //endregion

    //==========================================================================================
    //                                  Menu Items
    //==========================================================================================
    //region Handle Menu Items
    private boolean ImportExportDialog(MenuItem menuItem) {
        String[] optionType = new String[]{"Import tasks", "Export tasks"};

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
        builder.setTitle("What would you like to do?");

        builder.setSingleChoiceItems(optionType, -1, (DialogInterface dialog, int which) -> {
            if (optionType[which].equals("Import tasks")) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.setType("text/plain");
                startActivityForResult(intent, 2);

            } else if (optionType[which].equals("Export tasks")) {
                // Ignore if there are no tasks...
                if (taskList.size() == 0) {
                    dialog.dismiss();
                    Toast.makeText(TaskActivity.this, "There are no tasks to export...", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, FILENAME);
                startActivityForResult(intent, 1);
            }
            dialog.dismiss();
        });
        builder.show();

        return true;
    }

    private boolean SignOutMenuListener(MenuItem menuItem) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        return true;
    }
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tasks);
        setTitle("Task List");

        recyclerView = findViewById(R.id.recyclerView);
        noDataTextView = findViewById(R.id.rv_empty);
        FloatingActionButton fab = findViewById(R.id.addTask);

        // Gets the User reference
        // Using UID as key as Firebase does not allow special characters...
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(user.getUid());
        userRef.child("email").setValue(user.getEmail());

        reference = userRef.child("Tasks");

        GetTaskDataFromFirebase();

        // Fab Button for Adding new Tasks
        fab.setOnClickListener(this::fabListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_layout_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
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

        importExportData = new ImportExportData(getApplicationContext(), getContentResolver());
        importExportTaskMenu.setOnMenuItemClickListener(this::ImportExportDialog);
        signOutMenu.setOnMenuItemClickListener(this::SignOutMenuListener);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) importExportData.ExportTasks(resultCode, data, taskList);
        else if (requestCode == 2) importExportData.ImportTasks(data);
    }
}