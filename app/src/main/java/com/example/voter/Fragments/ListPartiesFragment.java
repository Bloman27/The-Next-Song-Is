package com.example.voter.Fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.voter.Firebase;
import com.example.voter.Models.Party;
import com.example.voter.PartyService;
import com.example.voter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListPartiesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListPartiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListPartiesFragment extends Fragment {

    private ActionBar toolbar;
    private OnFragmentInteractionListener mListener;

    Map<String, Party> parties;

    private ArrayAdapter<String> listViewAdapter;
    private ListView listView;
    private List<String> list;
    private String ongoingPartyId = "";

    String tempPartyNameForContextMenu = "";


    public ListPartiesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListPartiesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListPartiesFragment newInstance() {
        ListPartiesFragment fragment = new ListPartiesFragment();
        fragment.parties = new HashMap<>();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(R.string.loading);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_parties, container, false);

        listView = (ListView) view.findViewById(R.id.partiesList);
        setContentInListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String partyName = (String) listView.getItemAtPosition(position);
                Party party = parties.get(partyName);
                loadFragment(DisplayPartyFragment.newInstance(party.getPartyId(), partyName));
            }
        });
        registerForContextMenu(listView);

        getParties("polkotoi");

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        tempPartyNameForContextMenu = (String) listView.getItemAtPosition(acmi.position);

        getActivity().getMenuInflater().inflate(R.menu.party_floating_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.startParty:
                if (!isMyServiceRunning(PartyService.class)) {
                    startService(parties.get(tempPartyNameForContextMenu).getPartyId(), tempPartyNameForContextMenu);
                    Toast.makeText(getActivity(), R.string.partyStartedSuccess, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.partyOngoing, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.pauseParty:
                if (isMyServiceRunning(PartyService.class)) {
                    stopService(getView());
                    Toast.makeText(getActivity(), R.string.partyStopped, Toast.LENGTH_SHORT).show();

                } else
                    Toast.makeText(getActivity(), R.string.noPartyOngoing, Toast.LENGTH_SHORT).show();
                break;
            case R.id.deleteParty:
                deleteParty(parties.get(tempPartyNameForContextMenu).getPartyId());
                Toast.makeText(getActivity(), R.string.partyDeleted, Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
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
        void onFragmentInteraction(Uri uri);
    }

    private void getParties(String userId) {

        FirebaseFirestore db = Firebase.getInstance().getDb();

        Map<String, Party> map = new HashMap<>();
        CollectionReference partiesRef = db.collection("Parties");

        Query userPartiesQuery = partiesRef.whereEqualTo("host", userId);

        userPartiesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("DisplayPartyFragment", "listen:error", e);
                    return;
                }
                Party party;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New party: " + dc.getDocument().getData());
                            party = dc.getDocument().toObject(Party.class);
                            party.setPartyId(dc.getDocument().getId());
                            parties.put(dc.getDocument().getData().get("name").toString(), party);
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified party: " + dc.getDocument().getData());
                            party = dc.getDocument().toObject(Party.class);
                            party.setPartyId(dc.getDocument().getId());
                            parties.put(dc.getDocument().getData().get("name").toString(), party);
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed party: " + dc.getDocument().getData());
                            parties.remove(dc.getDocument().getData().get("name").toString());
                            break;
                    }
                }
                list.clear();
                list.addAll(parties.keySet());
                listViewAdapter.notifyDataSetChanged();
                toolbar.setTitle(R.string.title_list_parties);
            }

        });
    }

    private void startParty(String partyId) {
        FirebaseFirestore db = Firebase.getInstance().getDb();
        Map<String, Object> map = new HashMap<>();
        map.put("ongoing", true);
        db.collection("Parties").document(partyId).update(map);
    }

    private void stopParty(String partyId) {
        FirebaseFirestore db = Firebase.getInstance().getDb();
        Map<String, Object> map = new HashMap<>();
        map.put("ongoing", false);
        db.collection("Parties").document(partyId).update(map);
        PartyService.killThread = true;
    }

    private void deleteParty(String partyId) {
        FirebaseFirestore db = Firebase.getInstance().getDb();

        db.collection("Parties").document(partyId).delete();
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setContentInListView() {
        list = new ArrayList<>();
        list.addAll(parties.keySet());
        listViewAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                list);

        listView.setAdapter(listViewAdapter);
    }

    private void startService(String partyId, String partyName) {
        Intent serviceIntent = new Intent(getContext(), PartyService.class);
        serviceIntent.putExtra(PartyService.PARTYID, partyId);
        serviceIntent.putExtra(PartyService.PARTYNAME, partyName);

        getActivity().startService(serviceIntent);
        ongoingPartyId = partyId;
        startParty(partyId);
    }

    private void stopService(View v) {
        Intent serviceIntent = new Intent(getContext(), PartyService.class);
        getActivity().stopService(serviceIntent);
        if (!ongoingPartyId.equals(""))
            stopParty(ongoingPartyId);

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
