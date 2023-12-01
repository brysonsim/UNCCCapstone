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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uncc.habittracker.data.model.Event;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.databinding.FragmentEditEventBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditEventFragment extends Fragment {

    Event event;
    FragmentEditEventBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public EditEventFragment() {
        // Required empty public constructor
    }

    public EditEventFragment(Event event){
        this.event = event;
    }

    public static EditEventFragment newInstance(String param1, String param2) {
        EditEventFragment fragment = new EditEventFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditEventBinding.inflate(inflater, container, false);

        // Checks if the current user is verified to either show the "Sponsored" checkbox or keep it hidden
        db.collection("users")
                .whereEqualTo("uid", auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);

                                if (user.getVerified().equals("1")) {
                                    binding.editSponsoredCheckBox.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Edit Event");

        binding.editTextEventTitle.setText(event.getTitle());
        binding.editTextTextDescription.setText(event.getDescription());
        binding.editTextLocation.setText(event.getLocation().toString());

        String habit = event.getHabitType();
        if (habit.equals("Study")){
            binding.typeSelector.check(R.id.rbStudy);
        }else if (habit.equals("Work Out")){
            binding.typeSelector.check(R.id.rbWorkOut);
        }else if (habit.equals("Mindfulness")){
            binding.typeSelector.check(R.id.rbLifestyle);
        }

        binding.editSponsoredCheckBox.setChecked(event.getSponsored());

        binding.buttonCancelEventEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancelEdit();
            }
        });

        binding.buttonEditevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("events").document(event.getDocId());

                HashMap<String, Object> data = new HashMap<>();


                RadioButton radioButton = (RadioButton) binding.typeSelector.findViewById(binding.typeSelector.getCheckedRadioButtonId());
                String habitType = radioButton.getText().toString();
                GeoPoint gp = new GeoPoint(0 , 0);
                data.put("location", gp);


                data.put("title", binding.editTextEventTitle.getText().toString().toUpperCase());
                data.put("description", binding.editTextTextDescription.getText().toString().toUpperCase());
                data.put("habitType", habitType);
                data.put("location", gp);
                data.put("sponsored", binding.editSponsoredCheckBox.isChecked());

                docRef.update(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Event Updated Successfully", Toast.LENGTH_SHORT).show();
                                        event.setTitle(binding.editTextEventTitle.getText().toString().toUpperCase());
                                        event.setDescription(binding.editTextTextDescription.getText().toString().toUpperCase());
                                        event.setHabitType(habitType);
                                        event.setLocation(gp);
                                        event.setSponsored(binding.editSponsoredCheckBox.isChecked());
                                        mListener.afterEditOpen(event);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Error : Server not respsonding", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
        });
    }

    EditFragmentListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (EditFragmentListener) context;
    }

    interface EditFragmentListener{
        void cancelEdit();
        void afterEditOpen(Event event);
    }
}