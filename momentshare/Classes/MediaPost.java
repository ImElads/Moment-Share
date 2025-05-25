package com.example.momentshare.Classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MediaPost {
    private String id;

    private User uploader;
    private String description;
    private Date uploadTime;

    private ArrayList<String> mediaUrls = new ArrayList<>();

    public MediaPost(String description){
        this.description = description;
        uploader = Model.getInstance().getCurrentUser();
        uploadTime = new Date();
    }
    //not sure its needed
    public MediaPost(){
        uploader = Model.getInstance().getCurrentUser();
        uploadTime = new Date();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //not sure its needed
    public void setDescription (String description) {
        this.description = description;
    }
    public ArrayList<String> getMediaUrls() {
        return mediaUrls;
    }
    public void setMediaUrls(ArrayList<String> mediaUrls){this.mediaUrls = mediaUrls;}

    public User getUploader() {
        return uploader;
    }

    public String getDescription() {
        return description;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public String getTimeSinceUpload() {
        long diffInMillis = new Date().getTime() - uploadTime.getTime();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }

        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        }

        long months = days / 30; // Approximate
        return months + " month" + (months == 1 ? "" : "s") + " ago";
    }

}
