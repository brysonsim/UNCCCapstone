package com.uncc.habittracker;

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
import com.uncc.habittracker.databinding.FragmentUpdateEmailBinding;


public class UpdateEmailFragment extends Fragment {



    public UpdateEmailFragment() {
        // Required empty public constructor
    }

    FragmentUpdateEmailBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        binding = FragmentUpdateEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Update Email");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        binding.buttonConfirmUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.updateEmail("user@uncc.edu").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Log.d("TAG", "onComplete: email changed ");
                        }
                        else{
                            Log.d("TAG", "onComplete: email not changed ");
                        }
                    }
                });



//                String newEmail = binding.editTextNewEmail.getText().toString();
//                if(newEmail.isEmpty() == true)
//                {
//                    Toast.makeText(getActivity(), "Enter a valid email", Toast.LENGTH_SHORT).show();
//                }
//                else
//                {
//                    Log.d("TAG", "email is valid: "+ newEmail);
//
//
//                    user.updateEmail("thenewEmail@gmail.com").addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task)
//                        {
//                            if(task.isSuccessful()){
//                                Log.d("TAG", newEmail);
//                                Toast.makeText(getActivity(), "success", Toast.LENGTH_SHORT).show();
//                            }
//                            else{
//                                Log.d("TAG", "fail " + newEmail);
//                            }
//                        }
//                    });
//                }
            }
        });

    }
}