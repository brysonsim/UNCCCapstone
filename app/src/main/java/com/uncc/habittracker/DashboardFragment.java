package com.uncc.habittracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.ArrayAdapter;

import com.google.android.material.chip.ChipDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uncc.habittracker.data.model.Follower;
import com.uncc.habittracker.data.model.HabitProgress;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.data.model.UserHabitDoc;
import com.uncc.habittracker.databinding.FragmentDashboardBinding;
import com.uncc.habittracker.databinding.DashboardRowItemBinding;
import com.uncc.habittracker.utilities.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {
    // This variable will hold the binding to our fragment UI. Using this allows us to access
    // components without having to use findViewByID first.
    FragmentDashboardBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<UserHabitDoc> mUserHabits = new ArrayList<>();
    ArrayList<HabitProgress> mHabitProgressPrimary = new ArrayList<>();
    ArrayList<User> mUsers = new ArrayList<>();
    DashboardFragment.DashboardAdapter adapter;
    ArrayAdapter<String> autoCompleteAdapterArray;
    ListenerRegistration listenerRegistration;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String loggedInUserId;

    String TAG = "Dashboard";

    public DashboardFragment() {
        // Required empty public constructor
    }

    // Called when the fragment is instantiating its UI view.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize binding to layout where this fragment is assigned as the context.
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        // Retrieve the logged in users Firebase UID and store for later use
        loggedInUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        return binding.getRoot();
    }

    // Called immediately after onCreateView but before any saved state has been restored to the
    // view
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DashboardFragment.DashboardAdapter();
        binding.recyclerView.setAdapter(adapter);

        pullHabitProgressForLoggedInUser();

        // Setup our ArrayAdapter be to used by the AutoCompleteTextView. This adapter controls the
        // UI of the elements in its dropdown.
        autoCompleteAdapterArray = new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_list_item_1);

        // Query Firebase for all users and prepare content for our AutoCompleteTextView
        // TODO: When following logic implemented switch this to only pull users the logged in user
        //   has followed
        db.collection("followers").whereEqualTo("userID", loggedInUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    ArrayList<String> followedUsers = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Follower follower = doc.toObject(Follower.class);
                        followedUsers.add(follower.getFollowingID());
                    }

                    if (followedUsers.size() > 0)
                    {
                        db.collection("users").whereIn("uid", followedUsers).get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                if (task2.getResult() != null) {
                                    for (QueryDocumentSnapshot doc : task2.getResult()) {
                                        User user = doc.toObject(User.class);
                                        Log.d("Debug auto complete", user.getDisplayName());
                                        user.setFirebaseUid(doc.getId());
                                        mUsers.add(user);
                                        autoCompleteAdapterArray.add(user.getDisplayName());
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

        binding.autoCompleteCompareToTextView.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set adapter of our AutoCompleteTextView to the AdapterArray we configured.
        binding.autoCompleteCompareToTextView.setAdapter(autoCompleteAdapterArray);

        // This listener handles reacting to the clearing of the AutoCompleteTextView.
        binding.autoCompleteCompareToTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String currentValue = binding.autoCompleteCompareToTextView.getText().toString();

                // If this component is cleared, we need to reset the secondary progress that got
                // set when selecting a user to compare against.
                if (currentValue.equals("")) {
                    setupDataListener(loggedInUserId);
                }
            }
        });

        // Setup the on item click listener for the AutoCompleteTextView. On selection, find the
        // matching user, and pull their habit data for comparison.
        binding.autoCompleteCompareToTextView.setOnItemClickListener((adapterView, view1, position, id) -> {
            String valueOfSelection = String.valueOf(binding.autoCompleteCompareToTextView.getText());
            Log.d("Chosen selection", valueOfSelection);

            // Locate the user matching the selected value.
            Optional<User> selectedUser = mUsers.stream()
                    .filter(user -> user.getDisplayName().equals(valueOfSelection))
                    .findFirst();

            // If found, convert the text entered to a Chip UI element and pull
            if (selectedUser.isPresent()) {
                User mUser = selectedUser.get();
                Log.d("Chosen selection (val)", mUser.getFirebaseUid());
                createUserChip(mUser);
                setupDataListener(loggedInUserId, mUser.getUid());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyListenerRegistration();
    }

    @Override
    public void onResume() {
        super.onResume();

        Optional.ofNullable((MainActivity)getActivity())
                .map(AppCompatActivity::getSupportActionBar)
                .ifPresent(actionBar -> actionBar.setTitle("Dashboard"));
    }

    private void destroyListenerRegistration() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    // The method converts the plain text keyed into the AutoCompleteTextView into a Chip UI
    // element.
    private void createUserChip(User selectedUser) {
        ChipDrawable chip = ChipDrawable.createFromResource(
                this.requireContext(), R.xml.standalone_chip);
        ImageSpan span = new ImageSpan(chip);
        int cursorPosition = binding.autoCompleteCompareToTextView.getSelectionStart();
        Editable text = binding.autoCompleteCompareToTextView.getText();
        chip.setText(selectedUser.getDisplayName());
        chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
        text.setSpan(span, 0, cursorPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    // This function handles preparing the data used by the progress bars when there is no user
    // being compared again.
    public void setupDataListener(String userId) {
        setupDataListener(userId, "");
    }

    // This function handles preparing the data used by the progress bars when there is a user to
    // compare against. It can also be passed a blank value for the secondary user to disable this.
    public void setupDataListener(String userId, String secondaryUserId) {
        destroyListenerRegistration();
        ArrayList<HabitProgress> mCompareToUserHabits = new ArrayList<>();

        Log.d("Secondary UserID", secondaryUserId);

        // If a secondary user was passed, pulls their habit data in preparation for setting up the
        // RecyclerView data listener.
        if (!secondaryUserId.isEmpty()) {
            db.collection("habitProgress")
                    .whereEqualTo("userId", secondaryUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HabitProgress habitProgress = document.toObject(HabitProgress.class);
                                mCompareToUserHabits.add(habitProgress);
                            }

                            Log.d("UserHabits compare", String.valueOf(mCompareToUserHabits.size()));

                            initializeDataListener(userId, mCompareToUserHabits);
                        }
                    });
        }
        // Otherwise, rather than pulling a list of habit use the empty array we declared earlier in
        // preparation for setting up the RecyclerView data listener.
        else {
            initializeDataListener(userId, mCompareToUserHabits);
        }
    }

    // This sets up the data listener for the RecyclerView which drives the progress bars.
    public void initializeDataListener(String userId, ArrayList<HabitProgress> compareToUserHabits) {
        // Query the logged in users habits, compare to a secondary user (if applicable), and notify
        // the RecyclerView adapter that new data is available.
        listenerRegistration = db.collection("usersHabits")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    mUserHabits.clear();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            // Convert query row result to an instance of our UserHabit data model.
                            UserHabitDoc userHabit = doc.toObject(UserHabitDoc.class);
                            userHabit.setDocId(doc.getId());

                            // If there are user habits to compare against do the comparison here.
                            if (compareToUserHabits.size() > 0) {
                                Log.d("UserHabits compare", userHabit.getHabitTypeID());

                                // When matching on another users habits make sure the habitTypeID and
                                // frequency match.
                                List<HabitProgress> selectedUserHabit = compareToUserHabits.stream()
                                        .filter(compareToHabit -> compareToHabit.getHabitType().equals(userHabit.getHabitTypeID()))
                                        .filter(compareToHabit -> compareToHabit.getFrequency().equals(userHabit.getFrequency()))
                                        .collect(Collectors.toList());

                                // If we get a match, set the progressSecondary attribute on the
                                // UserHabit currently being processed.
                                if (selectedUserHabit.size() > 0) {
                                    ArrayList<HabitProgress> mHabitProgressSecondary = new ArrayList<>(selectedUserHabit);

                                    UserHabitDoc selectedUH = new UserHabitDoc();
                                    selectedUH.setDocId(mHabitProgressSecondary.stream().findFirst().get().getUserHabitDocId());

                                    switch (userHabit.getFrequency()) {
                                        case "Daily":
                                            userHabit.setProgressSecondary(DateUtils.getDailyProgressForWeek(mHabitProgressSecondary, selectedUH));
                                            break;
                                        case "Weekly":
                                            userHabit.setProgressSecondary(DateUtils.getWeeklyProgressForWeek(mHabitProgressSecondary, selectedUH));
                                            break;
                                        default:
                                            userHabit.setProgressSecondary(DateUtils.getMonthlyProgress(mHabitProgressSecondary, selectedUH));
                                            break;
                                    }
                                }
                            }

                            mUserHabits.add(userHabit);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    public void pullHabitProgressForLoggedInUser() {
        mHabitProgressPrimary.clear();

        db.collection("habitProgress")
                .whereEqualTo("userId", loggedInUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HabitProgress habitProgress = document.toObject(HabitProgress.class);
                            mHabitProgressPrimary.add(habitProgress);
                        }
                    }

                    if (task.isComplete()) {
                        setupDataListener(loggedInUserId);
                    }
                });
    }

    // Setup adapter for our RecyclerView. These provide a binding from an app-specific dataset to
    // views that are displayed within a RecyclerView.
    class DashboardAdapter extends RecyclerView.Adapter<DashboardFragment.DashboardAdapter.DashboardViewHolder> {
        @NonNull
        @Override
        public DashboardFragment.DashboardAdapter.DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Initialize binding to layout, same as before this allows us to access elements within
            // the RecyclerView without having to use findViewByID.
            DashboardRowItemBinding itemBinding = DashboardRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new DashboardFragment.DashboardAdapter.DashboardViewHolder(itemBinding);
        }

        // This is called by the RecyclerView to display the data at a given position.
        @Override
        public void onBindViewHolder(@NonNull DashboardFragment.DashboardAdapter.DashboardViewHolder holder, int position) {
            UserHabitDoc userHabit = mUserHabits.get(position);
            holder.setupUI(userHabit);
        }

        // Implementing this function allows the RecyclerView to know how many row items it will
        // contain.
        @Override
        public int getItemCount() {
            return mUserHabits.size();
        }

        // This class describes the item view and metadata about its place within the RecyclerView.
        class DashboardViewHolder extends RecyclerView.ViewHolder {
            DashboardRowItemBinding mBinding;
            UserHabitDoc mUserHabit;

            // In this constructor we are taking the passed item binding and assigning it to an
            // internal attribute so we can use the binding within this class.
            public DashboardViewHolder(DashboardRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                mBinding = itemBinding;
            }

            // This method sets up the UI for each row of the RecyclerView.
            public void setupUI(UserHabitDoc userHabit) {
                this.mUserHabit = userHabit;
                mBinding.textViewHabitName.setText(mUserHabit.getNameOverride());

                switch (mUserHabit.getFrequency()) {
                    case "Daily":
                        mUserHabit.setProgress(DateUtils.getDailyProgressForWeek(mHabitProgressPrimary, mUserHabit));
                        break;
                    case "Weekly":
                        mUserHabit.setProgress(DateUtils.getWeeklyProgressForWeek(mHabitProgressPrimary, mUserHabit));
                        break;
                    default:
                        mUserHabit.setProgress(DateUtils.getMonthlyProgress(mHabitProgressPrimary, mUserHabit));
                        break;
                }

                Log.d("Debug progress", String.valueOf(mUserHabit.getProgress()));

                // Retrieve habit progress
                double habitProgress = mUserHabit.getProgress();
                double habitProgressPercent;

                // Retrieve secondary habit progress (if applicable, this applies if a user is being
                // compared to)
                double habitProgressSecondary = mUserHabit.getProgressSecondary();
                double habitProgressSecondaryPercent;

                Log.d("Progress Primary", mUserHabit.getHabitTypeID() + ": " + habitProgress);
                Log.d("Progress Secondary", mUserHabit.getHabitTypeID() + ": " + habitProgressSecondary);

                // Calculate percentage completion based on the habit interval. We are showing
                // progress at the week level so habits at the weekly and monthly level are
                // calculated differently.
                if (mUserHabit.getFrequency().equals("Daily")) {
                    habitProgressPercent = (habitProgress / 7.0) * 100.0;
                    habitProgressSecondaryPercent = (habitProgressSecondary / 7.0) * 100.0;
                }
                else {
                    habitProgressPercent = habitProgress * 100.0;
                    habitProgressSecondaryPercent = habitProgressSecondary * 100.0;
                }

                // If habit progress percent is greater than 100%, update it.
                if (habitProgressPercent > 100.0) {
                    habitProgressPercent = 100.0;
                }

                // If secondary habit progress percent is greater than 100%, update it.
                if (habitProgressSecondaryPercent > 100.0) {
                    habitProgressSecondaryPercent = 100.0;
                }

                // If secondary habit progress is set and is less than the logged in users progress
                // we need to use the secondary progress bar so the UI can show the correct coloring
                // of each segment of the progress bar. Make sure to hide the main progress bar as a
                // precaution.
                if (habitProgressSecondary > 0 && habitProgressSecondaryPercent < habitProgressPercent) {
                    mBinding.progressBar.setVisibility(View.INVISIBLE);
                    mBinding.progressBarSecondary.setVisibility(View.VISIBLE);
                    mBinding.progressBarSecondary.setProgress((int)habitProgressSecondaryPercent);
                    mBinding.progressBarSecondary.setSecondaryProgress((int)habitProgressPercent);
                }
                // Otherwise, we either have no secondary habit progress set or we are not comparing
                // our habit progress to another user. Make sure to use the main progress bar and
                // hide the secondary progress bar as a precaution.
                else {
                    mBinding.progressBarSecondary.setVisibility(View.INVISIBLE);
                    mBinding.progressBar.setVisibility(View.VISIBLE);
                    mBinding.progressBar.setProgress((int)habitProgressPercent);
                    mBinding.progressBar.setSecondaryProgress((int)habitProgressSecondaryPercent);
                }

                // Populate text views showing the actual progress percentages.
                mBinding.textViewPrimaryPercent.setText(String.format("%d%%", (int)habitProgressPercent));

                if (habitProgressSecondary > 0) {
                    mBinding.textViewSecondaryPercent.setText(String.format("%d%%", (int)habitProgressSecondaryPercent));
                }
                else {
                    mBinding.textViewSecondaryPercent.setText("");
                }
            }
        }
    }
}