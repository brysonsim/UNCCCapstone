package com.uncc.habittracker;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uncc.habittracker.databinding.FragmentHabitsBinding;
import com.uncc.habittracker.databinding.FragmentSettingsBinding;
import com.uncc.habittracker.databinding.FragmentUserAccountBinding;

public class AccountFragment extends Fragment {
    FragmentUserAccountBinding binding;
    AccountFragment.AccountListener mListener;
    //AccountFragment.AccountAdapter adapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserAccountBinding.inflate(inflater, container, false);


        db.collection("users")
                .whereEqualTo("uid", auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Debug Get Id", document.getId());
                                TextView userName = getView().findViewById(R.id.userNametxt);
                                TextView aboutSection = getView().findViewById(R.id.aboutTxt);
                                userName.setText(document.getString("firstName") + " " + document.getString("lastName"));


                                if(document.getString("about") == "" || document.getString("about") == null){
                                    aboutSection.setText("Head to edit to fill out\nyour Bio!");
                                }else{
                                    aboutSection.setText(document.getString("about"));
                                }

                            }
                        }
                    }
                });


        binding.userEditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aListener.editAccount();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    AccountListener aListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        aListener = (AccountFragment.AccountListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Account");
    }

    interface AccountListener{
        void editAccount();
    }

}


