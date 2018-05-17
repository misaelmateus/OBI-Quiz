package com.alpha2.duenem.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 13/06/17.
 */

public class Lesson implements Serializable {
    private Boolean isDone;
    private String text;
    private String title;
    private String description;
    private String uid;
    private Topic topic;
    private List<Material> materials;

    public Lesson() {
        materials = new ArrayList<>();
        title = "";
        description = "";
        text = "";
        isDone = false;
    }

    public Lesson(String title, String description) {
        this.title = title;
        this.description = description;
        materials = new ArrayList<>();
        isDone = false;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Exclude
    public List<Material> getMaterial() {
        return this.materials;
    }

    public void addMaterial(Material material) {
        materials.add(material);
    }

    public void setIsDone(Boolean isDone) {
        this.isDone = isDone;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
