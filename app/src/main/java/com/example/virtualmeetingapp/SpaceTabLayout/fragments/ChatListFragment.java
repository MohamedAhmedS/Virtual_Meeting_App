package com.example.virtualmeetingapp.SpaceTabLayout.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.virtualmeetingapp.MainActivity;
import com.example.virtualmeetingapp.Model.ModelChat;
import com.example.virtualmeetingapp.Model.ModelChatlist;
import com.example.virtualmeetingapp.Model.User;
import com.example.virtualmeetingapp.R;
import com.example.virtualmeetingapp.adapter.AdapterChatlist;
import com.example.virtualmeetingapp.adapter.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatListFragment extends Fragment {

    //firebase auth
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private List<ModelChatlist> chatlistList;
    private List<User> userList;
    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private AdapterChatlist adapterChatlist;
    private EditText search_bar;
    private ImageView btnClose;
    private TextView noUser;

//    public ChatListFragment() {
//        // Required empty public constructor
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_chatlist_fragment, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        noUser = view.findViewById(R.id.noUser);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        search_bar = view.findViewById(R.id.search_bar);
        btnClose = view.findViewById(R.id.btnClose);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        noUser.setVisibility(View.VISIBLE);

        btnClose.setVisibility(View.GONE);


        if(btnClose.getVisibility() == View.GONE) {
            search_bar.setPadding(10, 0, 10, 0);
        }

        chatlistList = new ArrayList<>();

        checkUserStatus();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear();
                noUser.setVisibility(View.VISIBLE);

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                    noUser.setVisibility(View.GONE);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
                btnClose.setVisibility(View.VISIBLE);
                if(search_bar.getText().toString().matches(""))
                {
                    btnClose.setVisibility(View.GONE);
                }
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        search_bar.setText("");
                        btnClose.setVisibility(View.GONE);
                    }
                });

            }
        });
        return view;
    }
    private void filter(String text) {
        ArrayList<User> filteredList = new ArrayList<>();
        noUser.setVisibility(View.VISIBLE);
        for (User user : userList) {
            if (user.getUserName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
                noUser.setVisibility(View.GONE);
            }
        }
        adapterChatlist.filterList(filteredList);
    }
//    private void searchUsers(final String s) {
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
//
//        userRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                userList.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    if(user.getUserName().contains(s)) {
//                        userList.add(user);
//                    }
//                }
//
//                adapterChatlist = new AdapterChatlist(getContext(), userList);
//                recyclerView.setAdapter(adapterChatlist);
//                recyclerView.setItemViewCacheSize(1024);
//                adapterChatlist.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                noUser.setVisibility(View.VISIBLE);

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (ModelChatlist chatlist : chatlistList) {
                        assert user != null;
                        if (user.getId() != null && user.getId().equals(chatlist.getId())) {
                            userList.add(user);
                            noUser.setVisibility(View.GONE);
                            break;
                        }
                    }
                    //adapter
                    adapterChatlist = new AdapterChatlist(getContext(), userList);
                    //setadapter
                    recyclerView.setAdapter(adapterChatlist);
                    //set last message
                    for (int i = 0; i < userList.size(); i++) {
                        lastMessage(userList.get(i).getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) &&
                                    chat.getSender().equals(currentUser.getUid())) {
                        //instead of displaying url in message show "sent photo"
                        if (chat.getType().equals("image")) {
                            theLastMessage = "Sent a photo";
                        } else {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatlist = new AdapterChatlist(getContext(), userList);

                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                recyclerView.setAdapter(adapterChatlist);
                recyclerView.setItemViewCacheSize(1024);
                adapterChatlist.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), MainActivity.class));
//            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        setHasOptionsMenu(true);//to show menu option in fragment
//        super.onCreate(savedInstanceState);
//    }

    /*inflate options menu*/
//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
//        //inflating menu
//        inflater.inflate(R.menu.menu_main, menu);
//
//        super.onCreateOptionsMenu(menu, inflater);
//    }
}