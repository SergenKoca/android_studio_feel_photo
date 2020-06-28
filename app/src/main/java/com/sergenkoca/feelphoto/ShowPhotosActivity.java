package com.sergenkoca.feelphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sergenkoca.feelphoto.Adapters.PhotosGridviewAdapter;
import com.sergenkoca.feelphoto.Models.FeelImage;
import com.sergenkoca.feelphoto.Models.User;

import java.util.ArrayList;

public class ShowPhotosActivity extends AppCompatActivity {

    TextView who;
    GridView photos;
    String user_key = null;
    boolean isOwnPhotos = false;
    ArrayList<FeelImage> feelImageArrayList = new ArrayList<>();
    TextView no_image;
    ImageView photo;
    LinearLayout detail;
    Button close,delete;
    String detail_photo_url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photos);
        getSupportActionBar().setTitle("Fotoğaflar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        user_key = intent.getStringExtra("user_key");
        isOwnPhotos = intent.getBooleanExtra("is_own_photos",false);

        init();
        setListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPhotos();
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

    private void init(){
        who = findViewById(R.id.show_photo_ac_who);
        photo = findViewById(R.id.show_photo_ac_image);
        detail = findViewById(R.id.show_photo_ac_detail);
        close = findViewById(R.id.show_photo_ac_close);
        delete = findViewById(R.id.show_photo_ac_delete);
        if(isOwnPhotos){
            who.setText("Fotoğraf sahibi > siz");
        }
        else{
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users").child(user_key);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        User user = dataSnapshot.getValue(User.class);
                        who.setText("Fotoğraf sahibi > "+user.getUsername());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        photos = findViewById(R.id.show_photo_ac_photos);
        no_image = findViewById(R.id.show_photo_ac_no_image);
    }

    private void setListener(){
        photos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isOwnPhotos == true){
                    FeelImage clicked_item = (FeelImage)photos.getItemAtPosition(position);
                    Glide.with(getApplicationContext()).load(clicked_item.getUrl()).into(photo);
                    photos.setEnabled(false);
                    detail.setVisibility(View.VISIBLE);
                    detail_photo_url = clicked_item.getUrl();
                }
                else{
                    FeelImage clicked_item = (FeelImage)photos.getItemAtPosition(position);
                    Glide.with(getApplicationContext()).load(clicked_item.getUrl()).into(photo);
                    photos.setEnabled(false);
                    detail.setVisibility(View.VISIBLE);
                    detail_photo_url = clicked_item.getUrl();
                    delete.setVisibility(View.GONE);
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photos.setEnabled(true);
                detail.setVisibility(View.GONE);
                detail_photo_url = null;
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowPhotosActivity.this);
                builder.setMessage("Fotoğrafı silmek istediğinden emin misin?");
                builder.setPositiveButton("Sil", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         deletePhoto();
                    }
                });
                builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
    }

    private void getPhotos(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("images");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    FeelImage feelImage = ds.getValue(FeelImage.class);
                    if(feelImage.getUserKey().equals(user_key)){
                        feelImageArrayList.add(feelImage);
                    }
                }
                if(feelImageArrayList.size() == 0){
                    photos.setVisibility(View.GONE);
                    no_image.setVisibility(View.VISIBLE);
                }
                else{
                    no_image.setVisibility(View.GONE);
                    connectAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void connectAdapter(){
        PhotosGridviewAdapter photosGridviewAdapter = new PhotosGridviewAdapter(feelImageArrayList,getApplicationContext());
        photos.setAdapter(photosGridviewAdapter);
    }

    private void deletePhoto(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("images");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String key = null;
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        FeelImage feelImage = ds.getValue(FeelImage.class);
                        if(feelImage.getUserKey().equals(user_key) && feelImage.getUrl().equals(detail_photo_url)){
                            key = ds.getKey();
                            detail_photo_url = null;
                            break;
                        }
                    }
                    FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto").child("images").child(key);
                    databaseReference1.removeValue();
                    detail.setVisibility(View.GONE);
                    Toast.makeText(ShowPhotosActivity.this, "Fotoğraf Silindi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
