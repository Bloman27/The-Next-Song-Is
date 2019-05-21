package com.example.voter.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.voter.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PartyStartedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PartyStartedFragment extends Fragment {

    String info = "";
    TextView textView;

    public PartyStartedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PartyStartedFragment.
     */
    public static PartyStartedFragment newInstance(String name, String userId, int numberOfSongs) {
        PartyStartedFragment fragment = new PartyStartedFragment();
        fragment.info = "Impreza " + name + " rozpoczęta!\n"
                + "Liczba piosenek: " + numberOfSongs + "\nGospodarz: " + userId;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_party_started, container, false);

        textView = (TextView) view.findViewById(R.id.textView2);

        textView.setText(info);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
