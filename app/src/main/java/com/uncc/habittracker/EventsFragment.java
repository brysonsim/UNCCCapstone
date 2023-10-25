package com.uncc.habittracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.uncc.habittracker.databinding.FragmentEventsBinding;
import com.uncc.habittracker.databinding.FragmentHabitsBinding;

public class EventsFragment extends Fragment {
    FragmentEventsBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public EventsFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Events");
        String user = mAuth.getCurrentUser().toString();
        Log.d("TAG", "The current user is" +user);
        binding.textViewEvent.setText("information");
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

    interface EventsListener
    {
        void createNewEvent();
    }


}