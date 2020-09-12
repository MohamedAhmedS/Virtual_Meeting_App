package com.example.virtualmeetingapp.ViewPagerFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.virtualmeetingapp.MainActivity;
import com.example.virtualmeetingapp.Model.ImageModel;
import com.example.virtualmeetingapp.R;
import com.example.virtualmeetingapp.VerificationActivity;
import com.example.virtualmeetingapp.adapter.ImageAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

public class VisitorRegister extends Fragment {
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber = "";
    private String mVerificationId;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;

    private static final int IMAGE_CODE = 1;
    TextView selectBtn, noImage;
    RecyclerView recyclerView;
    List<ImageModel> imageModels;
    ImageAdapter imageAdapter;

    private StorageReference mStorageRef;

    public static VisitorRegister newInstance() {
        return new VisitorRegister();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_visitor_register, container, false);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(getContext());


        phoneText = view.findViewById(R.id.phoneNumber);
        continueAndNextBtn = view.findViewById(R.id.btnSendCode);
        selectBtn = view.findViewById(R.id.btnSelect);
        recyclerView = view.findViewById(R.id.recyclerViewId);
        noImage = view.findViewById(R.id.noImage);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        mStorageRef = FirebaseStorage.getInstance().getReference();


        imageModels = new ArrayList<>();

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, IMAGE_CODE);


            }
        });
          // originally it is empty so it will return null of an object reference
//        int itemCount = imageAdapter.getItemCount();
//
//        if (itemCount > 1) {
//            noImage.setVisibility(View.GONE);
//        }

        ccp = (CountryCodePicker) view.findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);


        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = ccp.getFullNumberWithPlus();
                if (!phoneNumber.equals("")) {
                    loadingBar.setTitle("Phone Number Verification");
                    loadingBar.setMessage("Please wait, while we are verifying your phone number.");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    Intent intent = new Intent(getContext(), VerificationActivity.class);
                    startActivity(intent);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, getActivity(), mCallbacks);
                } else {
                    Toast.makeText(getContext(), "Please write valid phone number.", Toast.LENGTH_SHORT).show();
                }

            }


        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getContext(), "Invalid Phone Number...", Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendToken = forceResendingToken;

                checker = "Code Sent";

                loadingBar.dismiss();
                Toast.makeText(getContext(), "Code has been sent, please check.", Toast.LENGTH_SHORT).show();
            }
        };
        return view;
    }

    // Put your logic like firebase etc. . .
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            Intent homeIntent = new Intent(getContext(), MainActivity.class);
            startActivity(homeIntent);
            getActivity().finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(getContext(), "Congratulations, you are logged in successfully.", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            loadingBar.dismiss();
                            String e = task.getException().toString();
                            Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendUserToMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK) {

            if (data != null) {
                if (data.getClipData() != null) {

                    int totalitem = data.getClipData().getItemCount();

                    if (totalitem != 2) {
                        Toast.makeText(getContext(), "Select 2 images of an ID (Back and forth)", Toast.LENGTH_LONG).show();
                        return;
                    }

                    for (int i = 0; i < totalitem; i++) {

                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        String imagename = getFileName(imageUri);

                        ImageModel modalClass = new ImageModel(imagename, imageUri);
                        imageModels.add(modalClass);

                        imageAdapter = new ImageAdapter(getContext(), imageModels);
                        recyclerView.setAdapter(imageAdapter);

                        noImage.setVisibility(View.GONE);


                        StorageReference mRef = mStorageRef.child("image").child(imagename);

                        mRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Fail" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }


                }
            } else {
                Toast.makeText(getContext(), "Select 2 images of an ID (Back and forth)", Toast.LENGTH_LONG).show();
            }
        }

    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Context applicationContext = getContext();

            Cursor cursor = applicationContext.getContentResolver().query(uri, null, null, null, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
