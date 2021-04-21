package com.example.finalyearproject.data;

public class ToDoModel {
    protected String id;
    protected String taskName;
    protected boolean isCompleted;

    public ToDoModel() {
        this.id = "";
        this.taskName = "";
        this.isCompleted = false;
    }

    public ToDoModel(String id, String taskName, boolean isCompleted) {
        this.id = id;
        this.taskName = taskName;
        this.isCompleted = isCompleted;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
}
