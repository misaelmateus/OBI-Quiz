package com.alpha2.duenem.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Topic implements Serializable {
    private Boolean isDone;
    private String title;
    private String description;
    private String uid;
    private Discipline discipline;
    private int curr_level;
    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private List<Lesson> lessons;

    public Topic() {
        this.lessons = new ArrayList<>();
        isDone = false;
    }

    public Topic(String title, String description){
        this();
        this.title = title;
        this.description = description;
        isDone = false;
    }

    public void addLesson(Lesson lesson){
        lessons.add(lesson);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public String getTitle(){
        return this.title;
    }

    public String getDescription(){
        return this.description;
    }


    @Exclude
    public List<Lesson> getLessons(){
        return this.lessons;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", title, description);
    }

    public void setIsDone(Boolean isDone){
        this.isDone = isDone;
    }
    public boolean isDone(){
        return this.isDone;
    }

    public int getCurr_level() {
        return curr_level;
    }

    public void setCurr_level(int curr_level) {
        this.curr_level = curr_level;
    }
}
