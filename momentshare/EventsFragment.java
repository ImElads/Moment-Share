package com.example.momentshare;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.momentshare.Adapters.EventAdapter;
import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.User;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment implements Model.IModelUpdate {
    Model model;
    TextView tvNoEvents;
    RecyclerView rcvEventList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance(String param1, String param2) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        model = Model.getInstance();
        model.registerModelUpdate(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        tvNoEvents = view.findViewById(R.id.tv_no_events);
        rcvEventList = view.findViewById(R.id.rcv_event_list);

        ArrayList<Event> events = model.getEvents();

        if (events.isEmpty()){
            tvNoEvents.setVisibility(View.VISIBLE);
            rcvEventList.setVisibility(View.GONE);
        }

        rcvEventList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        EventAdapter adapter = new EventAdapter(view.getContext(), events);
        rcvEventList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (rcvEventList.getAdapter() != null) {
            rcvEventList.getAdapter().notifyDataSetChanged();
        }
        if (model.getEvents().isEmpty()){
            tvNoEvents.setVisibility(View.VISIBLE);
            rcvEventList.setVisibility(View.GONE);
        }
        else {
            tvNoEvents.setVisibility(View.GONE);
            rcvEventList.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void eventDataChanged(Exception ex) {

    }

    @Override
    public void userRegistrationCompleted(Exception ex) {

    }

    @Override
    public void userLoginCompleted(Exception ex) {
        tvNoEvents.setText("Loading...");
    }


    @Override
    public void userDataLoaded(Exception ex) {

    }

    @Override
    public void eventDataLoaded(Exception e) {
        if (rcvEventList.getAdapter() != null) {
            rcvEventList.getAdapter().notifyDataSetChanged();
        }

        if (model.getEvents().isEmpty()){
            tvNoEvents.setVisibility(View.VISIBLE);
            rcvEventList.setVisibility(View.GONE);
        }
        else {
            tvNoEvents.setVisibility(View.GONE);
            rcvEventList.setVisibility(View.VISIBLE);
        }
        tvNoEvents.setText("You are not a participant of any event");
    }

    @Override
    public void raiseMediaUploadComplete(Exception e, String url) {

    }

    @Override
    public void mediaPostChanged(Exception ex, Event event) {

    }

    @Override
    public void hostDataLoaded(Exception e, User user) {

    }

    @Override
    public void joinEventCompleted(Exception e) {

    }

    @Override
    public void createEventCompleted(Exception e) {

    }
}