package com.uncc.habittracker;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import com.uncc.habittracker.data.model.Habit;
import com.uncc.habittracker.databinding.HabitRowItemBinding;
import com.uncc.habittracker.databinding.FragmentHabitsBinding;

public class HabitsFragment extends Fragment {

    public HabitsFragment() {
        // Required empty public constructor
    }

    FragmentHabitsBinding binding;
    ArrayList<Habit> mHabits = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHabitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    HabitsAdapter adapter;
    ListenerRegistration listenerRegistration;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new HabitsAdapter();
        binding.recyclerView.setAdapter(adapter);
        //connect to the user habits store and get documents with the current users ID
        listenerRegistration = db.collection("usersHabits").whereEqualTo("userId", mAuth.getCurrentUser().getUid().toString()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    error.printStackTrace();
                    return;
                }

                mHabits.clear();
                //add the habits to an array list for storing based on the habit class scheme
                for (QueryDocumentSnapshot doc: value) {
                    Habit habit = doc.toObject(Habit.class);
                    habit.setDocId(doc.getId());
                    mHabits.add(habit);
                }

                adapter.notifyDataSetChanged();
            }
        });
        binding.buttonCreateHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("TAG", "creating new habit ");
                mListener.createNewHabit();
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

    class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitsViewHolder> {
        @NonNull
        @Override
        public HabitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            HabitRowItemBinding itemBinding = HabitRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new HabitsViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull HabitsViewHolder holder, int position) {
            Habit habit = mHabits.get(position);
            holder.mBinding.deleteHabitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("usersHabits")
                            .document(habit.getDocId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

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
            return mHabits.size();
        }

        class HabitsViewHolder extends RecyclerView.ViewHolder {
            HabitRowItemBinding mBinding;
            Habit mHabit;

            public HabitsViewHolder(HabitRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                mBinding = itemBinding;
            }

            //display data in the text holders
            public void setupUI(Habit habit) {

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                this.mHabit = habit;
                mBinding.textViewHabitName.setText(mHabit.getNameOverride());
                mBinding.textViewHabitType.setText(mHabit.getHabitTypeID());

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
    }
}