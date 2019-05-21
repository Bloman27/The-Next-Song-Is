package com.example.voter;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.voter.Activities.HostActivity.CHANNEL_ID;
import static java.lang.Thread.interrupted;


public class PartyService extends Service {

    public static final String PARTYID = "PartyId";
    public static final String PARTYNAME = "PartyName";
    private static  String partyId;
    private static  String partyName;
    public static boolean killThread = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        partyId = intent.getStringExtra(PARTYID);
        partyName = intent.getStringExtra(PARTYNAME);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(partyName)
                .setContentText("Impreza trwa")
                .setSmallIcon(R.drawable.ic_music_video_black_24dp)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!killThread){
                    getMostPopularSong(partyId);
                    try {
                        TimeUnit.MINUTES.sleep(3);
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }).start();

        startForeground(1, notification);


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopParty(partyId);
        killThread = true;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopParty(String partyId) {
        FirebaseFirestore db = Firebase.getInstance().getDb();
        Map<String, Object> map = new HashMap<>();
        map.put("ongoing", false);
        db.collection("Parties").document(partyId).update(map);
    }

    private void getMostPopularSong(String partyId){
        FirebaseFirestore db = Firebase.getInstance().getDb();
        CollectionReference songsRef = db.collection("Parties")
                .document(partyId).collection("Songs");
        Query query = songsRef.whereEqualTo("wasPlayed", false)
                .orderBy("votes", Query.Direction.DESCENDING)
                .limit(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                task.isSuccessful();
                if(task.isSuccessful()){
                    for(DocumentSnapshot document: task.getResult().getDocuments()){
                        String spotifyId = document.get("spotifyId").toString();
                        Spotify.getInstance().queueSong(spotifyId);
                        Map<String,Object> map = new HashMap<>();
                        map.put("wasPlayed", true);
                        songsRef.document(document.getId()).update(map);
                    }
                }
            }
        });
    }
}
