package com.uncc.habittracker.data.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Habit implements Serializable {
    public Timestamp createdAt;
    public String name, docId, userId;

    public Habit() {

    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
