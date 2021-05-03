package com.example.finalyearproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_1_ID = "TaskManager.channel1";

    @Override
    public void onCreate() {
        super.onCreate();
        CreateNotificationChannels();
    }

    /**
     * Creates the notification Channels
     */
    private void CreateNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel1 = new NotificationChannel(CHANNEL_1_ID, "Task", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel1.setDescription("Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel1);
        }
    }
}
