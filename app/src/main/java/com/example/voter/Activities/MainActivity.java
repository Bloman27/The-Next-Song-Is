package com.example.voter.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.voter.R;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    /** Called when the user taps the startParty button */
    public void startHostActivity(View view) {
        Intent intent = new Intent(this, HostActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the startParty button */
    public void startGuestActivity(View view) {
        Intent intent = new Intent(this, GuestActivity.class);
        startActivity(intent);
    }
}
