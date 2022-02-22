package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Fragments.WaitingFragment;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.Chat;
import com.example.chatapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;

    String theLastMessage;
    String type;
    int unread ;
    List<Chat> mChat;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.drawable.profile);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (isChat){
            lastMessage(user.getId(), holder.last_msg);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isChat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        if (isChat){
            coutunread(user.getId(),holder.unreadmessage);
        }else {
            holder.unreadmessage.setVisibility(View.GONE);
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;
        private TextView unreadmessage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            unreadmessage = itemView.findViewById(R.id.unreadmessage);
        }
    }

    // Check for the last Message.
    private void lastMessage(final String userId, final TextView last_msg) {
        type = "default";

        DatabaseReference lMReference = FirebaseDatabase.getInstance().getReference("Chats");
        lMReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null){
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                                chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {

                            theLastMessage = chat.getMessage();
                            type = chat.getType();
                        }
                    }
                }
                switch (type) {
                    case "default" :
                        last_msg.setText("No Message");
                        break;
                    case "text" :
                        last_msg.setText(theLastMessage);
                        break;
                    case "image" :
                        last_msg.setText("A Image");
                        break;
                    case "docx" :
                        last_msg.setText("A Word File");
                        break;
                    case "pdf" :
                        last_msg.setText("A PDF File");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                type = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void coutunread(String userId, TextView unreadmessage){
        DatabaseReference unreadReference = FirebaseDatabase.getInstance().getReference("Chats");
        unreadReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                unread =0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null){

                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) && !chat.getIsseen()){
                            unread++;
                        }
                    }
                    if (unread == 0){
                        unreadmessage.setVisibility(View.GONE);
                    }else {
                        unreadmessage.setVisibility(View.VISIBLE);
                        unreadmessage.setText(unread+"");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
