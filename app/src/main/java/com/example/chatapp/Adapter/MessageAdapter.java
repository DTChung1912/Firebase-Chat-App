package com.example.chatapp.Adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.ImageViewerActivity;
import com.example.chatapp.MainActivity;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.Chat;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageURL;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    DeleteListener deleteListener;


    private ArrayList<String> deletePositionLists;

    private SharedPreferences prefDelete;


    public MessageAdapter(Context mContext, List<Chat> mChat, String imageURL) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageURL = imageURL;

        try {
            this.deleteListener = ((DeleteListener) mContext);
        }catch (ClassCastException e){
            throw new ClassCastException(e.getMessage());
        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Chat chat = mChat.get(position);

        if (imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.drawable.profile);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profile_image);
        }

        if (position == mChat.size()-1) {
            if (chat.getIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

        holder.showMessage.setVisibility(View.GONE);
        holder.deleteMessage.setVisibility(View.GONE);
        holder.imageFile.setVisibility(View.GONE);

        deletePositionLists = new ArrayList<>();

        prefDelete  = mContext.getSharedPreferences("PREFSDEl1", MODE_PRIVATE);

        Set<String> setDelete = prefDelete.getStringSet("isdelete", Collections.singleton("none"));
        deletePositionLists.addAll(setDelete);


        if (!deletePositionLists.contains(chat.getId()) ){
            if (chat.getType().equals("text") ){
                holder.showMessage.setVisibility(View.VISIBLE);
                holder.showMessage.setText(chat.getMessage());
            }
            else if (chat.getType().equals("image")){
                holder.imageFile.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(chat.getFileUrl()).into(holder.imageFile);
            }else if (chat.getType().equals("docx") || chat.getType().equals("pdf")){
                holder.imageFile.setVisibility(View.VISIBLE);
                holder.imageFile.setImageResource(R.drawable.file);
            }

        }else {
            holder.imageFile.setVisibility(View.GONE);
            holder.showMessage.setVisibility(View.GONE);
            holder.deleteMessage.setVisibility(View.VISIBLE);
        }

        if (chat.getSender().equals(firebaseUser.getUid())){
            holder.imageFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chat.getType().equals("docx")
                            || chat.getType().equals("pdf")){
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and View this doc cument",
                                        "Delete for me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getFileUrl()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteMessageForMe(chat, holder);
                                }else if (i == 2){
                                    deleteMessageForEveryone(chat);
                                }
                            }
                        });
                        builder.show();
                    }else if (chat.getType().equals("image")){
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View this Image",
                                        "Delete for me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",chat.getFileUrl());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteMessageForMe(chat, holder);
                                }else if (i == 2){
                                    deleteMessageForEveryone(chat);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
            holder.showMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chat.getType().equals("text")){
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Delete for Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){

                                    deleteMessageForMe(chat, holder);
                                }else if (i == 1){
                                    deleteMessageForEveryone(chat);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
            holder.deleteMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Undo",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Options?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (i == 0){
                                UndoMessage(chat,holder);

                            }
                        }
                    });
                    builder.show();
                }
            });
        }
        else {
            holder.imageFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chat.getType().equals("docx")
                            || chat.getType().equals("pdf")){
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and View this doc cument",
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getFileUrl()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if(i == 1){
                                    deleteMessageForMe(chat, holder);
                                }

                            }
                        });
                        builder.show();
                    }else if (chat.getType().equals("image")){
                        CharSequence options[] = new CharSequence[]
                                {
                                        "View this Image",
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",chat.getFileUrl());
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (i == 1){
                                    deleteMessageForMe(chat, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
            holder.showMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chat.getType().equals("text")){
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Options?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0){
                                    deleteMessageForMe(chat, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
            holder.deleteMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Undo",
                                    "Cancel"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Options?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (i == 0){
                                UndoMessage(chat,holder);
                            }
                        }
                    });
                    builder.show();
                }
            });

        }





    }

    private void deleteMessageForEveryone( Chat currentChat){
        reference = FirebaseDatabase.getInstance().getReference();
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    Chat chat1 = dataSnapshot.getValue(Chat.class);
//                    if (chat1.getId().equals(currentChat.getId())){
//                        HashMap<String,Object> hashMap = new HashMap<>();
//                        hashMap.put("isdeleted",true);
//                        dataSnapshot.getRef().updateChildren(hashMap);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        reference.child("Chats")
                .child(currentChat.getId())
                .removeValue();
    }

    private void deleteMessageForMe(Chat chat, MessageAdapter.ViewHolder holder ){
        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFSDEl1", MODE_PRIVATE).edit();
        Set<String> setDelete = new HashSet<String>();
        deletePositionLists.add(chat.getId());

        setDelete.addAll(deletePositionLists);
        editor.putStringSet("isdelete",setDelete);
        editor.apply();

        Log.d("TAG", "deleteMessageForMe: "+ deletePositionLists);

        holder.showMessage.setVisibility(View.GONE);
        holder.deleteMessage.setVisibility(View.VISIBLE);
        holder.imageFile.setVisibility(View.GONE);
    }

    private void UndoMessage(Chat chat, MessageAdapter.ViewHolder holder ){

        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFSDEl1", MODE_PRIVATE).edit();
        Set<String> setDelete = new HashSet<String>();
        deletePositionLists.remove(chat.getId());
        setDelete.addAll(deletePositionLists);
        editor.putStringSet("isdelete",setDelete);
        editor.apply();


        holder.deleteMessage.setVisibility(View.GONE);
        holder.showMessage.setVisibility(View.GONE);
        holder.imageFile.setVisibility(View.GONE);
        if (chat.getType().equals("text") ){
            holder.showMessage.setVisibility(View.VISIBLE);
            holder.showMessage.setText(chat.getMessage());
        }
        else if (chat.getType().equals("image")){
            holder.imageFile.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(chat.getFileUrl()).into(holder.imageFile);
        }else if (chat.getType().equals("docx") || chat.getType().equals("pdf")){
            holder.imageFile.setVisibility(View.VISIBLE);
            holder.imageFile.setImageResource(R.drawable.file);
        }


    }



    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView showMessage,deleteMessage;
        public ImageView profile_image,imageFile;
        public TextView txt_seen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.showMessage);
            deleteMessage = itemView.findViewById(R.id.deleteMessage);
            profile_image = itemView.findViewById(R.id.profile_image);
            imageFile = itemView.findViewById(R.id.imageFile);
            txt_seen = itemView.findViewById(R.id.txt_seen);

        }
    }



    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }


    public interface DeleteListener{
        public void deleteListener(Intent intent);
    }
}
