package com.example.finalyearproject.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.finalyearproject.App;
import com.example.finalyearproject.R;
import com.example.finalyearproject.activities.TaskActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("taskName");
        String taskDetails = intent.getStringExtra("taskDetails");

        Intent activityIntent = new Intent(context, TaskActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

        Notification.Builder builder = new Notification.Builder(context, App.CHANNEL_1_ID);
        builder.setSmallIcon(R.drawable.ic_baseline_add_alert_24);
        builder.setContentTitle(taskName);
        builder.setContentText(taskDetails);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, notification);
    }
}
