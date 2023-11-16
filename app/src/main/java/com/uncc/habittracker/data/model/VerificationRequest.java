package com.uncc.habittracker.data.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class VerificationRequest {
    private String firstName,lastName,userId, docId, currDocId;
    private Timestamp timestamp;

    public VerificationRequest(){

    }

    public VerificationRequest(String firstName, String lastName, String userId, String docId, String currDocId, Timestamp timestamp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.docId = docId;
        this.currDocId = currDocId;
        this.timestamp = timestamp;
    }

    public String getCurrDocId() {
        return currDocId;
    }

    public void setCurrDocId(String currDocId) {
        this.currDocId = currDocId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
        return dateFormat.format(timestamp.toDate());
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public String toString() {
        return "VerificationRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userId='" + userId + '\'' +
                ", docId='" + docId + '\'' +
                ", currDocId='" + currDocId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
