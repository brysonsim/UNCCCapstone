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
import com.google.firebase.FirebaseError;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uncc.habittracker.data.model.VerificationRequest;
import com.uncc.habittracker.databinding.FragmentApproveVerificationBinding;
import com.uncc.habittracker.databinding.ItemVerificationRequestBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ApproveVerification extends Fragment {

    FragmentApproveVerificationBinding binding;
    ArrayList<VerificationRequest> mLists = new ArrayList<>();
    String TAG = "demo-test";
    public ApproveVerification() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentApproveVerificationBinding.inflate(inflater, container, false);

        binding.homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goBackToHome();
            }
        });

        return binding.getRoot();
    }

    VerificationAdapter adapter;
    ListenerRegistration listenerRegistration;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.approvalListRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new VerificationAdapter(this.mListener,getActivity());
        binding.approvalListRecyclerview.setAdapter(adapter);

        listenerRegistration = FirebaseFirestore
                .getInstance().
                collection("approve_verification")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            error.printStackTrace();
                            return;
                        }
                        mLists.clear();
                        for (QueryDocumentSnapshot doc : value){
                            VerificationRequest verificationRequest = doc.toObject(VerificationRequest.class);
                            verificationRequest.setCurrDocId(doc.getId());
                            mLists.add(verificationRequest);
                        }
                        Log.d(TAG, "onEvent: "+mLists);
                        adapter.notifyDataSetChanged();

                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null){
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    ApproveVerificationListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (ApproveVerificationListener) context;
    }

    interface ApproveVerificationListener{
        void goBackToHome();
    }



    public class VerificationAdapter extends RecyclerView.Adapter<VerificationAdapter.ViewHolder> {

        ApproveVerificationListener listener;
        private Context context;

        public VerificationAdapter(ApproveVerificationListener listener, Context context){
            super();
            this.listener = listener;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemVerificationRequestBinding binding = ItemVerificationRequestBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VerificationRequest verificationRequest = mLists.get(position);
            int backgroundColor = position % 2 == 0 ? R.color.light_background : R.color.dark_background;
            holder.itemView.setBackgroundResource(backgroundColor);
            holder.setupUI(verificationRequest);
        }

        @Override
        public int getItemCount() {
            return mLists.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {


            private final ItemVerificationRequestBinding mBinding;
            VerificationRequest mVerificationRequest;



            public ViewHolder(@NonNull ItemVerificationRequestBinding itemView) {
                super(itemView.getRoot());
                mBinding = itemView;
            }
            public void setupUI(VerificationRequest verificationRequest){
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                this.mVerificationRequest = verificationRequest;

                mBinding.textViewName.setText(mVerificationRequest.getFirstName()+ " "+mVerificationRequest.getLastName());
                mBinding.textViewtimeStamp.setText(mVerificationRequest.getFormattedTimestamp());

                mBinding.buttonApprove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateRecord("1");
                    }
                });

                mBinding.buttonDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateRecord("0");
                    }
                });

            }
            public void updateRecord(String value){

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> userData = new HashMap<>();
                userData.put("verified", value);

                db.collection("users")
                        .document(mVerificationRequest.getDocId())
                        .update(userData)
                        .addOnSuccessListener(aVoid -> {
                            db.collection("approve_verification").document(mVerificationRequest.getCurrDocId())
                                    .delete()
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, "Error : Server not Respsonding !", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });

                        })
                        .addOnFailureListener(e -> {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Error : Servor not responding", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });


            }

        }


    }

}