package com.example.voter.Fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.voter.Firebase;
import com.example.voter.ListViewAdapter;
import com.example.voter.Models.Party;
import com.example.voter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.javatuples.Pair;
import org.javatuples.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayPartyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DisplayPartyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayPartyFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    //List<Song> songs;
    ListView listView;
    String partyId;
    String partyName;
    private ActionBar toolbar;
    private List<Map<String, String>> list2;
    private Map<String, Pair<String, String>> songs;
    private ListViewAdapter listViewAdapter;
    private List<String> listOfSongsVotedFor;
    /*
    Contains map in format: String key = songId. Boolean value = wasVoted
     */
    SharedPreferences sharedPref;
    boolean defaultValue = false;

    public DisplayPartyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DisplayPartyFragment.
     */
    public static DisplayPartyFragment newInstance(String partyId, String partyName) {
        DisplayPartyFragment fragment = new DisplayPartyFragment();
        fragment.songs = new HashMap<>();
        fragment.partyId = partyId;
        fragment.partyName = partyName;
        fragment.listOfSongsVotedFor = new ArrayList<>();

        return fragment;
    }

    public static DisplayPartyFragment newInstance(String partyId) {
        DisplayPartyFragment fragment = new DisplayPartyFragment();
        fragment.songs = new HashMap<>();
        fragment.partyId = partyId;
        fragment.listOfSongsVotedFor = new ArrayList<>();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(R.string.loading);
        Context context = getActivity();
        sharedPref = context.getSharedPreferences(partyId, Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_party, container, false);

        listView = (ListView) view.findViewById(R.id.songsList);
        setContentInListView2();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> song = (Map<String, String>) listView.getItemAtPosition(position);
                String songName = song.get(ListViewAdapter.FIRST_COLUMN);

                if (sharedPref.getBoolean(songs.get(songName).getValue0(), defaultValue)) {
                    Toast.makeText(getActivity(), R.string.alreadyVoted, Toast.LENGTH_SHORT).show();
                } else {
                    sendVote(songs.get(songName).getValue0(), partyId);
                    listOfSongsVotedFor.add(songs.get(songName).getValue0());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(songs.get(songName).getValue0(), true);
                    editor.commit();
                }

            }
        });

        getSongs(partyId);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString()
            //       + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void getSongs(String partyId) {

        FirebaseFirestore db = Firebase.getInstance().getDb();

        DocumentReference partyRef = db.collection("Parties").document(partyId);
        CollectionReference songsRef = partyRef.collection("Songs");

        Query songsQuery = songsRef.whereEqualTo("wasPlayed", false);

        songsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("DisplayPartyFragment", "listen:error", e);
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New song: " + dc.getDocument().getData());
                            songs.put(dc.getDocument().getData().get("title").toString(),
                                    new Pair<>(dc.getDocument().getId(), dc.getDocument().getData().get("votes").toString()));
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified song: " + dc.getDocument().getData());
                            songs.put(dc.getDocument().getData().get("title").toString(),
                                    new Pair<>(dc.getDocument().getId(), dc.getDocument().getData().get("votes").toString()));
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed song: " + dc.getDocument().getData());
                            break;
                    }
                }
                list2.clear();
                for (String s : songs.keySet()) {
                    Map<String, String> map2 = new HashMap<>();
                    map2.put(ListViewAdapter.FIRST_COLUMN, s);
                    map2.put(ListViewAdapter.SECOND_COLUMN, songs.get(s).getValue1());
                    if (sharedPref.getBoolean(songs.get(s).getValue0(), defaultValue))
                        map2.put(ListViewAdapter.COLOR, "green");
                    list2.add(map2);
                }
                listViewAdapter.notifyDataSetChanged();

                if (partyName != null)
                    toolbar.setTitle(partyName);

            }
        });

    }

    private void setContentInListView2() {
        list2 = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(
                getActivity(), list2);
        if (listView == null)
            Log.d("Null", "listView");
        listView.setAdapter(listViewAdapter);
    }

    private void sendVote(String songId, String partyId) {

        FirebaseFirestore db = Firebase.getInstance().getDb();

        DocumentReference songRef = db.collection("Parties").document(partyId)
                .collection("Songs").document(songId);
        Log.d("x", partyId + " " + songId);
        songRef.update("votes", FieldValue.increment(1));


    }

}


