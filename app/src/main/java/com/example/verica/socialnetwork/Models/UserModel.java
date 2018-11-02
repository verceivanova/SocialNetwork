package com.example.verica.socialnetwork.Models;

/**
 * Created by AliAh on 30/10/2018.
 */

public class UserModel {
    String country,dob,fullname,gender,profileimage,relationshipstatus,status,username,fcmKey;

    public UserModel(String country, String dob, String fullname, String gender, String profileimage, String relationshipstatus, String status, String username, String fcmKey) {
        this.country = country;
        this.dob = dob;
        this.fullname = fullname;
        this.gender = gender;
        this.profileimage = profileimage;
        this.relationshipstatus = relationshipstatus;
        this.status = status;
        this.username = username;
        this.fcmKey = fcmKey;
    }

    public String getFcmKey() {
        return fcmKey;
    }

    public void setFcmKey(String fcmKey) {
        this.fcmKey = fcmKey;
    }

    public UserModel() {
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getRelationshipstatus() {
        return relationshipstatus;
    }

    public void setRelationshipstatus(String relationshipstatus) {
        this.relationshipstatus = relationshipstatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
