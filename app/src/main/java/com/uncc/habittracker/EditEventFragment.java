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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.uncc.habittracker.data.model.Event;
import com.uncc.habittracker.databinding.FragmentEditEventBinding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditEventFragment extends Fragment implements OnMapReadyCallback {

    MapView mapView;
    GoogleMap map;
    Event event;
    FragmentEditEventBinding binding;

    PlacesClient placesClient;

    LatLng currentLocation;

    public EditEventFragment() {
        // Required empty public constructor
    }

    public EditEventFragment(Event event){
        this.event = event;
    }

    public static EditEventFragment newInstance(String param1, String param2) {
        EditEventFragment fragment = new EditEventFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditEventBinding.inflate(inflater, container, false);

        mapView = binding.mapView2;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
//        make sure to pull the API key from the local properties   file
        Places.initialize(getActivity().getApplicationContext(),BuildConfig.MAPS_API_KEY);

        // Create a new PlacesClient instance
        placesClient = Places.createClient(getActivity());


        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //        Map auto complete fragment
        AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment)getChildFragmentManager().findFragmentById(R.id.autoCompleteLocationFragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS_COMPONENTS));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Location location = new Location("");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                currentLocation = new LatLng(location.getLatitude(),location.getLongitude());

                Log.d("CREATEVENT", currentLocation.toString());

            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("Error","An error occurred" + status);
            }
        });


        requireActivity().setTitle("Edit Event");

        binding.editTextEventTitle.setText(event.getTitle());
        binding.editTextTextDescription.setText(event.getDescription());
        binding.editTextLocation.setText(event.getLocation().toString());

        String habit = event.getHabitType();
        if (habit.equals("Study")){
            binding.typeSelector.check(R.id.rbStudy);
        }else if (habit.equals("Work Out")){
            binding.typeSelector.check(R.id.rbWorkOut);
        }else if (habit.equals("Mindfulness")){
            binding.typeSelector.check(R.id.rbLifestyle);
        }

        binding.buttonCancelEventEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.cancelEdit();
            }
        });

        binding.buttonEditevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("events").document(event.getDocId());

                HashMap<String, Object> data = new HashMap<>();


                RadioButton radioButton = (RadioButton) binding.typeSelector.findViewById(binding.typeSelector.getCheckedRadioButtonId());
                String habitType = radioButton.getText().toString();
                GeoPoint gp = new GeoPoint(0 , 0);
                if(currentLocation != null){
                    gp = new GeoPoint(currentLocation.latitude, currentLocation.longitude);
                }

                final GeoPoint finalGp = gp; // Declare a final variable

                data.put("title", binding.editTextEventTitle.getText().toString().toUpperCase());
                data.put("description", binding.editTextTextDescription.getText().toString().toUpperCase());
                data.put("habitType", habitType);
                data.put("location", finalGp);

                docRef.update(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Event Updated Successfully", Toast.LENGTH_SHORT).show();
                                        event.setTitle(binding.editTextEventTitle.getText().toString().toUpperCase());
                                        event.setDescription(binding.editTextTextDescription.getText().toString().toUpperCase());
                                        event.setHabitType(habitType);
                                        event.setLocation(finalGp); // Use the final variable here
                                        mListener.afterEditOpen(event);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Error: Server not responding", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
        });
    }

    EditFragmentListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (EditFragmentListener) context;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;

        GeoPoint location = event.getLocation();

        LatLng eventLocation = new LatLng(location.getLatitude(), location.getLongitude());

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        map.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title(event.getTitle())
                .zIndex(10));

        float zoomLevel = 15.0f; // You can set your desired zoom level here
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, zoomLevel));

        map.setTrafficEnabled(true);
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

    interface EditFragmentListener{
        void cancelEdit();
        void afterEditOpen(Event event);
    }
}