package com.sergenkoca.feelphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sergenkoca.feelphoto.Models.FeelImage;
import com.sergenkoca.feelphoto.Models.ProfileImage;
import com.sergenkoca.feelphoto.Models.User;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView circleImageView;
    TextView profile_ac_upload_profile,email,username,congratulate_count,profile_ac_show_photos;
    String user_key = null;
    String user_email = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("Profilim");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // user_key'i bul
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("FeelPhoto").child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        if(user.getEmail().equals(user_email)){
                            user_key = ds.getKey();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
    }

    private void init(){
        circleImageView = findViewById(R.id.profile_ac_profile);
        email = findViewById(R.id.profile_ac_email);
        username = findViewById(R.id.profile_ac_username);
        congratulate_count = findViewById(R.id.profile_ac_congratulate_count);
        profile_ac_upload_profile = findViewById(R.id.profile_ac_upload_profile);
        profile_ac_show_photos = findViewById(R.id.profile_ac_show_photos);
    }

    private void setListener(){
        profile_ac_upload_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        profile_ac_show_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this,ShowPhotosActivity.class);
                intent.putExtra("user_key",user_key);
                intent.putExtra("is_own_photos",true);
                startActivity(intent);
            }
        });
    }

    private void getProfile(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String temp_email = firebaseAuth.getCurrentUser().getEmail();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        User user = new User();
                        user = ds.getValue(User.class);
                        if(user.getEmail().equals(temp_email)){
                            email.setText(user.getEmail());
                            username.setText(user.getUsername());
                            congratulate_count.setText(user.getCongratulateCount());
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


    private void pickImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                circleImageView.setImageBitmap(selectedImage);
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                ImageView imageView = new ImageView(ProfileActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(selectedImage);
                builder.setView(imageView);
                builder.setPositiveButton("Yükle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadImage(imageUri);
                    }
                });
                builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNeutralButton("Seç", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pickImage();
                    }
                });
                builder.show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(ProfileActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(final Uri file){
        Toast.makeText(ProfileActivity.this,"Fotoğraf yükleme durumu birazdan bildirilecek",Toast.LENGTH_SHORT).show();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("profileImages").child(firebaseAuth.getUid());
        storageReference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url = uri.toString();

                                final ProfileImage profileImage = new ProfileImage(user_key,download_url);
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("profileImages");
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            String main_key = null;
                                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                ProfileImage profileImage1 = ds.getValue(ProfileImage.class);
                                                if(profileImage1.getUserKey().equals(user_key)){
                                                    main_key = ds.getKey();
                                                    break;
                                                }
                                            }
                                            FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                                            DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto").child("profileImages").child(main_key).child("url");
                                            databaseReference1.setValue(profileImage.getUrl());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                //Toast.makeText(DiscoverActivity.this,"Yükleme Başarılı",Toast.LENGTH_SHORT).show();

                                // bildirim ekle
                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(ProfileActivity.this)
                                                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                                .setContentTitle("Feel Photo")
                                                .setContentText("Profil Fotoğrafı başarıyla yüklendi.");
                                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(2, mBuilder.build());
                                circleImageView.setImageURI(file);

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        // bildirim ekle
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(ProfileActivity.this)
                                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                        .setContentTitle("Feel Photo")
                                        .setContentText("Profil Fotoğrafı yüklenirken bir hata meydana geldi.Tekrar deneyin.");
                        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(2, mBuilder.build());
                    }
                });
    }
}
