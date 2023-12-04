package com.uncc.habittracker.data.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;


public class Habit implements Serializable {

    private String frequency;
    private String habitTypeID;
    private String nameOverride;
    private String userId;



    private String docId;
    private int progress;
    private Timestamp createdAt;
    public Habit() {

    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }



    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getHabitTypeID() {
        return habitTypeID;
    }

    public void setHabitTypeID(String habitTypeID) {
        this.habitTypeID = habitTypeID;
    }

    public String getNameOverride() {
        return nameOverride;
    }

    public void setNameOverride(String nameOverride) {
        this.nameOverride = nameOverride;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Habit{" +
                "frequency='" + frequency + '\'' +
                ", habitTypeID='" + habitTypeID + '\'' +
                ", nameOverride='" + nameOverride + '\'' +
                ", userId='" + userId + '\'' +
                ", docId='" + docId + '\'' +
                ", progress=" + progress +
                ", createdAt=" + createdAt +
                '}';
    }
}