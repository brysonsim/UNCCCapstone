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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.databinding.FragmentSettingsBinding;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String TAG = "demo-test";
    User user = null;
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sListener.logout();
            }
        });


//        binding.buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sListener.updateEmail();
//            }
//        });

        binding.buttonUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sListener.updatePassword();

            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Settings");


        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser.getUid();

        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");

        Query query = usersCollection.whereEqualTo("uid", currentUid);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    user = document.toObject(User.class);
                    user.setFirebaseUid(document.getId());
                    binding.userName.setText(user.getFirstName() + " "+user.getLastName());

                    if (user.getVerified().equals("1")) {
                        binding.verifyIconImage.setVisibility(View.VISIBLE);
                    } else if (user.getVerified().equals("0")){
                        binding.verifyButton.setVisibility(View.VISIBLE);
                    } else if (user.getVerified().equals("pending")){
                        disableButton();
                    }

                    if (user.getAdmin()){
                        binding.approveVerificationButton.setVisibility(View.VISIBLE);
                        binding.approveVerificationButton.setEnabled(true);
                    }
                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Error : Server Not Responding !", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.approveVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sListener.openAdminApproval();
            }
        });

        binding.verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> verificationData = new HashMap<>();
                verificationData.put("userId", user.getUid());
                verificationData.put("firstName", user.getFirstName());
                verificationData.put("lastName", user.getLastName());
                verificationData.put("timestamp", FieldValue.serverTimestamp());
                verificationData.put("docId",user.getFirebaseUid());

                db.collection("approve_verification")
                        .add(verificationData)
                        .addOnSuccessListener(documentReference -> {
                            updateUserDataAfterVerification(user.getFirebaseUid());
                        })
                        .addOnFailureListener(e -> {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Error : Failed to Sent Request !", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
            }
        });


    }

    private void updateUserDataAfterVerification(String docId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("verified", "pending");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(docId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    disableButton();
                })
                .addOnFailureListener(e -> {
                    sListener.errorState();
                });
    }

    private void disableButton(){
        binding.verifyButton.setVisibility(View.VISIBLE);
        binding.verifyButton.setText("REQUEST SENT");
        binding.verifyButton.setEnabled(false);
    }

    SettingsListener sListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sListener = (SettingsFragment.SettingsListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Settings");
    }

    interface SettingsListener {
        void logout();
        void errorState();
        void openAdminApproval();

        void updateEmail();
        void updatePassword();
    }
}

