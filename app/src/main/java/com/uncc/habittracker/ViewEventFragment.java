package com.uncc.habittracker;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uncc.habittracker.data.model.Event;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.databinding.FragmentViewEventBinding;

public class ViewEventFragment extends Fragment implements OnMapReadyCallback {
    FragmentViewEventBinding binding;
    Event event;
    MapView mapView;
    GoogleMap map;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ViewEventFragment() {
        // Required empty public constructor
    }

    public ViewEventFragment(Event event) {
        this.event = event;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewEventBinding.inflate(inflater, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle("View Event");

        binding.eventTitleTextView.setText(event.getTitle().toUpperCase());
        binding.valueHostTextView.setText(event.getOwnerName().toUpperCase());
        binding.valueAboutTextView.setText(event.getDescription().toUpperCase());
        binding.valueTimeAndDateTextView.setText(event.getTime().toDate().toString());

        Fragment fragment = this;

        db.collection("users")
                .whereEqualTo("uid", event.getOwnerId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);


                                if (user.getVerified().equals("1")) {
                                    binding.verifyIconImage.setVisibility(View.VISIBLE);
                                }

                                // Create a storage reference from our app
                                StorageReference storageRef = storage.getReference();

                                StorageReference profileImageRef = storageRef.child("/profile_photos/" /*+ user.getProfilePhoto()*/);

                                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUrl) {
                                        Glide.with(fragment)
                                                .load(downloadUrl)
                                                .centerCrop()
                                                .into(binding.profilePhotoImageView);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        binding.profilePhotoImageView.setImageResource(R.drawable.placeholder_profile);
                                    }
                                });

                            }
                        }
                        else {
                            binding.profilePhotoImageView.setImageResource(R.drawable.placeholder_profile);
                        }
                    }
                });
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
    public void onResume() {
        mapView.onResume();
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("View Events");

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
}