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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uncc.habittracker.databinding.FragmentCreateEventsBinding;
import com.uncc.habittracker.databinding.FragmentCreateHabitBinding;

import java.util.HashMap;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create Event");

        binding.buttonCancelEventCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mListener.cancelEventCreation();
            }
        });

        binding.buttonCreateEventFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String title = binding.editTextEventTitle.getText().toString();
                String desc = binding.editTextTextMultiLineDescription.getText().toString();
                String location = binding.editTextTextPostalAddress.getText().toString();
                if(title.isEmpty()){
                    Toast.makeText(getActivity(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                } else if(desc.isEmpty()){
                    Toast.makeText(getActivity(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else if(location.isEmpty()){
                    Toast.makeText(getActivity(), "location cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    DocumentReference docRef = db.collection("events").document();
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("title", title);
                    data.put("description", desc);
                    data.put("ownerId", auth.getCurrentUser().getUid());
                    data.put("ownerName", auth.getCurrentUser().getDisplayName());
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("docId", docRef.getId());

                    docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mListener.submitEventCreation();

                            }
                            else{
                            }
                        }
                    });

                }

            }
        });



    }

    CreateEventListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreateEventListener) context;
    }

    interface CreateEventListener
    {
        void cancelEventCreation();
        void submitEventCreation();
    }



}