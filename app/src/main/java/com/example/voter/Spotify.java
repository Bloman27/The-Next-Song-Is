package com.example.voter;

import android.app.Activity;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Empty;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.UserPrivate;

import static android.support.constraint.Constraints.TAG;

public class Spotify {

    //Spotify
    private final String CLIENT_ID = "xxx";
    private final String REDIRECT_URI = "xxx";
    private final int REQUEST_CODE = 1337;
    private SpotifyApi spotifyApi;

    // Contains user playlists
    private Pager<PlaylistSimple> playlistsList;

    // Contains playlist's songs
    private Pager<PlaylistTrack> playlistTrackPager;

    // Contains logged in user's data
    private UserPrivate userPrivate;

    private CountDownLatch countDownLatch;

    private static Spotify INSTANCE;

    private SpotifyAppRemote mSpotifyAppRemote;

    private Activity mActivity;

    private Spotify() {
    }

    public static Spotify getInstance() {
        if (INSTANCE == null)
            synchronized (Spotify.class) {
                if (INSTANCE == null)
                    INSTANCE = new Spotify();
            }
        return INSTANCE;
    }

    public int getRequestCode() {
        return REQUEST_CODE;
    }

    public SpotifyApi getSpotifyApi() {
        if(spotifyApi == null)
            spotifyApi = new SpotifyApi();
        return spotifyApi;
    }

    public void downloadUserData(){
        SpotifyService spotify = getSpotifyApi().getService();
        countDownLatch = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                userPrivate = spotify.getMe();
                System.out.println("id " + userPrivate.id);
                countDownLatch.countDown();
            }
        }).start();

        try{
            countDownLatch.await();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void downloadPlaylists(){
        SpotifyService spotify = getSpotifyApi().getService();
        countDownLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                playlistsList = spotify.getMyPlaylists();
                for(int i = 0; i < playlistsList.items.size(); i++){
                    Log.d("HostActivity",playlistsList.items.get(i).name);
                    Log.d("HostActivity", Integer.toString(playlistsList.items.get(i).tracks.total));
                    Log.d("HostActivity", playlistsList.items.get(i).id);
                }
                countDownLatch.countDown();
            }
        }).start();

        try{
            countDownLatch.await();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void downloadSongs(String playlistId) {
        SpotifyService spotify = getSpotifyApi().getService();
        countDownLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                playlistTrackPager = spotify.getPlaylistTracks(userPrivate.id,playlistId);
                for(int i = 0; i < playlistTrackPager.items.size(); i++){
                    Log.d("HostActivity",playlistTrackPager.items.get(i).track.name);
                }
                countDownLatch.countDown();
            }
        }).start();

        try{
            countDownLatch.await();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Pager<PlaylistTrack> getPlaylistTrackPager() {
        return playlistTrackPager;
    }

    public Pager<PlaylistSimple> getPlaylistsList() {
        return playlistsList;
    }

    public UserPrivate getUserPrivate() {
        return userPrivate;
    }

    public void spotifyLogin(Activity activity){
        mActivity = activity;
        // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.setDebugMode(true);
        SpotifyAppRemote.connect(activity, connectionParams,
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                         mSpotifyAppRemote = spotifyAppRemote;

                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
    }

    public Map<String, String> parsePager(Pager<PlaylistSimple> pager){

        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < pager.items.size(); i++){
            map.put(pager.items.get(i).name, pager.items.get(i).id);
        }

        return map;
    }

    public void queueSong(String spotifyTrackId){
        connectSpotifyAppRemote(mActivity);
        CallResult<Empty> result = mSpotifyAppRemote.getPlayerApi().queue("spotify:track:" + spotifyTrackId);
        result.setResultCallback(new CallResult.ResultCallback<Empty>() {
            @Override
            public void onResult(Empty empty) {
                Log.d(TAG, "Track queued");
            }
        });
    }


    private void connectSpotifyAppRemote(Activity activity){
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.setDebugMode(true);

        SpotifyAppRemote.connect(activity, connectionParams,
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
    }


}