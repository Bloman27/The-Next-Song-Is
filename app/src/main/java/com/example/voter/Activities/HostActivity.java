// TODO dowiedzieć czemu w Fragmentach jest ten throw exception

package com.example.voter.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.voter.Firebase;
import com.example.voter.Fragments.ListPartiesFragment;
import com.example.voter.Fragments.ListPlaylistsFragment;
import com.example.voter.Fragments.NewPartyFragment;
import com.example.voter.R;
import com.example.voter.Spotify;
import com.google.firebase.FirebaseApp;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class HostActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "partyChannel";

    //Firebase
    Firebase firebase;

    //Spotify
    Spotify spotify;

    // UI elements
    private ActionBar toolbar;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_new_party:
                    toolbar.setTitle(R.string.title_new_party);
                    fragment = NewPartyFragment.newInstance(spotify.parsePager(spotify.getPlaylistsList()));
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_list_parties:
                    toolbar.setTitle(R.string.title_list_parties);
                    fragment = ListPartiesFragment.newInstance();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_list_playlists:
                    toolbar.setTitle(R.string.title_list_playlists);
                    fragment = ListPlaylistsFragment.newInstance(spotify.parsePager(spotify.getPlaylistsList()));
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        toolbar = getSupportActionBar();
        toolbar.setTitle(R.string.loading);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        spotify = Spotify.getInstance();
        spotify.spotifyLogin(this);

        FirebaseApp.initializeApp(this);
        firebase = Firebase.getInstance();

        createNotificationChannel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("HostActivity", "Connected! Yay!");
        // Check if result comes from the correct activity
        if (requestCode == spotify.getRequestCode()) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    String myAccessToken = response.getAccessToken();
                    Log.d("HostActivity", myAccessToken);
                    // Handle successful response

                    spotify.getSpotifyApi();
                    // Most (but not all) of the Spotify Web API endpoints require authorisation.
                    // If you know you'll only use the ones that don't require authorisation you can skip this step
                    spotify.getSpotifyApi().setAccessToken(myAccessToken);

                    spotify.downloadUserData();
                    spotify.downloadPlaylists();
                    Fragment fragment;
                    fragment = NewPartyFragment.newInstance(spotify.parsePager(spotify.getPlaylistsList()));
                    toolbar.setTitle(R.string.title_new_party);
                    loadFragment(fragment);

                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }


    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "EXAMPLE",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }
}
