package com.uncc.habittracker.data.model;

import java.util.ArrayList;

/*
 * This class is the user model
 * This will store basic information about the user
 * also be able to call their habits
 */

public class User {
    //user fields
    private String firstName, lastName, uid, about, firebaseUid, profilePhoto;
    private ArrayList<Habit> Habits;

    private Boolean isVerified;

    public User() {

    }
    //generated constructor
    public User(String firstName, String lastName, String uid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.uid = uid;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public ArrayList<Habit> getHabits() {
        return Habits;
    }

    public void setHabits(ArrayList<Habit> habits) {
        Habits = habits;
    }

    public String getDisplayName() { return firstName + " " + lastName; }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}
