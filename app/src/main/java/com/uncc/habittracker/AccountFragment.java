package com.uncc.habittracker;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
        binding.userEditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aListener.editAccount();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Account");
    }
    AccountListener aListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        aListener = (AccountFragment.AccountListener) context;
    }

    interface AccountListener {
        void editAccount();
    }

}

