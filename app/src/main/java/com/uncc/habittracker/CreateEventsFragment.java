package com.uncc.habittracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uncc.habittracker.databinding.FragmentCreateEventsBinding;
import com.uncc.habittracker.databinding.FragmentCreateHabitBinding;

public class CreateEventsFragment extends Fragment {


    public CreateEventsFragment() {
        // Required empty public constructor
    }
    FragmentCreateEventsBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        binding = FragmentCreateEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create Event");
    }


}