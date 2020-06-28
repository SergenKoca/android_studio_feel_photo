package com.sergenkoca.feelphoto.OtherUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sergenkoca.feelphoto.Models.ProfileImage;
import com.sergenkoca.feelphoto.Models.User;
import com.sergenkoca.feelphoto.R;
import com.sergenkoca.feelphoto.ShowPhotosActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherProfileActivity extends AppCompatActivity {

    CircleImageView circleImageView;
    TextView email,username,congratulate_count,profile_ac_show_photos,congratulate;
    String user_key = null;
    String user_email = null;
    String current_user_key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ohter_profile);
        getSupportActionBar().setTitle("Profil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        user_email = intent.getStringExtra("email");

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
        getProfile();
        getCongtStatus();
    }


    private void init(){
        circleImageView = findViewById(R.id.other_profile_ac_profile);
        email = findViewById(R.id.other_profile_ac_email);
        username = findViewById(R.id.other_profile_ac_username);
        congratulate_count = findViewById(R.id.other_profile_ac_congratulate_count);
        profile_ac_show_photos = findViewById(R.id.other_profile_ac_show_photos);
        congratulate = findViewById(R.id.other_profile_ac_congratulate);
    }

    private void setListener(){
        congratulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(congratulate.getText().toString().equalsIgnoreCase("Tebrik Et")){
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    final String current_user_email = firebaseAuth.getCurrentUser().getEmail();
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for (DataSnapshot ds:dataSnapshot.getChildren()){
                                    User user = ds.getValue(User.class);
                                    if(user.getEmail().equals(current_user_email)){
                                        current_user_key = ds.getKey();
                                        congratulate_post();
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
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users").child(user_key).child("congratulates").child(current_user_key);
                    databaseReference.removeValue();
                    congratulate.setText("Tebrik Et");
                }
            }
        });
        profile_ac_show_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherProfileActivity.this, ShowPhotosActivity.class);
                intent.putExtra("user_key",user_key);
                intent.putExtra("is_own_photos",false);
                startActivity(intent);
            }
        });
    }

    private void getProfile(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        User user = new User();
                        user = ds.getValue(User.class);
                        if(user.getEmail().equals(user_email)){
                            email.setText(user.getEmail());
                            username.setText(user.getUsername());
                            //congratulate_count.setText(user.getCongratulateCount());
                            user_key = ds.getKey();

                            FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto")
                                    .child("users").child(user_key).child("congratulates");
                            databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                    if(dataSnapshot1.exists()){
                                        congratulate_count.setText(dataSnapshot1.getChildrenCount()+"");
                                    }
                                    else{
                                        congratulate_count.setText("0");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            getProfilePhoto();
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

    private void getCongtStatus(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String current_user_email = firebaseAuth.getCurrentUser().getEmail();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        if(user.getEmail().equals(current_user_email)){
                            current_user_key = ds.getKey();
                            FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto").child("users").child(user_key).child("congratulates").child(current_user_key);
                            databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        congratulate.setText("Tebriği Geri Al");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
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

    private void getProfilePhoto(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("profileImages");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        ProfileImage profileImage = ds.getValue(ProfileImage.class);
                        if(profileImage.getUserKey().equals(user_key)){
                            Glide.with(getApplicationContext()).load(profileImage.getUrl()).into(circleImageView);
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

    private void congratulate_post(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users").child(user_key).child("congratulates").child(current_user_key);
        databaseReference.setValue(1);
        congratulate.setText("Tebriği Geri Al");
    }
}
