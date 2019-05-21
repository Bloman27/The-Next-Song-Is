package com.example.voter.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.voter.Fragments.JoinPartyFragment;
import com.example.voter.R;

public class GuestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, JoinPartyFragment.newInstance())
                    .commitNow();
        }

    }


}
