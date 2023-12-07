package com.uncc.habittracker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.uncc.habittracker.data.model.HabitProgress;
import com.uncc.habittracker.data.model.UserHabitDoc;
import com.uncc.habittracker.databinding.HabitRowItemBinding;
import com.uncc.habittracker.databinding.FragmentHabitsBinding;
import com.uncc.habittracker.utilities.DateUtils;

public class HabitsFragment extends Fragment {
    public HabitsFragment() {
        // Required empty public constructor
    }

    FragmentHabitsBinding binding;
    ArrayList<UserHabitDoc> mUserHabits = new ArrayList<>();
    ArrayList<UserHabitDoc> mUserHabitsToConfirm = new ArrayList<>();
    ArrayList<HabitProgress> mHabitProgress = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String loggedInUserId;
    HabitsAdapter adapter;
    ListenerRegistration listenerRegistration;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "HabitsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHabitsBinding.inflate(inflater, container, false);

        // Retrieve the logged in users Firebase UID and store for later use
        loggedInUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new HabitsAdapter();
        binding.recyclerView.setAdapter(adapter);

        getCurrentHabitProgress();

        binding.buttonCreateHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "creating new habit ");
                mListener.createNewHabit();
            }
        });

        binding.buttonConfirmProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmButtonClicked();
            }
        });
    }

    public void confirmButtonClicked() {
        // Store tasks, need this to track when all tasks have completed
        ArrayList<Task<Void>> tasks = new ArrayList<>();

        // For each user habit checked, create a new document in habitProgress collection
        for (UserHabitDoc userHabitDoc : mUserHabitsToConfirm) {
            DocumentReference docRef = db.collection("habitProgress").document();

            HashMap<String, Object> data = new HashMap<>();
            data.put("docId", docRef.getId());
            data.put("habitType", userHabitDoc.getHabitTypeID());
            data.put("source", "habit");
            data.put("userHabitDocId", userHabitDoc.getDocId());
            data.put("eventDocId", "");
            data.put("userId", this.loggedInUserId);
            data.put("progressDate", FieldValue.serverTimestamp());
            data.put("createdAt", FieldValue.serverTimestamp());
            data.put("frequency", userHabitDoc.getFrequency());

            // Add task that creates the new document in the habitProgress collection to tasks
            // ArrayList
            tasks.add(docRef.set(data));
        }

        // When all tasks have completed, reload the habits page. That way any habits that have
        // been marked complete and confirmed update in the UI and can no longer be interacted with.
        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                mListener.confirmHabit();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    public void getCurrentHabitProgress() {
        mHabitProgress.clear();

        db.collection("habitProgress")
                .whereEqualTo("userId", loggedInUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HabitProgress habitProgress = document.toObject(HabitProgress.class);
                            mHabitProgress.add(habitProgress);
                        }
                    }

                    if (task.isComplete()) {
                        setupListenerRegistration();
                    }
                });
    }

    public void setupListenerRegistration() {
        // Connect to the user habits store and get documents with the current users ID
        listenerRegistration = db.collection("usersHabits").whereEqualTo("userId", mAuth.getCurrentUser().getUid().toString()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    error.printStackTrace();
                    return;
                }

                mUserHabits.clear();

                //add the habits to an array list for storing based on the habit class scheme
                for (QueryDocumentSnapshot doc: value) {
                    UserHabitDoc userHabit = doc.toObject(UserHabitDoc.class);
                    userHabit.setDocId(doc.getId());

                    switch (userHabit.getFrequency()) {
                        case "Daily":
                            userHabit.setEligible(DateUtils.getDailyProgress(mHabitProgress, userHabit) <= 0);
                            break;
                        case "Weekly":
                            userHabit.setEligible(DateUtils.getWeeklyProgressForWeek(mHabitProgress, userHabit) <= 0);
                            break;
                        default:
                            userHabit.setEligible(DateUtils.getMonthlyProgress(mHabitProgress, userHabit) <= 0);
                            break;
                    }

                    mUserHabits.add(userHabit);
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitsViewHolder> {
        @NonNull
        @Override
        public HabitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            HabitRowItemBinding itemBinding = HabitRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new HabitsViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull HabitsViewHolder holder, int position) {
            UserHabitDoc habit = mUserHabits.get(position);
            holder.mBinding.deleteHabitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("usersHabits")
                            .document(habit.getDocId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    mListener.deleteHabit();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), "Error : Delete failed Server Error", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                }
            });

            holder.setupUI(habit);
        }

        @Override
        public int getItemCount() {
            return mUserHabits.size();
        }

        class HabitsViewHolder extends RecyclerView.ViewHolder {
            HabitRowItemBinding mBinding;
            UserHabitDoc mUserHabit;

            public HabitsViewHolder(HabitRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                mBinding = itemBinding;
            }

            //display data in the text holders

            public void setupUI(UserHabitDoc userHabit) {
                this.mUserHabit = userHabit;
                mBinding.textViewHabitName.setText(mUserHabit.getNameOverride());
                mBinding.textViewHabitType.setText(mUserHabit.getHabitTypeID());

                if (!mUserHabit.isEligible()) {
                    mBinding.cardViewHabit.setCardBackgroundColor(Color.GRAY);
                    mBinding.checkBoxHabitProgress.setVisibility(View.GONE);
                }
                else {
                    mBinding.checkBoxHabitProgress.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mBinding.checkBoxHabitProgress.isChecked()) {
                                mUserHabitsToConfirm.add(mUserHabit);
                            }
                            else {
                                mUserHabitsToConfirm.remove(mUserHabit);
                            }
                        }
                    });
                }
            }
        }
    }

    HabitsListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (HabitsListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Habits");
    }

    interface HabitsListener {
        void createNewHabit();
        void logout();
        void confirmHabit();
        void deleteHabit();
    }
}