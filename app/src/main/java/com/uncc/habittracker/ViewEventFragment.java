package com.uncc.habittracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uncc.habittracker.data.model.Event;
import com.uncc.habittracker.data.model.HabitProgress;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.data.model.UserHabitDoc;
import com.uncc.habittracker.databinding.FragmentViewEventBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class ViewEventFragment extends Fragment implements OnMapReadyCallback {
    String TAG = "ViewEventFragment";
    FragmentViewEventBinding binding;
    Event event;
    MapView mapView;
    GoogleMap map;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    ViewEventListener mListener;
    ArrayList<UserHabitDoc> mUserHabits = new ArrayList<>();
    Dialog registerDialog;
    UserHabitDoc trackedUserHabit;
    String userId;
    HabitProgress eventRegistration;
    private HabitSpinnerAdapter mHabitSpinnerAdapter;

    public ViewEventFragment() {
        // Required empty public constructor
    }

    public ViewEventFragment(Event event) {
        this.event = event;
        this.userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewEventBinding.inflate(inflater, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // If event is in the past, hide register/drop buttons
        if (event.getTime().compareTo(Timestamp.now()) < 0) {
            binding.buttonDrop.setVisibility(View.GONE);
            binding.buttonRegister.setVisibility(View.GONE);
        }
        // Determine whether the user is currently registered for the event. This will control
        // whether the Register or Drop button is shown.
        else {
            db.collection("habitProgress")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("eventDocId", event.getDocId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                binding.buttonDrop.setVisibility(View.VISIBLE);
                            }
                            else {
                                binding.buttonRegister.setVisibility(View.VISIBLE);
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                eventRegistration = document.toObject(HabitProgress.class);
                            }
                        }
                    });
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Determine if user has any matching habits. These habits will populate a habit
        // selection spinner on event registration.
        queryMatchingHabits();

        binding.eventTitleTextView.setText(event.getTitle().toUpperCase());
        binding.valueHostTextView.setText(event.getOwnerName().toUpperCase());
        binding.valueAboutTextView.setText(event.getDescription().toUpperCase());
        binding.valueTimeAndDateTextView.setText(event.getTime().toDate().toString());
        binding.buttonRegister.setOnClickListener(click -> registerDialog.show());
        binding.buttonDrop.setOnClickListener(click -> dropButtonClicked());
        binding.profilePhotoImageView.setImageResource(R.drawable.placeholder_profile);

        // If event host has been verified show the verified icon image along with their
        // name
        db.collection("users")
                .whereEqualTo("uid", event.getOwnerId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);

                            if (user.getVerified().equals("1")) {
                                binding.verifyIconImage.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    public Dialog createRegisterDialog() {
        trackedUserHabit = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.dialog_choose_habit, null);

        // Initialize the spinner containing a list of habit currently being tracked
        // to choose from.
        Spinner spinner = view.findViewById(R.id.habitsSpinner);

        mHabitSpinnerAdapter = new HabitSpinnerAdapter(view.getContext(),
                R.layout.habit_spinner_item,
                mUserHabits);

        spinner.setAdapter(mHabitSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                trackedUserHabit = mHabitSpinnerAdapter.getItem(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {  }
        });

        builder.setView(view)
                // Add action button for Register
                .setPositiveButton("Register", (dialog, id) -> registerButtonClicked())
                // Add action button for Cancel
                .setNegativeButton("Cancel", (dialog, id) -> {});
        return builder.create();
    }

    public void queryMatchingHabits(){
        db.collection("usersHabits")
                .whereEqualTo("userId", this.userId)
                .whereEqualTo("habitTypeID", event.getHabitType())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mUserHabits.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserHabitDoc userHabit = document.toObject(UserHabitDoc.class);
                            userHabit.setDocId(document.getId());
                            mUserHabits.add(userHabit);
                        }

                        registerDialog = createRegisterDialog();
                    }
                    else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public void registerButtonClicked() {
        DocumentReference docRef = db.collection("habitProgress").document();

        HashMap<String, Object> data = new HashMap<>();
        data.put("docId", docRef.getId());
        data.put("habitType", event.getHabitType());
        data.put("source", "event");
        data.put("userHabitDocId", trackedUserHabit.getDocId());
        data.put("eventDocId", event.getDocId());
        data.put("userId", this.userId);
        data.put("progressDate", event.getTime().toDate());
        data.put("createdAt", FieldValue.serverTimestamp());

        docRef.set(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                binding.buttonRegister.setVisibility(View.INVISIBLE);
                binding.buttonDrop.setVisibility(View.VISIBLE);
                mListener.eventRegister();
            }
            else {
                Optional.ofNullable(task.getException())
                        .map(Throwable::getMessage)
                        .ifPresent(message -> Log.d(TAG, message));
            }
        });
    }

    public void dropButtonClicked() {
        DocumentReference docRef = db.collection("habitProgress")
                .document(eventRegistration.getDocId());

        docRef.delete()
                .addOnSuccessListener(unused -> mListener.eventDrop())
                .addOnFailureListener(e -> Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error: Event registration not deleted!",
                                Toast.LENGTH_SHORT).show()));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        GeoPoint location = event.getLocation();

        // Set the map coordinates to the location of the event
        LatLng eventLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // Set the map type to Hybrid.
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Add a marker on the map coordinates.
        map.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title(event.getTitle())
                .zIndex(10));

        // Move the camera to the map coordinates.
        map.moveCamera(CameraUpdateFactory.newLatLng(eventLocation));

        // Display traffic.
        map.setTrafficEnabled(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (ViewEventListener)context;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();

        Optional.ofNullable((MainActivity)getActivity())
                .map(AppCompatActivity::getSupportActionBar)
                .ifPresent(actionBar -> actionBar.setTitle("View Event"));
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    interface ViewEventListener{
        void eventRegister();
        void eventDrop();
    }
}