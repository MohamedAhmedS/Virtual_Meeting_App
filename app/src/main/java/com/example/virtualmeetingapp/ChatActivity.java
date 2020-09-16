package com.example.virtualmeetingapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.circularimageview.CircularImageView;

import com.example.virtualmeetingapp.Model.ModelChat;
import com.example.virtualmeetingapp.Model.User;
import com.example.virtualmeetingapp.adapter.AdapterChat;
import com.example.virtualmeetingapp.notifications.Data;
import com.example.virtualmeetingapp.notifications.Sender;
import com.example.virtualmeetingapp.utils.Global;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    //views from xml
    RecyclerView recyclerView;
    CircularImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn, attachBtn;

    DatabaseReference usersDbRef;
    //for checking if use has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;
    FirebaseUser user;


    String hisUid;
    String myUid;
    String hisImage;

    //volley request queue for notification
    private RequestQueue requestQueue;

    private boolean notify = false;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;
    private String profileid;

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        String user1 = user.getUid();

        Intent intent = getIntent();
        hisUid = intent.getExtras().getString("hisUid");
        myUid = intent.getExtras().getString("myUid", user1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.proifleIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();

        checkUserStatus();
        usersDbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //get user picture and name
        usersDbRef.child(hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                User user = ds.getValue(User.class);
                assert user != null;
//                hisImage = user.getImageUrl();

                //check typing status
                if (user.getTypingTo().equals(myUid)) {
                    userStatusTv.setText("typing...");
                } else {
                    if (user.getOnlineStatus().equals("online")) {
                        userStatusTv.setText(user.getOnlineStatus());
                    } else {
                        //convert timestamp to proper time date
                        //convert time stamp to dd/mm/yyyy hh:mm am/pm
                        Calendar cal = Calendar.getInstance(Locale.getDefault());
                        cal.setTimeInMillis(Long.parseLong(user.getOnlineStatus()));
                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                        userStatusTv.setText("Last seen at: " + dateTime);

                    }
                }

                //set data
                nameTv.setText(user.getUserName());
//                if(user.getImageUrl().isEmpty()){
//                    profileIv.setImageResource(R.drawable.placeholder);
//                }
//                else {
//                    Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.default_user_avatar).into(profileIv);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //click button to send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                //get text from edit text
                final String message = messageEt.getText().toString().trim();
                //check if text is empty or not
                if (TextUtils.isEmpty(message)) {
                    //text empty
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(ChatActivity.this, "getInstanceId failed!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            String token = task.getResult().getToken();
//                            Toast.makeText(ChatActivity.this, "FCM New Token: " + token, Toast.LENGTH_LONG).show();
                            sendTextMessage(message);
                        }
                    });
                }
                //reset edittext after sending message
                messageEt.setText("");
            }
        });

        //click button to import image
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(ChatActivity.this);
            }
        });

        //check edit text change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(hisUid);//uid of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


//        checkOnlineStatus(currentUser.getOnlineStatus());
        readMessages();
        seenMessage();

    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference().child("Chats");
        seenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("seen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }

                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendTextMessage(final String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        ModelChat modelChat = new ModelChat();
        modelChat.setSender(myUid);
        modelChat.setReceiver(hisUid);
        modelChat.setMessage(message);
        modelChat.setTimestamp(timestamp);
        modelChat.setSeen(false);
        modelChat.setType("text");
        databaseReference.child("Chats").push().setValue(modelChat);

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (notify) {
                    assert user != null;
                    sendNotification(hisUid, user.getUserName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //create chatlist node/child in firebase database
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference().child("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference().child("Chatlist")
                .child(hisUid)
                .child(myUid);
        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendImageMessage(Uri imageUri, final ProgressDialog progressDialog) {
        notify = true;

        final String timeStamp = "" + System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/" + "post_" + timeStamp;

        /*Chats node will be created that will contain all images sent via chat*/

        //get bitmap from image uri
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri.toString(), options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray(); //conver image to bytes
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded
                        progressDialog.dismiss();
                        //get url of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();

                        if (uriTask.isSuccessful()) {
                            //add image uri and other info to database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            //setup required data
                            ModelChat modelChat = new ModelChat();
                            modelChat.setSender(myUid);
                            modelChat.setReceiver(hisUid);
                            modelChat.setMessage(downloadUri);
                            modelChat.setType("image");
                            modelChat.setTimestamp(timeStamp);
                            modelChat.setSeen(false);
                            //put this data to firebease
                            databaseReference.child("Chats").push().setValue(modelChat);

                            //send notification
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid);
                            database.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);

                                    if (notify) {
                                        assert user != null;
                                        sendNotification(hisUid, user.getUserName(), "Sent you a photo...");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            //create chatlist node/child in firebase database
                            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference().child("Chatlist")
                                    .child(myUid)
                                    .child(hisUid);
                            chatRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        chatRef1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference().child("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);
                            chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        chatRef2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                    }
                });


    }

    private void sendNotification(final String hisUid, final String name, final String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                User he = ds.getValue(User.class);
                Data data = new Data(
                        myUid,
                        name + ": " + message,
                        "New Message",
                        hisUid,
                        "ChatNotification",
                        R.drawable.placeholder);

                assert he != null;
                Sender sender = new Sender(data, he.getToken());

                //fcm json object request
                try {
                    JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    //response of the request
                                    Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            //put params
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            //todo replace your firebase fcm server key
                            //todo place your firaBasom Fcksem Serverato Kays
                            headers.put("Authorization", "key=AAAAWpjVRPA:APA91bFzPqn_WxXnk1psp8odOScWr3TAnw2pukb78aN0iXs8e5Pp4AvQKSYpNAf-FAhaK2Ztuo8tpyhoE7knsi3uHr3gMl23610M2Isy5sI_TK3tBzE5Qo1Trk95N7Rx3u31M3D6Zzi9");

                            return headers;
                        }
                    };

                    //add this request to queue
                    requestQueue.add(jsonObjectRequest);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {

        if (myUid != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            myUid = user.getUid(); //currently signed in user's uid
        } else {
            //user not signed in, go to main acitivity
//            startActivity(new Intent(this, MainActivity.class));
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//            finish();
            Toast.makeText(ChatActivity.this, "You not signed yet. Please sign in again.", Toast.LENGTH_LONG).show();
        }
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of onlineStatus of current user
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //update value of onlineStatus of current user
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
//        //set ofline with last seen time stamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //this method will be called after picking image from camera or gallery
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            Uri mImageUri = result.getUri();
//            ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
//            progressDialog.setMessage("Analyzing image..");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//            runTextRecognition(mImageUri, progressDialog);
        } else {
            Toast.makeText(this, "You quit camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onBackPressed();
    }
}
