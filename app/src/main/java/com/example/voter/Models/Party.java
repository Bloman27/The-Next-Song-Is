package com.example.voter.Models;

import com.google.firebase.firestore.Exclude;

public class Party {

    private String host;
    private String name;
    private boolean isOngoing;

    //document id in firebase
    private String partyId;

    public Party(String host, boolean isOngoing, String name) {
        this.host = host;
        this.isOngoing = isOngoing;
        this.name = name;
    }

    public Party() {
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public boolean isOngoing() {
        return isOngoing;
    }

    @Exclude
    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

}
