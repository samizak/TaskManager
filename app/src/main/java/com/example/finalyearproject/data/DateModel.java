package com.example.finalyearproject.data;

import android.annotation.SuppressLint;

/**
 * This class defines a Date
 */
public class DateModel {
    private int year = -1;
    private int month = -1;
    private int day = -1;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    /**
     * Formats the Integers used for date
     *
     * @return a Date string in the format: dd/mm/yyyy
     */
    @SuppressLint("DefaultLocale")
    public String FormatDate() {
        if (getYear() == -1) return "";
        return String.format("%d/%d/%d", getDay(), getMonth() + 1, getYear());
    }

}
