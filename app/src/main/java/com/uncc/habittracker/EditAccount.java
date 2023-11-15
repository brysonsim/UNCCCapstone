package com.uncc.habittracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uncc.habittracker.databinding.FragmentEditUserBinding;
import com.uncc.habittracker.databinding.FragmentUserAccountBinding;

public class EditAccount extends Fragment {

    FragmentEditUserBinding binding;

    public EditAccount() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditUserBinding.inflate(inflater, container, false);

        binding.editDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                eListener.account();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Account");
    }

    EditListener eListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        eListener = (EditAccount.EditListener) context;
    }

    interface EditListener {
        void account();
    }


}


