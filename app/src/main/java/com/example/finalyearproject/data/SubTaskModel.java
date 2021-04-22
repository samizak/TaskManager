package com.example.finalyearproject.data;

public class SubTaskModel extends ToDoModel {

    public SubTaskModel() {
    }

    public SubTaskModel(String taskName, boolean isCompleted) {
        this.taskName = taskName;
        this.isCompleted = isCompleted;
    }
}
