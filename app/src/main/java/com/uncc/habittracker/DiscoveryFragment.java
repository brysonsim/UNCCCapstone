package com.uncc.habittracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uncc.habittracker.data.model.User;
import com.uncc.habittracker.databinding.FragmentDiscoveryBinding;
import com.uncc.habittracker.databinding.FragmentFollowingBinding;
import com.uncc.habittracker.databinding.PeopleRowItemBinding;

import java.util.ArrayList;

public class DiscoveryFragment extends Fragment {

    ArrayList<User> mUsers = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FragmentDiscoveryBinding binding;
    public DiscoveryFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentDiscoveryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    UserAdapter adapter;
    ListenerRegistration listenerRegistration;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.RecyclerViewPeople.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter =  new UserAdapter();
        binding.RecyclerViewPeople.setAdapter(adapter);

        listenerRegistration = FirebaseFirestore.getInstance().collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(error != null){
                    error.printStackTrace();
                    return;
                }
                mUsers.clear();
                for(QueryDocumentSnapshot doc: value){
                    User user = doc.toObject(User.class);
                    mUsers.add(user);
                }
                adapter.notifyDataSetChanged();

            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(listenerRegistration != null)
        {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{
        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            PeopleRowItemBinding itemBinding = PeopleRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new UserViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = mUsers.get(position);
            holder.setupUI(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder{

            PeopleRowItemBinding mBinding;
            User mUser;
            public UserViewHolder(PeopleRowItemBinding itemBinding) {
                super(itemBinding.getRoot());
                mBinding = itemBinding;

            }

            public void setupUI(User user){
                this.mUser = user;
                mBinding.textViewProfileName.setText(mUser.getDisplayName());
                mBinding.buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mBinding.buttonViewProfile.getText().toString().compareToIgnoreCase("follow")==0)
                        {
                            mBinding.buttonViewProfile.setText("Unfollow");
                        }
                        else{
                            mBinding.buttonViewProfile.setText("Follow");

                        }
                    }
                });
            }


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Discovery");
    }
}