package com.uncc.habittracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uncc.habittracker.data.model.Event;
import com.uncc.habittracker.databinding.EventRowItemBinding;
import com.uncc.habittracker.databinding.FragmentEventsBinding;
import com.uncc.habittracker.databinding.FragmentHabitsBinding;

import java.util.ArrayList;
import java.security.Timestamp;


public class EventsFragment extends Fragment {
    FragmentEventsBinding binding;
    ArrayList<Event> mEvents = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public EventsFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    EventsAdapter adapter;
    ListenerRegistration listenerRegistration;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EventsAdapter(this.mListener, getActivity());
        binding.recyclerViewEvents.setAdapter(adapter);

        listenerRegistration = FirebaseFirestore.getInstance().collection("events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null)
                {
                    error.printStackTrace();
                    return;
                }
                Log.d("demo", "inside onEvent ");
                mEvents.clear();
                int c = 1;
                for(QueryDocumentSnapshot doc: value)
                {
                    Log.d("demo", "iteration: " + c );
                    c++;
                    Event event= doc.toObject(Event.class);
                    Log.d("demo", "converted obj");
                    mEvents.add(event);
                }
                adapter.notifyDataSetChanged();
            }
        });

        String user = mAuth.getCurrentUser().toString();
        Log.d("TAG", "The current user is" +user);
        binding.buttonCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.createNewEvent();
            }
        });

    }

    EventsListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (EventsListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Events");
    }

    interface EventsListener
    {
        void createNewEvent();

        void viewEvent(Event event);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(listenerRegistration != null){
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }


    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder>
    {
        EventsListener listener;

        private Context context;
        public EventsAdapter(EventsListener listener, Context context)
        {
            super();
            this.listener = listener;
            this.context = context;
        }
        @NonNull
        @Override
        public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            EventRowItemBinding itemBinding = EventRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new EventsViewHolder(itemBinding, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {
            Event event = mEvents.get(position);
            holder.setupUI(event);
        }

        @Override
        public int getItemCount() {
            return mEvents.size();
        }

        class EventsViewHolder extends RecyclerView.ViewHolder
            {
            EventRowItemBinding mBinding;
            Event mEvent;
            EventsListener listener;

            public EventsViewHolder(EventRowItemBinding itemBinding, EventsListener listener) {
                super(itemBinding.getRoot());
                mBinding = itemBinding;
                this.listener = listener;
            }
            public void setupUI(Event event){
                this.mEvent = event;
                mBinding.textViewForumCreatedBy.setText(mEvent.getOwnerName());
                mBinding.textViewEventTitle.setText(mEvent.getTitle());
                mBinding.textViewDescription.setText(mEvent.getDescription());
                //needs to change to change the number of attendees
                mBinding.textViewEventNumerOfAttendees.setText(mEvent.getHabitType());

                if(mAuth.getCurrentUser().getUid().equals(mEvent.getOwnerId()))
                {
                    mBinding.imageViewDelete.setVisibility(View.VISIBLE);
                    mBinding.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                }
                else{mBinding.imageViewDelete.setVisibility(View.INVISIBLE);}

                mBinding.cardViewEvent.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        listener.viewEvent(mEvent);
                    }
                });

            }
        }
    }


}