package com.uncc.habittracker;

import android.content.Context;
import android.location.Location;
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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.uncc.habittracker.data.model.Event;
import com.uncc.habittracker.databinding.FragmentCreateEventsBinding;
import com.uncc.habittracker.databinding.FragmentCreateHabitBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

public class CreateEventsFragment extends Fragment implements OnMapReadyCallback {

    public MapView mapView;
    GoogleMap map;

    public CreateEventsFragment() {
        // Required empty public constructor
    }


    FragmentCreateEventsBinding binding;
    PlacesClient placesClient;

    LatLng currentLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateEventsBinding.inflate(inflater, container, false);

        // Initialize the map view
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize Places
        Places.initialize(getActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);

        // Create a new PlacesClient instance
        placesClient = Places.createClient(getActivity());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Map auto-complete fragment
        AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autoCompleteLocationFragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Location location = new Location("");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                Log.d("CREATEVENT", currentLocation.toString());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("Error", "An error occurred" + status);
            }
        });

        binding.buttonCancelEventCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.cancelEventCreation();
            }
        });

        binding.buttonCreateEventFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = binding.editTextEventTitle.getText().toString();
                String desc = binding.editTextTextMultiLineDescription.getText().toString();

                if (title.isEmpty()) {
                    Toast.makeText(getActivity(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (desc.isEmpty()) {
                    Toast.makeText(getActivity(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (binding.typeSelector.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getActivity(), "Please select a habit", Toast.LENGTH_SHORT).show();
                } else {
                    String TAG = "demo";

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    DocumentReference docRef = db.collection("events").document();
                    HashMap<String, Object> data = new HashMap<>();

                    RadioButton radioButton = (RadioButton) binding.typeSelector.findViewById(binding.typeSelector.getCheckedRadioButtonId());
                    String habitType = radioButton.getText().toString();
                    GeoPoint gp = new GeoPoint(0, 0);
                    if (currentLocation != null) {
                        gp = new GeoPoint(currentLocation.latitude, currentLocation.longitude);
                    }
                    data.put("location", gp);

                    data.put("title", title);
                    data.put("description", desc);
                    data.put("ownerId", auth.getCurrentUser().getUid());
                    data.put("ownerName", auth.getCurrentUser().getDisplayName());
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("time", FieldValue.serverTimestamp());
                    data.put("docId", docRef.getId());
                    data.put("habitType", habitType);

                    docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "passed putting data");
                                mListener.submitEventCreation();
                            } else {
                                Log.d(TAG, "failed putting data");
                            }
                        }
                    });

                    // Example: Add a marker on the map at the selected location
                    if (currentLocation != null && map != null) {
                        map.clear(); // Clear previous markers
                        map.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("Selected Location")
                                .zIndex(10));

                        float zoomLevel = 15.0f; // You can set your desired zoom level here
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
                    }
                }
            }
        });

    }

    CreateEventListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreateEventListener) context;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // You can customize the map settings here
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
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

    interface CreateEventListener {
        void cancelEventCreation();
        void submitEventCreation();
    }
}