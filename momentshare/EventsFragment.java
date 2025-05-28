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
    EventAdapter adapter; // Declare adapter as class variable to update it later

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public EventsFragment() {
        // Required empty public constructor
    }

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the views
        tvNoEvents = view.findViewById(R.id.tv_no_events);
        rcvEventList = view.findViewById(R.id.rcv_event_list);

        // Set the LayoutManager for RecyclerView
        rcvEventList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Initialize the adapter only once with initial events data
        ArrayList<Event> events = model.getEvents();
        adapter = new EventAdapter(view.getContext(), events);
        rcvEventList.setAdapter(adapter);

        // Show appropriate message if no events
        updateEventVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensure the adapter gets notified when the data changes
        if (adapter != null && model.getEvents() != null) {
            adapter.notifyDataSetChanged();
        }

        // Show or hide the no events message
        updateEventVisibility();
    }

    @Override
    public void eventDataChanged(Exception ex) {
        // Handle event data changes if necessary (e.g., errors)
    }

    @Override
    public void userRegistrationCompleted(Exception ex) {
        // Handle user registration completion (e.g., after sign up)
    }

    @Override
    public void userLoginCompleted(Exception ex) {
        tvNoEvents.setText("Loading...");
    }

    @Override
    public void userDataLoaded(Exception ex) {
        // Handle user data loaded callback
    }

    @Override
    public void eventDataLoaded(Exception e) {
        // When new event data is loaded, refresh the list in the adapter
        if (adapter != null && model.getEvents() != null) {
            adapter.notifyDataSetChanged();  // Notify the adapter that data has changed
        }

        // Show or hide the no events message
        updateEventVisibility();
        tvNoEvents.setText("You are not a participant of any event");
    }

    @Override
    public void raiseMediaUploadComplete(Exception e, String url) {
        // Handle media upload completion
    }

    @Override
    public void mediaPostChanged(Exception ex) {
        // Handle media post changes
    }

    @Override
    public void hostDataLoaded(Exception e, User user) {
        // Handle host data loaded callback
    }

    @Override
    public void joinEventCompleted(Exception e) {
        // Handle join event completion
    }

    @Override
    public void createEventCompleted(Exception e, String id) {
        // Handle event creation completion
    }

    // Helper method to handle visibility of events and the 'no events' message
    private void updateEventVisibility() {
        if (model.getEvents().isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            rcvEventList.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            rcvEventList.setVisibility(View.VISIBLE);
        }
    }
}
