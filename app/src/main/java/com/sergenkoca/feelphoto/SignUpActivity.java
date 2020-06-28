package com.sergenkoca.feelphoto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sergenkoca.feelphoto.Models.ProfileImage;
import com.sergenkoca.feelphoto.Models.User;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUpActivity extends AppCompatActivity {

    private EditText username,email,password,passwordAgain;
    private Button signUpBtn;
    private FloatingActionButton info;
    String user_key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setTitle(getString(R.string.signUp));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        setListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init(){
        username = findViewById(R.id.signUpAc_username);
        email = findViewById(R.id.signUpAc_email);
        password = findViewById(R.id.signUpAc_password);
        passwordAgain = findViewById(R.id.signUpAc_passwordAgain);
        signUpBtn = findViewById(R.id.signUpAc_signUpBtn);
        info = findViewById(R.id.signUpAc_info);
    }

    private void setListener(){
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignUpActivity.this);
                alertDialog.setMessage("Aşağıdaki durumlarda dikkate alınız.\n\n" +
                        "1. Epostanı doğrulamadığın takdirde hesap silinir.\n" +
                        "2. Eposta doğrulama linki süresi geçmiş ise hesap silinir. Tekrar kayıt olmanız gerekir.\n" +
                        "3. Kayıt olmada sorun yaşanıyorsa başka eposta ile kayıt olmayı deneyin.");
                alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().isEmpty() || email.getText().toString().isEmpty() || password.getText().toString().isEmpty()
                        || passwordAgain.getText().toString().isEmpty() || !password.getText().toString().equals(passwordAgain.getText().toString())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(getString(R.string.checkInfo));
                    builder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
                else{
                    try {
                        final AlertDialog builder = new AlertDialog.Builder(SignUpActivity.this).create();
                        ProgressBar progressBar = new ProgressBar(SignUpActivity.this);
                        builder.setView(progressBar);
                        builder.setCancelable(false);
                        builder.show();
                        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            firebaseAuth.getCurrentUser().sendEmailVerification()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Date date = new Date();
                                                                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                                                String createdAt = formatter.format(date);
                                                                User user = new User(username.getText().toString(),email.getText().toString(),false,false,"0", createdAt);
                                                                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                                                DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users").push();
                                                                databaseReference.setValue(user);


                                                                databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
                                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if(dataSnapshot.exists()){
                                                                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                                                User user1 = ds.getValue(User.class);
                                                                                if(user1.getEmail().equals(email.getText().toString())){
                                                                                    user_key = ds.getKey();


                                                                                    ProfileImage profileImage = new ProfileImage(user_key,"https://firebasestorage.googleapis.com/v0/b/little-projects-34fa3.appspot.com/o/user.png?alt=media&token=27fe7d36-4125-4b8c-8844-64d6041a4ab7");
                                                                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("FeelPhoto").child("profileImages").push();
                                                                                    databaseReference1.setValue(profileImage);

                                                                                    builder.dismiss();
                                                                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SignUpActivity.this);
                                                                                    builder1.setMessage(getString(R.string.verifyEmailSend));
                                                                                    builder1.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialog, int which) {

                                                                                        }
                                                                                    });
                                                                                    builder1.show();
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
                                                            else if (task.isCanceled()){
                                                                AlertDialog.Builder builder1 = new AlertDialog.Builder(SignUpActivity.this);
                                                                builder1.setMessage(getString(R.string.Error));
                                                                builder1.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                    }
                                                                });
                                                                builder.dismiss();
                                                                builder1.show();
                                                            }
                                                        }
                                                    });
                                        }
                                        else if(task.isCanceled()){

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(SignUpActivity.this);
                                builder1.setMessage(getString(R.string.Error));
                                builder1.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                builder.dismiss();
                                builder1.show();
                            }
                        });
                    }catch (Exception e){
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(SignUpActivity.this);
                        builder1.setMessage(getString(R.string.Error));
                        builder1.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder1.show();
                    }
                }
            }
        });
    }
}
