package com.example.momentshare.Classes;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String id;

    private final ArrayList<MediaPost> mediaPosts = new ArrayList<>();

    private String title;
    private String description;
    private String location;
    private String eventImageUrl;

    private final String hostId;
    private final ArrayList<String> participantIds = new ArrayList<>();

    private Date dateScheduled;
    private final Date dateCreated;

    public Event(String title, String description, String location, Date dateEventPlanned){
        this.title = title;
        this.description = description;
        this.dateScheduled = dateEventPlanned;
        this.location = location;
        dateCreated = new Date();
        hostId = Model.getInstance().getCurrentUser().getId();
        participantIds.add(hostId);
    }
    public Event(){
        dateCreated = new Date();
        hostId = Model.getInstance().getCurrentUser().getId();
        participantIds.add(hostId);
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return title;
    }
    public void setName(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getEventImageUrl() {
        return eventImageUrl;
    }
    public void setEventImageUrl(String eventImageUrl) {
        this.eventImageUrl = eventImageUrl;
    }
    public String getHostId() {
        return hostId;
    }
    public ArrayList<MediaPost> getMediaPosts() {
        return mediaPosts;
    }
    public ArrayList<String> getParticipantIds() {
        return participantIds;
    }
    public Date getDateScheduled() {
        return dateScheduled;
    }
    public void setDateScheduled(Date dateScheduled) {
        this.dateScheduled = dateScheduled;
    }
    public Date getDateCreated() {
        return dateCreated;
    }

    public void addParticipant(String participantId){
        participantIds.add(participantId);
    }

    public boolean isUserHost(User user){
        if (user.getId().equals(hostId))
            return true;
        return false;
    }
}