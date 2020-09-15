package com.example.virtualmeetingapp.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.bumptech.glide.Glide;

import com.example.virtualmeetingapp.CallingActivity;
import com.example.virtualmeetingapp.ChatActivity;
import com.example.virtualmeetingapp.MainActivity;
import com.example.virtualmeetingapp.Model.User;
import com.example.virtualmeetingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ImageViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isFragment;
    private List<User> mUser;

    private FirebaseUser firebaseUser;
    private String userName="", profileImage="";
    private String calledBy="";
//    private ProfileFragment profileFragment = new ProfileFragment();

    public UserAdapter(Context context, List<User> users, boolean isFragment) {
        mContext = context;
        mUsers = users;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public UserAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ImageViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

//        holder.userItem.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_anim));

        final Activity activity = (Activity) mContext;
        final User user = mUsers.get(position);

        if (!user.getId().equals(firebaseUser.getUid()) && firebaseUser != null) {

//            holder.btn_follow.setVisibility(View.VISIBLE);
//            isFollowing(user.getId(), holder.btn_follow);

            holder.username.setText(user.getUserName());
            holder.userType.setText(user.getUserType());
            holder.id.setText(user.getId());

//            if (user.getImageUrl() != null) {
//                Glide.with(mContext).load(user.getImageUrl()).into(holder.image_profile);
//            } else {
//            }
            Glide.with(mContext).load(R.drawable.placeholder).into(holder.image_profile);

            checkForReceivingCall();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isFragment) {
                        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileid", user.getId());
                        editor.apply();

//                        ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                                new ProfileFragment()).commit();

//                        getFragmentInstance.getInstance().setSearchFragment(profileFragment);
//                        ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.fragment_container,
//                                        getFragmentInstance.getInstance().getSearchFragment()).commit();
                    } else {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.putExtra("publisherid", user.getId());
                        mContext.startActivity(intent);
                        ((Activity) mContext).finish();
//                        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }
            });

                holder.btnChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        intent.putExtra("hisUid", user.getId());
                        intent.putExtra("myUid", firebaseUser.getUid());
                        mContext.startActivity(intent);
                        ((Activity) mContext).finish();

                    }

                });

                holder.btnVideoChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(mContext, CallingActivity.class);
                        intent.putExtra("hisUid", user.getId());
                        intent.putExtra("myUid", firebaseUser.getUid());
//                        intent.putExtra("visit_user_id", listUserId);
                        mContext.startActivity(intent);
                        ((Activity) mContext).finish();

                    }
                });
        } else {
            holder.userItem.setVisibility(View.GONE);
        }


    }

//    private void addNotification(String userid) {
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
//
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("userid", firebaseUser.getUid());
//            hashMap.put("text", "started following you");
//            hashMap.put("postid", "");
//            hashMap.put("ispost", false);
//
//            reference.push().setValue(hashMap);
//        }
//    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView username, userType, id;
        public CircularImageView image_profile;
        carbon.widget.TextView btnChat, btnVideoChat;
        ConstraintLayout userItem;

        ImageViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            userType = itemView.findViewById(R.id.tvUserType);
            id = itemView.findViewById(R.id.id);
            image_profile = itemView.findViewById(R.id.image_profile);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnVideoChat = itemView.findViewById(R.id.btnVideoChat);
            userItem = itemView.findViewById(R.id.userItem);
        }
    }

//    private void isFollowing(final String userid, final Button button) {
//
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (firebaseUser != null) {
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
//                    .child("Follow").child(Objects.requireNonNull(firebaseUser).getUid()).child("following");
//            reference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @SuppressLint("SetTextI18n")
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.child(userid).exists()) {
//                        button.setText("following");
//                    } else {
//                        button.setText("follow");
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }
//    }
private void checkForReceivingCall()
{
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUserId = mAuth.getCurrentUser().getUid();

    usersRef.child(currentUserId)
            .child("Ringing")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild("ringing"))
                    {
                        calledBy = dataSnapshot.child("ringing").getValue().toString();

                        Intent callingIntent = new Intent(mContext, CallingActivity.class);
                        callingIntent.putExtra("hisUid", calledBy);
                        mContext.startActivity(callingIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
}


}
