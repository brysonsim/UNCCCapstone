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
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.uncc.habittracker.databinding.FragmentCreateEventsBinding;
import com.uncc.habittracker.databinding.FragmentCreateHabitBinding;

import java.util.HashMap;

public class CreateEventsFragment extends Fragment {
 private int test;

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
                else if(binding.typeSelector.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getActivity(), "Please select a habit", Toast.LENGTH_SHORT).show();
                }
                else{
                    String TAG = "demo";


                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    DocumentReference docRef = db.collection("events").document();
                    HashMap<String, Object> data = new HashMap<>();


                    RadioButton radioButton = (RadioButton) binding.typeSelector.findViewById(binding.typeSelector.getCheckedRadioButtonId());
                    String habitType = radioButton.getText().toString();
                    GeoPoint gp = new GeoPoint(0 , 0);
                    data.put("location", gp);


                    data.put("title", title);
                    data.put("description", desc);
                    data.put("ownerId", auth.getCurrentUser().getUid());
                    data.put("ownerName", auth.getCurrentUser().getDisplayName());
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("time", FieldValue.serverTimestamp());
                    data.put("docId", docRef.getId());
                    data.put("habitType", habitType);
                    data.put("location", gp);

                    Log.d(TAG, auth.getCurrentUser().getDisplayName());


                    docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "passed putting data");
                                mListener.submitEventCreation();
                            }
                            else{
                                Log.d(TAG, "failed putting data");
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

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Create Event");
    }

    interface CreateEventListener
    {
        void cancelEventCreation();
        void submitEventCreation();
    }



}