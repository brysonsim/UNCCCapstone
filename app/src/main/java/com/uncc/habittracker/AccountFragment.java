package com.uncc.habittracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uncc.habittracker.databinding.FragmentHabitsBinding;
import com.uncc.habittracker.databinding.FragmentSettingsBinding;
import com.uncc.habittracker.databinding.FragmentUserAccountBinding;

public class AccountFragment extends Fragment {
    FragmentUserAccountBinding binding;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Account");
    }
    //AccountListener aListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //sListener = (SettingsFragment.SettingsListener) context;
    }

}

