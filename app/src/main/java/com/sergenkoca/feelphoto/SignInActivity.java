package com.sergenkoca.feelphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sergenkoca.feelphoto.Models.User;

public class SignInActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button signInBtn;
    private TextView notYetAccount;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().setTitle(getString(R.string.signIn));

        init();
        setListener();
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()){
            Intent intent = new Intent(SignInActivity.this,DiscoverActivity.class);
            startActivity(intent);
        }

    }

    private void init(){
        email = findViewById(R.id.signInAc_email);
        password = findViewById(R.id.signInAc_password);
        signInBtn = findViewById(R.id.signInAc_signInBtn);
        notYetAccount = findViewById(R.id.signInAc_notYetAccount);
        forgotPassword = findViewById(R.id.signInAc_forgotPassword);
    }

    private void setListener(){
        notYetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setMessage(R.string.checkInfo);
                    builder.show();
                }
                else{
                    final AlertDialog builder0 = new AlertDialog.Builder(SignInActivity.this).create();
                    ProgressBar progressBar = new ProgressBar(SignInActivity.this);
                    builder0.setView(progressBar);
                    builder0.setCancelable(false);
                    builder0.show();
                    final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        if(firebaseAuth.getCurrentUser() != null){
                                            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                                // devam edebilir.
                                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                                DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){
                                                            User user = new User();
                                                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                                                user = ds.getValue(User.class);
                                                                if(user.getEmail().equals(email.getText().toString())){
                                                                    user.setActive(true);
                                                                    user.setEmailVerify(true);
                                                                    String key = ds.getKey();
                                                                    FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                                                                    DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto").child("users").child(key);
                                                                    databaseReference1.setValue(user);
                                                                    Intent intent = new Intent(SignInActivity.this,DiscoverActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                            else{
                                                builder0.dismiss();
                                                // email doğrulanmamamış.
                                                firebaseAuth.signOut();
                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                                                builder.setMessage(getString(R.string.notYetVerifyEmail));
                                                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                });
                                                builder.show();
                                            }
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            builder0.cancel();
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(SignInActivity.this);
                            builder2.setMessage(R.string.checkInfo);
                            builder2.show();
                        }
                    });
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(SignInActivity.this);
                dialog.setContentView(R.layout.forgot_password);
                final EditText email_forgot = dialog.findViewById(R.id.forgot_password_email);
                Button submit = dialog.findViewById(R.id.forgot_password_submit);
                Button close = dialog.findViewById(R.id.forgot_password_close);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(email_forgot.getText()!=null){
                            dialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                            final AlertDialog alertDialog = builder.create();
                            ProgressBar progressBar = new ProgressBar(SignInActivity.this);
                            alertDialog.setCancelable(false);
                            alertDialog.setView(progressBar);
                            alertDialog.show();
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email_forgot.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                alertDialog.dismiss();
                                                Toast.makeText(SignInActivity.this, "Lütfen Epostanızı Kontrol Edin", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    alertDialog.dismiss();
                                    Toast.makeText(SignInActivity.this, "Bir Hata Meydana Geldi", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(SignInActivity.this, "Lütfen Bilgilerinizi Kontrol Edin", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
            }
        });
    }
}
