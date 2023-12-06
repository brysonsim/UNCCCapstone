package com.uncc.habittracker.data.model;

public class Follower {
    private String docID, followingID, userID;

    public Follower() {
    }

    public Follower(String docID, String followingID, String userID) {
        this.docID = docID;
        this.followingID = followingID;
        this.userID = userID;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getFollowingID() {
        return followingID;
    }

    public void setFollowingID(String followingID) {
        this.followingID = followingID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
