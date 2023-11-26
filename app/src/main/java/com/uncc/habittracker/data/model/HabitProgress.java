package com.uncc.habittracker.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class HabitProgress implements Serializable {
    private String habitType, source, userHabitDocId, eventDocId, userId, docId;
    private Timestamp progressDate, createdAt;

    public HabitProgress() {

    }

    public String getHabitType() {
        return habitType;
    }

    public void setHabitType(String habitType) {
        this.habitType = habitType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUserHabitDocId() {
        return userHabitDocId;
    }

    public void setUserHabitDocId(String userHabitDocId) {
        this.userHabitDocId = userHabitDocId;
    }

    public String getEventDocId() {
        return eventDocId;
    }

    public void setEventDocId(String eventDocId) {
        this.eventDocId = eventDocId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getProgressDate() {
        return progressDate;
    }

    public void setProgressDate(Timestamp progressDate) {
        this.progressDate = progressDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}