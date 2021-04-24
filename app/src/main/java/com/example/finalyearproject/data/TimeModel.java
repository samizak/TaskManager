package com.example.finalyearproject.data;

import java.util.Locale;

public class TimeModel {
    private int hour = -1;
    private int minute = -1;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String FormatTime() {
        if (getHour() == -1) return "";
        return String.format(Locale.getDefault(), "%02d:%02d", getHour(), getMinute());
    }
}
