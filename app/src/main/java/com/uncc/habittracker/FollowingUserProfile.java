package com.uncc.habittracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uncc.habittracker.data.model.Follower;
import com.uncc.habittracker.data.model.Habit;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.databinding.FragmentFollowingBinding;
import com.uncc.habittracker.databinding.FragmentFollowingUserProfileBinding;

import java.util.ArrayList;
import java.util.HashMap;


public class FollowingUserProfile extends Fragment {

    User user;
    FirebaseFirestore db =  FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<Follower> followers = new ArrayList<>();
    int followerNum;




    public FollowingUserProfile(User user) {
        this.user = user;
        // Required empty public constructor
    }


    FragmentFollowingUserProfileBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        binding = FragmentFollowingUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();    }

    ListenerRegistration listenerRegistration;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        String firstName, lastName, uid, about, firebaseUid, verified;

        firstName = user.getFirstName();
        lastName = user.getLastName();
        about = user.getAbout();
        uid = user.getUid();
        binding.followerAbout.setText(about);
        binding.userNametxt.setText(firstName+" "+lastName);

        String currentUid = mAuth.getCurrentUser().getUid();
        Log.d("TAG", "current uid " + currentUid);
        Log.d("TAG", "current uid follower" + uid);

        listenerRegistration = FirebaseFirestore.getInstance().collection("followers").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Log.d("TAG", "inside error: ");
                    error.printStackTrace();
                    return;
                }
                else{
                    followers.clear();
                    for(QueryDocumentSnapshot doc: value){
                        Follower follower = doc.toObject(Follower.class);
                        followers.add(follower);
                    }

                    boolean followExists = false;

                    int x= followers.size();
                    binding.userFollowerNum.setText("Followers: " + followers.size());
                    for (int i = 0; i < x; i++) {
                        String inDocUser = followers.get(i).getUserID();
                        String inFollwerUser = followers.get(i).getFollowingID();
                        Log.d("TAG", "inDocUser: "  + inDocUser + "     "+ currentUid);
                        Log.d("TAG", "InfollowerUser: "  + inFollwerUser +"     " + uid);
                        if(inDocUser.compareTo(currentUid)==0 && inFollwerUser.compareTo(uid)==0)
                        {
                            followExists=true;
                        }
                    }
                    if(followExists)
                    {
                        binding.followButton.setText("Unfollow");
                    }
                    else{
                        binding.followButton.setText("Follow");
                    }
                }
            }
        });

        binding.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonText = binding.followButton.getText().toString();

                if(buttonText.compareTo("Follow") == 0)
                {
                    DocumentReference docRef = db.collection("followers").document();

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("userID", currentUid);
                    data.put("followingID", user.getUid());
                    data.put("docID", docRef.getId());


                    docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                binding.followButton.setText("Unfollow");
                            }else{
                                Toast.makeText(getActivity(), "wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    int x= followers.size();
                    String docid = "";
                    for (int i = 0; i < x; i++) {
                        String inDocUser = followers.get(i).getUserID();
                        String inFollwerUser = followers.get(i).getFollowingID();
                        if(inDocUser.compareTo(currentUid)==0 && inFollwerUser.compareTo(uid)==0)
                        {
                            docid = followers.get(i).getDocID();
                        }
                    }
                    db.collection("followers").document(docid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG", "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", "Error deleting document", e);
                                }
                            });

                }
            }
        });


    }

}