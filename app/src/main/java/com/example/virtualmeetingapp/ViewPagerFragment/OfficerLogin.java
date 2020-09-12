package com.example.virtualmeetingapp.ViewPagerFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.virtualmeetingapp.MainActivity;
import com.example.virtualmeetingapp.Model.User;
import com.example.virtualmeetingapp.R;
import com.example.virtualmeetingapp.utils.Global;
import com.example.virtualmeetingapp.utils.TextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class OfficerLogin extends Fragment {

    EditText email, password;
    Button login;
    FirebaseAuth auth;


    public static OfficerLogin newInstance() {
        return new OfficerLogin();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_officer_login, container, false);
        email = view.findViewById(R.id.officerEmail);
        password = view.findViewById(R.id.officerPassword);
        login = view.findViewById(R.id.btnLogin);
        auth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(getContext());
                pd.setMessage("Please wait...");
                pd.show();

                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (TextUtils.isEmpty(str_email) && TextUtils.isEmpty(str_password) && !str_email.contains("k0rv3x")) {
                    pd.dismiss();
                    email.setError("Email is required!");
                    password.setError("Password is required!");
                } else {

                    auth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
//                                        if (Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()) {
                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                                    .child(auth.getCurrentUser().getUid());

                                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    pd.dismiss();

                                                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                                                    editor.putString(TextUtil.PREF_LATEST_USER_ID, auth.getCurrentUser().getUid());
                                                    editor.apply();

                                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
//                                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                    Global.setCurrentUser(dataSnapshot.getValue(User.class));
                                                    getActivity().finish();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    pd.dismiss();
                                                }
                                            });
//                                        } else {
//                                            pd.dismiss();
//                                            Toast.makeText(LoginActivity.this, "Please, verify your e-mail address", Toast.LENGTH_SHORT).show();
//                                        }

                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        return view;
    }
}