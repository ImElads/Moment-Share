package com.example.momentshare.Classes;

public class User {
    private String id;

    private String email;
    private String username;
    private String profileImageUrl;


    public User(String id, String email, String username){
        this.id  = id;
        this.email = email;
        this.username = username;
    }

    public User(){

    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl){this.profileImageUrl = profileImageUrl;}

}
