package com.uncc.habittracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import com.uncc.habittracker.databinding.FragmentEditUserBinding;
import com.uncc.habittracker.databinding.FragmentUserAccountBinding;

import java.util.HashMap;

public class EditAccount extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FragmentEditUserBinding binding;

    String userProfile = "";

    public EditAccount() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditUserBinding.inflate(inflater, container, false);

        db.collection("users")
                .whereEqualTo("uid", auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userProfile = document.getId();
                                EditText firstName = getView().findViewById(R.id.editFirstName);
                                EditText lastName = getView().findViewById(R.id.editLastName);
                                firstName.setText(document.getString("firstName"));
                                lastName.setText(document.getString("lastName"));

                                EditText aboutSection = getView().findViewById(R.id.editAbout);

                                if(document.getString("about") != "" || document.getString("about") != null) {
                                    aboutSection.setText(document.getString("about"));
                                }

                            }
                        }
                    }
                });
        binding.editDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               DocumentReference docref = db.collection("users").document(userProfile);

                HashMap<String, Object> data = new HashMap<>();
                data.put("firstName", binding.editFirstName.getText().toString());
                data.put("lastName",binding.editLastName.getText().toString());
                data.put("about", binding.editAbout.getText().toString());
                docref.update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            eListener.account();
                        }
                        else {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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


