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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uncc.habittracker.databinding.FragmentDiscoveryBinding;
import com.uncc.habittracker.databinding.FragmentUpdatePasswordBinding;

public class UpdatePasswordFragment extends Fragment {

    public UpdatePasswordFragment()
    {
        // Required empty public constructor
    }

    FragmentUpdatePasswordBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        binding = FragmentUpdatePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Update Password");

        binding.buttonConfirmUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = binding.editTextTextPassword.getText().toString();
                if(newPassword.isEmpty() == true)
                {
                    Toast.makeText(getActivity(), "Enter a valid password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("TAG", "onComplete: password updated ");
                                        Toast.makeText(getActivity(), "Password Update Successful!", Toast.LENGTH_LONG).show();
                                        mListener.logoutUpdatePass();
                                    }
                                    else{
                                        Toast.makeText(getActivity(), "Password Could not be successfully completed. Please try logout and login again or try again later", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                }
            }
        });

    }
    UpdatePassword mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (UpdatePasswordFragment.UpdatePassword) context;
    }
    interface UpdatePassword {
        void logoutUpdatePass();
    }}