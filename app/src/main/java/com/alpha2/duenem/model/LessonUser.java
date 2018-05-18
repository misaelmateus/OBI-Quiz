package com.alpha2.duenem.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonUser implements Serializable {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private List<Historic> historic;
    private String uidLesson;
    private Boolean done = false;

    // System of Memorization -------------------------//
    private Date lastDate;
    private int correctStreak;
    private Date nextDate;
    private double EF;
    private int interval;

    public LessonUser() {
        this.EF = 1.3;
        this.interval = 1;
        this.historic = new ArrayList<>();
    }

    public LessonUser(Date lastDate, int correctStreak, Date nextDate) {
        this();
        this.lastDate = lastDate;
        this.correctStreak = correctStreak;
        this.nextDate = nextDate;
    }

    @Exclude
    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) throws ParseException {
        this.lastDate = dateFormat.parse(lastDate);
    }

    @Exclude
    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public int getCorrectStreak() {
        return correctStreak;
    }

    public void setCorrectStreak(int correctStreak) {
        this.correctStreak = correctStreak;
    }

    @Exclude
    public Date getNextDate() {
        return nextDate;
    }

    // Firebase Database Getters and Setters

    public void setNextDate(String nextDate) throws ParseException {
        this.nextDate = dateFormat.parse(nextDate);
    }

    @Exclude
    public void setNextDate(Date nextDate) {
        this.nextDate = nextDate;
    }

    @PropertyName("lastDate")
    public String getLastDateString() {
        return dateFormat.format(this.lastDate);
    }

    @PropertyName("nextDate")
    public String getNextDateString() {
        return dateFormat.format(this.nextDate);
    }

    public double getEF() {
        return this.EF;
    }

    private void setNextEF(int q) {
        EF = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));
        if (EF < 1.3) EF = 1.3;
    }

    private void setNextInterval(int q, Boolean isDone) {
        if (q < 3) {
            interval = 1;
            correctStreak = 0;
        } else if (!isDone) {
            if (correctStreak == 0) {
                interval = 1;
                correctStreak++;
            } else if (correctStreak == 1) {
                interval = 4;
                correctStreak++;
            } else {
                setNextEF(q);
                interval = (int) (interval * EF);
                correctStreak++;
            }
        }
    }

    public void userDoneQuestion(int grade, Date date, int time) {
        int q = 0;
        if (grade >= 90) q = 5;
        else if (grade >= 80) q = 4;
        else if (grade >= 60) q = 3;
        else if (grade >= 50) q = 2;
        else if (grade >= 30) q = 1;
        boolean isDone = false;
        if (q >= 4) isDone = true;
        setNextInterval(q, isDone);
        setNextDate(date);
        this.done |= isDone;
        historic.add(new Historic(date, grade, time));
    }

    public int getInterval() {
        return interval;
    }

    public String getUidLesson() {
        return uidLesson;
    }

    public void setUidLesson(String uidTopic) {
        this.uidLesson = uidTopic;
    }

    public Boolean getDone() {
        return done;
    }
}
