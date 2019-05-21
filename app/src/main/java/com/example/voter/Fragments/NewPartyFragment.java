package com.example.voter.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.voter.Firebase;
import com.example.voter.PartyService;
import com.example.voter.R;
import com.example.voter.Spotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewPartyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewPartyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewPartyFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    Map<String, String> playlists;
    Spinner spinner;
    EditText editText;
    Button startNewPartyButton;
    Spotify spotify;
    Firebase firebase;


    public NewPartyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewPartyFragment.
     */
    public static NewPartyFragment newInstance(Map<String, String> playlists) {
        NewPartyFragment fragment = new NewPartyFragment();
        fragment.playlists = playlists;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotify = Spotify.getInstance();
        firebase = Firebase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_party, container, false);

        editText = (EditText) view.findViewById(R.id.editText);

        spinner = (Spinner) view.findViewById(R.id.spinner2);
        List<String> list = new ArrayList<>();
        list.addAll(playlists.keySet());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                list);

        spinner.setAdapter(spinnerAdapter);

        startNewPartyButton = (Button) view.findViewById(R.id.startNewParty);
        startNewPartyButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Log.d("NewPartyFragment", editText.getText().toString());
                Log.d("NewPartyFragment", "Spinner" + playlists.get(spinner.getSelectedItem().toString()));
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), R.string.noTitle, Toast.LENGTH_SHORT).show();

                } else {
                    spotify.downloadSongs(playlists.get(spinner.getSelectedItem().toString()));

                    Log.d("NewPartyFragment", spotify.getPlaylistTrackPager().toString());
                    firebase.createParty(editText.getText().toString(), spotify.getUserPrivate().id, spotify.getPlaylistTrackPager());

                    startService(v);
                    loadFragment(PartyStartedFragment.newInstance(editText.getText().toString(), spotify.getUserPrivate().id, spotify.getPlaylistTrackPager().items.size()));
                }
            }
        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString()
            //        + " must implement OnFragmentInteractionListener");
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

    private void startService(View v){
        Intent serviceIntent = new Intent(getContext(), PartyService.class);
        serviceIntent.putExtra(PartyService.PARTYID, "xdx");
        serviceIntent.putExtra(PartyService.PARTYNAME, "Bania u cygana");

        getActivity().startService(serviceIntent);
    }

}
