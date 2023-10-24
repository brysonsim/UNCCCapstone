package com.uncc.habittracker;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.Spanned;
import android.text.method.Touch;
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

        binding.autoCompleteCompareToTextView.setAdapter(autoCompleteAdapterArray);

        db.collection("users")
                .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Debug Get Id", document.getId());
                                setupDataListener(document.getId());
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
                        //createUserChip(mUser);
                    }
                }
            }
        });
    }

    private void createUserChip(User selectedUser) {
        ChipDrawable chip = ChipDrawable.createFromResource(this.getContext(), R.xml.standalone_chip);
        ImageSpan span = new ImageSpan(chip);
        int cursorPosition = binding.autoCompleteCompareToTextView.getSelectionStart();
        int spanLength = selectedUser.getDisplayName().length() + 2;
        Editable text = binding.autoCompleteCompareToTextView.getText();
        chip.setText(selectedUser.getDisplayName());
        chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
        Log.d("span", span.toString() + " " + String.valueOf(cursorPosition) + " " + String.valueOf(spanLength));
        text.setSpan(span, 0, cursorPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public void setupDataListener(String userId) {
        setupDataListener(userId, "");
    }

    public void setupDataListener(String userId, String secondaryUserId) {
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

                double habitProgress = (double)mUserHabit.getProgress();
                double habitProgressPercent = (habitProgress / 7.0) * 100.0;

                mBinding.progressBar.setProgress((int)habitProgressPercent);
            }
        }
    }
}