package com.example.finalyearproject.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.example.finalyearproject.activities.TaskActivity;
import com.example.finalyearproject.data.TaskModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class ImportExportData {

    private final Context context;
    private final ContentResolver contentResolver;
    private final Gson gson;

    private EncryptDecryptData encryptDecryptData;

    public ImportExportData(Context context, ContentResolver contentResolver) {
        this.context = context;
        this.contentResolver = contentResolver;
        gson = new Gson();

        try {
            encryptDecryptData = new EncryptDecryptData();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Converts TaskModel ArrayList into Json string and saves data to file
    public void ExportTasks(int resultCode, Intent data, ArrayList<TaskModel> taskList) {
        if (resultCode == RESULT_OK) {
            OutputStream outputStream;
            Uri uri = data.getData();
            String jsonTasks = gson.toJson(taskList);

            try {
                String encryptedData = encryptDecryptData.EncryptData(jsonTasks);

                outputStream = contentResolver.openOutputStream(uri);
                outputStream.write(encryptedData.getBytes(StandardCharsets.UTF_8));
                outputStream.close();

                Toast.makeText(context, "File Saved!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(context, "Failed to Save file!", Toast.LENGTH_LONG).show();
            }
        } else Toast.makeText(context, "Failed to Save file!", Toast.LENGTH_LONG).show();
    }

    // Converts Json string into ToDoModel ArrayList and pushes it to Firebase Database
    public void ImportTasks(Intent data) {
        Uri uri = data.getData();
        String fileContent = ReadTextFile(uri);

        try {
            String decryptedData = encryptDecryptData.DecryptData(fileContent);

            ArrayList<TaskModel> taskModels = gson.fromJson(decryptedData, new TypeToken<ArrayList<TaskModel>>() {
            }.getType());

            for (TaskModel taskModel : taskModels)
                TaskActivity.PushToDatabase(taskModel);

        } catch (Exception e) {
            Toast.makeText(context, "Failed to read from file!", Toast.LENGTH_LONG).show();
        }
    }

    // Reads data from file using URI and returns string
    private String ReadTextFile(Uri uri) {
        StringBuilder builder = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(contentResolver.openInputStream(uri)));
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);

            reader.close();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return builder.toString();
    }
}
