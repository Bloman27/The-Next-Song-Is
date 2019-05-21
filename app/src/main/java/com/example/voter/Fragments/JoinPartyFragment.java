package com.example.voter.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.voter.Firebase;
import com.example.voter.Models.Party;
import com.example.voter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class JoinPartyFragment extends Fragment {

    private ActionBar toolbar;
    EditText editText;

    public static JoinPartyFragment newInstance() {
        return new JoinPartyFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_join_party, container, false);
        Button joinButton = (Button) view.findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setTitle(R.string.loading);
                editText = (EditText) view.findViewById(R.id.editText2);
                Log.d("JoinPartyFragment", editText.getText().toString());
                getParty(editText.getText().toString());
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(R.string.join_party);
    }


    private void loadFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow();
    }


    private void getParty(String documentId) {

        FirebaseFirestore db = Firebase.getInstance().getDb();

        Map<String, String> map = new HashMap<>();
        DocumentReference partyRef = db.collection("Parties").document(documentId);//.document("B08EDY0sDepS1rmTwV4l").collection("Songs");

        partyRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().get("name") != null) {
                        Party party = task.getResult().toObject(Party.class);
                        if(task.getResult().getBoolean("ongoing"))
                            loadFragment(DisplayPartyFragment.newInstance(editText.getText().toString(), party.getName()));
                        else
                            Toast.makeText(getActivity(), R.string.partyStopped, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.wrongPartyId, Toast.LENGTH_SHORT).show();
                        toolbar.setTitle(R.string.join_party);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.connectionProblems, Toast.LENGTH_SHORT).show();
                    toolbar.setTitle(R.string.join_party);
                }
            }
        });

    }


}
