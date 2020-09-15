package com.example.virtualmeetingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.virtualmeetingapp.ChatActivity;
import com.example.virtualmeetingapp.Model.User;
import com.example.virtualmeetingapp.R;
import com.example.virtualmeetingapp.SpaceTabLayout.fragments.ChatListFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    private Context mContext;
    private List<User> userList; //get user info
    private HashMap<String, String> lastMessageMap;
    private FirebaseUser mUser;
    public AdapterChatlist(Context context, List<User> userList) {
        this.mContext = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_chatlist.xml
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_chatlist, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data
        final String hisUid = userList.get(i).getId();
//        String userImage = userList.get(i).getImageUrl();
        String userName = userList.get(i).getUserName();
        String lastMessage = lastMessageMap.get(hisUid);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        //set data
        myHolder.nameTv.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")) {
            myHolder.lastMessageTv.setVisibility(View.GONE);
        } else {
            myHolder.lastMessageTv.setVisibility(View.VISIBLE);
            myHolder.lastMessageTv.setText(lastMessage);
        }
        try {
            Picasso.get().load(R.drawable.placeholder).into(myHolder.profileIv);
        } catch (Exception e) {
        }
        //set online status of other users in chatlist
        if (userList.get(i).getOnlineStatus().equals("online")) {
            //online
            myHolder.onlineStatusIv.setImageResource(R.drawable.online);
        } else {
            //offline
            myHolder.onlineStatusIv.setImageResource(R.drawable.offline);
        }


        //handle click of user in chatlist
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                intent.putExtra("currentUser", mUser);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                ((Activity)mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size(); //size of the list
    }


    class MyHolder extends RecyclerView.ViewHolder {
        //views of row_chatlist.xml
        ImageView onlineStatusIv;
        CircularImageView profileIv;
        TextView nameTv, lastMessageTv;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }

    public void filterList(ArrayList<User> filteredList) {
        userList = filteredList;
        notifyDataSetChanged();
    }
}