package com.uncc.habittracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import com.uncc.habittracker.databinding.FragmentCreateHabitBinding;


public class CreateHabitFragment extends Fragment {
    private RadioGroup typeSelect,freqSelect;
    private String type,freq;
    public CreateHabitFragment() {
        // Required empty public constructor
    }

    FragmentCreateHabitBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateHabitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create Habit");
        typeSelect = getView().findViewById(R.id.typeSelector);
        freqSelect = getView().findViewById(R.id.freqChooser);

        typeSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(R.id.rbStudy == i){
                    type = "study";
                }
                else if(R.id.rbWorkOut == i){
                    type = "Work Out";
                }
                else if(R.id.rbMind == i){
                    type = "Mindfulness";
                }
            }
        });
        freqSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(R.id.rbDaily == i){
                    freq = "Daily";
                }
                else if(R.id.rbWeekly == i){
                    freq = "Weekly";
                }
                else if(R.id.rbMonthly == i){
                    freq = "Monthly";
                }
            }
        });

        //populate the drop down with the habit types
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        DocumentReference docRef = db.collection("habitTypes").document();



        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancelCreateHabit();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.editTextHabitName.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    DocumentReference docRef = db.collection("usersHabits").document();

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("frequency",freq);
                    data.put("habitTypeID", type);
                    data.put("nameOverride", name);
                    data.put("progress", 0);
                    data.put("userId", auth.getCurrentUser().getUid());



                    docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mListener.doneCreateHabit();
                            }
                            else {
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    CreateHabitListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreateHabitListener) context;
    }

    interface CreateHabitListener{
        void cancelCreateHabit();
        void doneCreateHabit();
    }
}