package com.example.chatapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.MainActivity;
import com.example.chatapp.Notifications.Token;
import com.example.chatapp.R;
import com.example.chatapp.model.Chat;
import com.example.chatapp.model.Chatlist;
import com.example.chatapp.model.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


public class WaitingFragment extends Fragment {

    private RecyclerView recyclerView;


    private List<User> mUsers;

    FirebaseUser firebaseUser;
    DatabaseReference dbReference;

    private List<Chatlist> senderList;
    private List<Chatlist> reiceiverList;

    private UserAdapter userAdapter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_waiting, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        senderList = new ArrayList<>();
        reiceiverList = new ArrayList<>();
        mUsers = new ArrayList<>();



        dbReference = FirebaseDatabase.getInstance().getReference("Chatlist/Receiver").child(firebaseUser.getUid());
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reiceiverList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    reiceiverList.add(chatlist);
                }
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("Chatlist/Sender").child(firebaseUser.getUid());
                db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                        senderList.clear();
                        for (DataSnapshot snapshot1 : dataSnapshot1.getChildren()) {
                            Chatlist chatlist = snapshot1.getValue(Chatlist.class);
                            senderList.add(chatlist);
                        }
                        chatList();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        dbReference = FirebaseDatabase.getInstance().getReference("Users");
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (Chatlist reilist : reiceiverList) {
                        if (user.getId().equals(reilist.getId())){
                            mUsers.add(user);
                        }
                    }
                    for (Chatlist senlist : senderList){
                        if (user.getId().equals(senlist.getId())){
                            mUsers.remove(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}