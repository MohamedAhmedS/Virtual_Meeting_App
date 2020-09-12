package com.example.virtualmeetingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;


public class VerificationActivity extends AppCompatActivity implements View.OnClickListener,
        OnOtpCompletionListener {
  private Button validateButton;
  private OtpView otpView;
  private android.app.ProgressDialog bd;
  private String mVerificationId;
  private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
  private FirebaseAuth mAuth;
  private PhoneAuthProvider.ForceResendingToken mResendToken;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_verification);
    initializeUi();
    setListeners();

    bd = new ProgressDialog(this);


    String verificationCode = otpView.getText().toString();

      if (verificationCode.equals(""))
      {
        Toast.makeText(VerificationActivity.this, "Please write verification code first.", Toast.LENGTH_SHORT).show();
      }
      else
      {
        bd.setTitle("Code Verification");
        bd.setMessage("Please wait, while we are verifying your code.");
        bd.setCanceledOnTouchOutside(false);
        bd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
        signInWithPhoneAuthCredential(credential);
      }

    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
      @Override
      public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
      {
        signInWithPhoneAuthCredential(phoneAuthCredential);
      }

      @Override
      public void onVerificationFailed(FirebaseException e)
      {
        Toast.makeText(VerificationActivity.this, "Something went wrong. . .", Toast.LENGTH_SHORT).show();

        bd.dismiss();

      }
    };

  }

  @Override public void onClick(View v) {
    if (v.getId() == R.id.btnVerify) {
      Toast.makeText(this, otpView.getText(), Toast.LENGTH_SHORT).show();
    }
  }

  private void initializeUi() {
    otpView = findViewById(R.id.otp_view);
    validateButton = findViewById(R.id.btnVerify);
  }

  private void setListeners() {
    validateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
      }
    });
    otpView.setOtpCompletionListener(this);
  }

  @Override public void onOtpCompleted(String otp) {
    // do Stuff
    Toast.makeText(this, "OnOtpCompletionListener called", Toast.LENGTH_SHORT).show();
  }

  private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
    mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                  bd.dismiss();
                  Toast.makeText(VerificationActivity.this, "Congratulations, you are logged in successfully.", Toast.LENGTH_SHORT).show();
                  sendUserToMainActivity();
                }
                else
                {
                  bd.dismiss();
                  String e = task.getException().toString();
                  Toast.makeText(VerificationActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                }
              }
            });
  }

  private void sendUserToMainActivity()
  {
    Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }
}
