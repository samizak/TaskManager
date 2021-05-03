package com.example.finalyearproject.data;

import java.util.HashMap;

/**
 * This class defines a Main Task
 */
public class TaskModel extends ToDoModel {
    private String details;
    private String date;
    private String time;

    private HashMap<String, SubTaskModel> subTaskModel = new HashMap<>();

    public TaskModel() {
        this.id = "";
        this.taskName = "";
        this.details = "";
        this.date = "";
        this.time = "";
        this.isCompleted = false;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String description) {
        this.details = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public HashMap<String, SubTaskModel> getSubTaskModel() {
        return subTaskModel;
    }

    public void setSubTaskModel(HashMap<String, SubTaskModel> subTaskModel) {
        this.subTaskModel = subTaskModel;
    }
}
