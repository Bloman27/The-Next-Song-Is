package com.example.voter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.voter.Models.Party;
import com.example.voter.Models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistTrack;

public class Firebase {

    //Firebase
    private FirebaseFirestore db;

    private static Firebase INSTANCE;

    private Firebase() {
        db = FirebaseFirestore.getInstance();
    }

    public static Firebase getInstance() {
        if (INSTANCE == null)
            synchronized (Firebase.class) {
                if (INSTANCE == null)
                    INSTANCE = new Firebase();
            }
        return INSTANCE;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void createParty(String name, String host, Pager<PlaylistTrack> playlistTrackPager){

        db.collection("Parties")
                .add(new Party(host, false, name))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        Log.d("Firebase", "DocumentSnapshot (Party) written with ID: " + documentReference.getId());
                        WriteBatch batch = db.batch();

                        for(int i = 0; i < playlistTrackPager.items.size(); i++) {

                            String artists = playlistTrackPager.items.get(i).track.artists.get(0).name;
                            for (int j = 1; j < playlistTrackPager.items.get(i).track.artists.size(); j++) {
                                artists += " ";
                                artists += playlistTrackPager.items.get(i).track.artists.get(j).name;
                            }

                            Song song = new Song(artists,playlistTrackPager.items.get(i).track.id,
                                    playlistTrackPager.items.get(i).track.name,0,false);

                            DocumentReference songRef = documentReference.collection("Songs").document();

                            batch.set(songRef, song);
                        }
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Log.d("Firebase", "Batch (Songs) commit successful");
                                else
                                    Log.d("Firebase", "Batch (Songs) commit failed");
                            }
                        });
                    }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "Error writing document", e);
                    }
                });
    }

}
