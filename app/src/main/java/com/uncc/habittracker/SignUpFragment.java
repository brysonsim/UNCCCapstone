package com.uncc.habittracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uncc.habittracker.databinding.FragmentSignUpBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SignUpFragment extends Fragment {
    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
//create a new fireauth session
    FragmentSignUpBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.login();
            }
        });
        binding.buttonSignup.setOnClickListener(new View.OnClickListener() {
            //if the user is signing up grab their data
            @Override
            public void onClick(View v) {
                String firstName = binding.editTextFirstName.getText().toString();
                String lastName = binding.editTextLastName.getText().toString();
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                //stop the user if the data is empty, or if the email is not a uncc or charlotte email
                if (firstName.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter valid first name!", Toast.LENGTH_SHORT).show();
                }
                if (lastName.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter valid last name!", Toast.LENGTH_SHORT).show();
                }
                else if(email.isEmpty() || !(email.matches("[A-Za-z0-9]+@uncc\\.edu") || email.matches("[A-Za-z0-9]+@charlotte\\.edu"))) {
                    Toast.makeText(getActivity(), "Enter valid email!", Toast.LENGTH_SHORT).show();
                }
                else if (password.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter valid password!", Toast.LENGTH_SHORT).show();
                }
                else {
                    //create the user on the authentication side
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //update FirebaseUser profile firebase
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(firstName + " " + lastName)
                                        .build();
    //push the users profile to the user collection
                                mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            DocumentReference docRef = db.collection("users").document();

                                            HashMap<String, Object> data = new HashMap<>();
                                            data.put("firstName", firstName.toUpperCase());
                                            data.put("lastName", lastName.toUpperCase());
                                            data.put("uid", mAuth.getCurrentUser().getUid());
                                            data.put("about", "");
                                            data.put("verified","0");
                                            data.put("usersHabits", new ArrayList<String>());
                                            data.put("verified","0");
                                            data.put("admin",false);

                                            docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mListener.authSuccessful();
                                                    }
                                                    //notifications for the users if  there was an error
                                                    else {
                                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else {
                                Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    SignUpListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (SignUpListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.create_account_label);
    }

    interface SignUpListener {
        void login();
        void authSuccessful();
    }
}