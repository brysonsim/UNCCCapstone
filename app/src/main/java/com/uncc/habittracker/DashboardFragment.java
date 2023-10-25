package com.uncc.habittracker;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.data.model.UserHabit;
import com.uncc.habittracker.databinding.FragmentDashboardBinding;
import com.uncc.habittracker.databinding.DashboardRowItemBinding;

import java.util.ArrayList;
import java.util.Optional;

public class DashboardFragment extends Fragment {
    FragmentDashboardBinding binding;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ArrayList<UserHabit> mUserHabits = new ArrayList<>();
    ArrayList<User> mUsers = new ArrayList<>();

    DashboardFragment.DashboardAdapter adapter;

    ArrayAdapter<String> autoCompleteAdapterArray;

    ListenerRegistration listenerRegistration;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String loggedInUserId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Dashboard");

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DashboardFragment.DashboardAdapter();
        binding.recyclerView.setAdapter(adapter);

        autoCompleteAdapterArray = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1);

        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            Log.d("Debug auto complete", user.getDisplayName());
                            user.setFirebaseUid(doc.getId());
                            mUsers.add(user);
                            autoCompleteAdapterArray.add(user.getDisplayName());
                        }
                    }
                }
            }
        });

        binding.autoCompleteCompareToTextView.setInputType(InputType.TYPE_NULL);
        binding.autoCompleteCompareToTextView.setAdapter(autoCompleteAdapterArray);

        binding.autoCompleteCompareToTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("TextChangedListener", "beforeTextChanged");
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TextChangedListener", "onTextChanged");
            }
            @Override
            public void afterTextChanged(Editable s) {
                String currentValue = binding.autoCompleteCompareToTextView.getText().toString();
                Log.d("TextChangedListener", "afterTextChanged: '" + currentValue + "'");

                if (currentValue.equals("")) {
                    setupDataListener(loggedInUserId);
                }
            }
        });

        db.collection("users")
                .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Debug Get Id", document.getId());
                                loggedInUserId = document.getId();
                                setupDataListener(loggedInUserId);
                            }
                        }
                    }
                });

        binding.autoCompleteCompareToTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String valueOfSelection = String.valueOf(binding.autoCompleteCompareToTextView.getText());
                Log.d("Chosen selection", valueOfSelection);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Optional<User> selectedUser = mUsers.stream()
                            .filter(user -> user.getDisplayName().equals(valueOfSelection))
                            .findFirst();

                    if (selectedUser.isPresent()) {
                        User mUser = selectedUser.get();
                        Log.d("Chosen selection (val)", mUser.getFirebaseUid());
                        createUserChip(mUser);
                        setupDataListener(loggedInUserId, mUser.getUid());
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyListenerRegistration();
    }

    private void destroyListenerRegistration() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void createUserChip(User selectedUser) {
        ChipDrawable chip = ChipDrawable.createFromResource(this.getContext(), R.xml.standalone_chip);
        ImageSpan span = new ImageSpan(chip);
        int cursorPosition = binding.autoCompleteCompareToTextView.getSelectionStart();
        Editable text = binding.autoCompleteCompareToTextView.getText();
        chip.setText(selectedUser.getDisplayName());
        chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
        text.setSpan(span, 0, cursorPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public void setupDataListener(String userId) {
        setupDataListener(userId, "");
    }

    public void setupDataListener(String userId, String secondaryUserId) {
        destroyListenerRegistration();
        ArrayList<UserHabit> mCompareToUserHabits = new ArrayList<>();

        Log.d("Secondary UserID", secondaryUserId);

        if (!secondaryUserId.isEmpty()) {
            db.collection("usersHabits").whereEqualTo("userId", secondaryUserId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserHabit userHabit = document.toObject(UserHabit.class);
                            mCompareToUserHabits.add(userHabit);
                        }

                        Log.d("UserHabits compare", String.valueOf(mCompareToUserHabits.size()));

                        initializeDataListener(userId, mCompareToUserHabits);
                    }
                }
            });
        }
        else {
            initializeDataListener(userId, mCompareToUserHabits);
        }
    }

    public void initializeDataListener(String userId, ArrayList<UserHabit> compareToUserHabits) {
        listenerRegistration = db.collection("usersHabits").whereEqualTo("userId", userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    error.printStackTrace();
                    return;
                }

                mUserHabits.clear();

                for (QueryDocumentSnapshot doc : value) {
                    UserHabit userHabit = doc.toObject(UserHabit.class);

                    if (compareToUserHabits.size() > 0) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Log.d("UserHabits compare", userHabit.getHabitTypeID());

                            Optional<UserHabit> selectedUserHabit = compareToUserHabits.stream()
                                    .filter(compareToUserHabit -> {
                                        Log.d("Inside filter block", compareToUserHabit.getHabitTypeID());

                                        return compareToUserHabit.getHabitTypeID().equals(userHabit.getHabitTypeID());
                                    })
                                    .findFirst();

                            if (selectedUserHabit.isPresent()) {
                                UserHabit mCompareToUserHabit = selectedUserHabit.get();
                                userHabit.setProgressSecondary(mCompareToUserHabit.getProgress());
                            }
                        }
                    }

                    mUserHabits.add(userHabit);
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    class DashboardAdapter extends RecyclerView.Adapter<DashboardFragment.DashboardAdapter.DashboardViewHolder> {
        @NonNull
        @Override
        public DashboardFragment.DashboardAdapter.DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DashboardRowItemBinding itemBinding = DashboardRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new DashboardFragment.DashboardAdapter.DashboardViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull DashboardFragment.DashboardAdapter.DashboardViewHolder holder, int position) {
            UserHabit userHabit = mUserHabits.get(position);
            holder.setupUI(userHabit);
        }

        @Override
        public int getItemCount() {
            return mUserHabits.size();
        }

        class DashboardViewHolder extends RecyclerView.ViewHolder {
            DashboardRowItemBinding mBinding;
            UserHabit mUserHabit;

            public DashboardViewHolder(DashboardRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                mBinding = itemBinding;
            }

            public void setupUI(UserHabit userHabit) {
                this.mUserHabit = userHabit;
                mBinding.textViewHabitName.setText(mUserHabit.getNameOverride());

                Log.d("Debug progress", String.valueOf(mUserHabit.getProgress()));

                // Progress 1
                double habitProgress = (double)mUserHabit.getProgress();
                double habitProgressPercent = (habitProgress / 7.0) * 100.0;
                Log.d("Progress Primary", mUserHabit.getHabitTypeID() + ": " + String.valueOf(habitProgress));

                // Progress 2
                double habitProgressSecondary = (double)mUserHabit.getProgressSecondary();
                Log.d("Progress Secondary", mUserHabit.getHabitTypeID() + ": " + String.valueOf(habitProgressSecondary));

                double habitProgressSecondaryPercent = (habitProgressSecondary / 7.0) * 100.0;
                //double habitProgressSecondaryPercent = 50.0;

                int colorLoggedInUser = Color.parseColor("#03DAC6");
                int colorComparedUser = Color.parseColor("#6200EE");

                // Generate a mutated version of the progress bar drawable. If we do not do this,
                // changing these properties will affect each of the progress bars, not the
                // individual ones.
                LayerDrawable progressDrawable = (LayerDrawable)mBinding.progressBar.getProgressDrawable().mutate().getConstantState().newDrawable();

                if (habitProgressSecondary > 0 && habitProgressSecondaryPercent < habitProgressPercent) {
                    progressDrawable.setId(0, android.R.id.background);

                    // We need to swap the position of the primary and secondary progress bar layers
                    // Default is background -> secondaryProgress -> progress
                    // Doing this ensures the secondary progress bar gets layered on top of the
                    // primary.
                    progressDrawable.setId(1, android.R.id.progress);
                    progressDrawable.setId(2, android.R.id.secondaryProgress);

                    // We also need to force the colors we want to use. Also need to force the tint
                    // mode to SRC_OVER preventing the applying of any alpha channels.
                    progressDrawable.findDrawableByLayerId(progressDrawable.getId(2)).setColorFilter(colorLoggedInUser, PorterDuff.Mode.SRC_OVER);
                    progressDrawable.findDrawableByLayerId(progressDrawable.getId(1)).setColorFilter(colorComparedUser, PorterDuff.Mode.SRC_OVER);
                }
                else {
                    progressDrawable.findDrawableByLayerId(progressDrawable.getId(2)).setColorFilter(colorComparedUser, PorterDuff.Mode.SRC_OVER);
                    progressDrawable.findDrawableByLayerId(progressDrawable.getId(1)).setColorFilter(colorLoggedInUser, PorterDuff.Mode.SRC_OVER);
                }

                // Apply our modified layer drawable to the progress bar
                mBinding.progressBar.setProgressDrawable(progressDrawable);
                mBinding.progressBar.setProgress((int)habitProgressPercent);
                mBinding.textViewPrimaryPercent.setText(String.format("%d%%", (int)habitProgressPercent));

                if (habitProgressSecondary > 0) {
                    mBinding.progressBar.setSecondaryProgress((int)habitProgressSecondaryPercent);
                    mBinding.textViewSecondaryPercent.setText(String.format("%d%%", (int)habitProgressSecondaryPercent));
                }
                else {
                    mBinding.progressBar.setSecondaryProgress(0);
                    mBinding.textViewSecondaryPercent.setText("");
                }
            }
        }
    }
}