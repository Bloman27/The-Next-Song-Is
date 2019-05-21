package com.example.voter.Models;

import com.google.firebase.firestore.Exclude;

public class Song {

    private String documentId;
    private String artists;
    private String spotifyId;
    private String title;
    private int votes;
    private boolean wasPlayed;

    public Song() {

    }

    public Song(String artists, String spotifyId, String title, int votes, boolean wasPlayed) {
        this.artists = artists;
        this.title = title;
        this.spotifyId = spotifyId;
        this.votes = votes;
        this.wasPlayed = wasPlayed;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public int getVotes() {
        return votes;
    }

    public String getTitle() {
        return title;
    }

    public String getArtists() {
        return artists;
    }

    public boolean isWasPlayed() {
        return wasPlayed;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
