package com.sergenkoca.feelphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sergenkoca.feelphoto.Adapters.DiscoverListviewAdapter;
import com.sergenkoca.feelphoto.Adapters.UserListAdapter;
import com.sergenkoca.feelphoto.Models.FeelImage;
import com.sergenkoca.feelphoto.Models.ProfileImage;
import com.sergenkoca.feelphoto.Models.User;
import com.sergenkoca.feelphoto.OtherUser.OtherProfileActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    LinearLayout noImages;
    LinearLayout existImages;
    SearchView searchView;
    ListView imageList;
    ProgressBar progressBar;
    FloatingActionButton uploadImage;
    private String user_key;
    ArrayList<FeelImage> feelImageList = new ArrayList<FeelImage>();
    LinearLayout detail;
    Button like,close;
    ImageView photo;
    String current_user_key=null;
    NotificationManager notifManager;
    public static boolean NOTIFI_STATE = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent intent = new Intent(DiscoverActivity.this,ProfileActivity.class);
                startActivity(intent);
                feelImageList.clear();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_log_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent1 = new Intent(DiscoverActivity.this,SignInActivity.class);
                startActivity(intent1);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        getSupportActionBar().setTitle(getString(R.string.discover));

        init();
        setListener();

        final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        // user_key'i bul
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("FeelPhoto").child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                        User user = new User();
                        user = ds.getValue(User.class);
                        if(user.getEmail().equals(email)){
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


    }

    @Override
    protected void onStart() {
        super.onStart();

        feelImageList.clear();
        getLastImages();
    }

    private void init(){
        noImages = findViewById(R.id.discoverAc_noImages);
        existImages = findViewById(R.id.discoverAc_exitsImages);
        searchView = findViewById(R.id.discoverAc_search);
        imageList = findViewById(R.id.discoverAc_listview);
        uploadImage = findViewById(R.id.discoverAc_uploadImage);
        progressBar = findViewById(R.id.discoverAc_mainProgress);
        detail = findViewById(R.id.discoverAc_detail);
        like = findViewById(R.id.discoverAc_like);
        close = findViewById(R.id.discoverAc_close);
        photo = findViewById(R.id.discoverAc_image);
    }

    private void setListener(){
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NOTIFI_STATE == true){
                    feelImageList.clear();
                    pickImage();
                }
                else{
                    Toast.makeText(DiscoverActivity.this, "Fotoğrafın Yüklenmesini Bekleyin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final FeelImage feelImage = (FeelImage)imageList.getItemAtPosition(position);
                    imageList.setEnabled(false);
                    searchView.setVisibility(View.GONE);
                    uploadImage.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(feelImage.getUrl()).into(photo);
                    detail.setVisibility(View.VISIBLE);
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchView.setVisibility(View.VISIBLE);
                            detail.setVisibility(View.GONE);
                            imageList.setEnabled(true);
                            uploadImage.setVisibility(View.VISIBLE);
                        }
                    });

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("images");
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String temp_key = null;
                                FeelImage temp_feel_image = null;
                                for (DataSnapshot ds:dataSnapshot.getChildren()){
                                    FeelImage temp = ds.getValue(FeelImage.class);
                                    if(temp.getUrl().equals(feelImage.getUrl())){
                                        temp_key = ds.getKey();
                                        temp_feel_image = temp;
                                        temp_feel_image.setDisplayCount(String.valueOf(Integer.valueOf(temp.getDisplayCount())+1));
                                        break;
                                    }
                                }

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("FeelPhoto").child("images").child(temp_key);
                                reference.setValue(temp_feel_image);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }catch (Exception e){
                    User user = (User)imageList.getItemAtPosition(position);
                    Intent intent = new Intent(DiscoverActivity.this, OtherProfileActivity.class);
                    intent.putExtra("email",user.getEmail());
                    startActivity(intent);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){
                    String[] split_s = newText.split("#");
                    if(split_s.length > 1){
                        searchPhoto(split_s[1]);
                    }
                    else{
                        searchUser(newText);
                    }
                }
                return false;
            }
        });

    }

    private void getLastImages(){
        if(current_user_key!=null){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FeelPhoto");
            Query lastQuery = databaseReference.child("images").orderByKey().limitToLast(10);
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            FeelImage  feelImage = new FeelImage();
                            feelImage = ds.getValue(FeelImage.class);
                            if(!feelImage.getUserKey().equals(current_user_key))
                                feelImageList.add(feelImage);
                        }
                        if(feelImageList.size()>0){
                            connectAdapterForImages();
                        }
                        else{
                            noImages.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            imageList.setVisibility(View.GONE);
                        }
                    }
                    else {
                        noImages.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        imageList.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        }
        else{
            getCurrentUserKeyForLastImage();
        }
    }
    private void getCurrentUserKeyForLastImage(){
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
                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("FeelPhoto");
                            Query lastQuery = databaseReference1.child("images").orderByKey().limitToLast(10);
                            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                                            FeelImage  feelImage = new FeelImage();
                                            feelImage = ds.getValue(FeelImage.class);
                                            if(!feelImage.getUserKey().equals(current_user_key))
                                                feelImageList.add(feelImage);
                                        }
                                        if(feelImageList.size()>0){
                                            connectAdapterForImages();
                                        }
                                        else{
                                            noImages.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                            imageList.setVisibility(View.GONE);
                                        }

                                    }
                                    else {
                                        noImages.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        imageList.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle possible errors.
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

    private void connectAdapterForImages(){
        DiscoverListviewAdapter discoverListviewAdapter = new DiscoverListviewAdapter(DiscoverActivity.this,feelImageList);
        imageList.setAdapter(discoverListviewAdapter);
        progressBar.setVisibility(View.GONE);
        noImages.setVisibility(View.GONE);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(DiscoverActivity.this);
                ImageView imageView = new ImageView(DiscoverActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(selectedImage);
                builder.setView(imageView);
                builder.setPositiveButton("Devam Et", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(DiscoverActivity.this);
                        final EditText editText = new EditText(DiscoverActivity.this);
                        editText.setHint("Fotoğrafı anlatan bir kaç kelime girin.(*boşluk bırakarak)");
                        builder1.setView(editText);
                        builder1.setPositiveButton("Yükle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                uploadImage(imageUri,editText.getText().toString());
                            }
                        });
                        builder1.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder1.show();
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
                Toast.makeText(DiscoverActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(DiscoverActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(Uri file, final String tags){
        NOTIFI_STATE = false;
        Toast.makeText(DiscoverActivity.this,"Fotoğraf yükleme durumu birazdan bildirilecek",Toast.LENGTH_SHORT).show();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("images").child(firebaseAuth.getUid()).child(System.currentTimeMillis()+"");
        storageReference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url = uri.toString();

                                FeelImage feelImage = new FeelImage(user_key,download_url,"0",tags);
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("images").push();
                                databaseReference.setValue(feelImage);
                                //Toast.makeText(DiscoverActivity.this,"Yükleme Başarılı",Toast.LENGTH_SHORT).show();

                                // bildirim ekle
                                CreateNotification(false);

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
                        CreateNotification(true);
                    }
                });
    }

    private void CreateNotification(boolean err){
        if(err==false){
            final int NOTIFY_ID = 0; // ID of notification
            String id = "1";
            String title = "Feel Photo";
            NotificationCompat.Builder builder;
            if (notifManager == null) {
                notifManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notifManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, title, importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(getApplicationContext(), id);
                builder.setContentTitle("Fotoğraf Başarıyla Yüklendi")
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentText(getApplicationContext().getString(R.string.app_name))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        //.setTicker(aMessage)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            }
            else {
                builder = new NotificationCompat.Builder(getApplicationContext(), id);
                builder.setContentTitle("Fotoğraf Başarıyla Yüklendi")
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentText(getApplicationContext().getString(R.string.app_name))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        //.setTicker(aMessage)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH);
            }
            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);
            NOTIFI_STATE = true;
        }
        else{
            final int NOTIFY_ID = 0; // ID of notification
            String id = "1";
            String title = "Feel Photo";
            NotificationCompat.Builder builder;
            if (notifManager == null) {
                notifManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notifManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, title, importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(getApplicationContext(), id);
                builder.setContentTitle("Fotoğraf Yükleme Başarısız")
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentText(getApplicationContext().getString(R.string.app_name))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setTicker("Fotoğraf yüklenirken bir hata meydana geldi. Tekrar Deneyin.")
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            }
            else {
                builder = new NotificationCompat.Builder(getApplicationContext(), id);
                builder.setContentTitle("Fotoğraf Yükleme Başarısız")
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentText(getApplicationContext().getString(R.string.app_name))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setTicker("Fotoğraf yüklenirken bir hata meydana geldi. Tekrar Deneyin.")
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH);
            }
            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);
            NOTIFI_STATE = true;
        }
    }

    private void searchPhoto(final String s){
        if(current_user_key != null){
            final String[] tags = s.split(" ");
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("images");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        ArrayList<FeelImage> searchImageList = new ArrayList<>();
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            FeelImage feelImage = ds.getValue(FeelImage.class);
                            String temp_tag = feelImage.getTags();
                            String[] arr_tag = temp_tag.split(" ");
                            boolean flag = false;
                            for (String tag:arr_tag){
                                for (String search_tag:tags){
                                    if(search_tag.equalsIgnoreCase(tag) && !feelImage.getUserKey().equals(current_user_key)){
                                        flag = true;
                                        break;
                                    }
                                }
                                if(flag==true)
                                    break;
                            }
                            if(flag==true){
                                searchImageList.add(feelImage);
                                flag = false;
                                break;
                            }
                        }

                        if(searchImageList.size() > 0){
                            DiscoverListviewAdapter discoverListviewAdapter = new DiscoverListviewAdapter(DiscoverActivity.this,searchImageList);
                            imageList.setAdapter(discoverListviewAdapter);
                            progressBar.setVisibility(View.GONE);
                            noImages.setVisibility(View.GONE);
                            imageList.setVisibility(View.VISIBLE);

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            getCurrentUserKeyForSearchPhoto(s);
        }
    }
    private void getCurrentUserKeyForSearchPhoto(final String s){
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
                            final String[] tags = s.split(" ");
                            FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto").child("images");
                            databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        ArrayList<FeelImage> searchImageList = new ArrayList<>();
                                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                                            FeelImage feelImage = ds.getValue(FeelImage.class);
                                            String temp_tag = feelImage.getTags();
                                            String[] arr_tag = temp_tag.split(" ");
                                            boolean flag = false;
                                            for (String tag:arr_tag){
                                                for (String search_tag:tags){
                                                    if(search_tag.equalsIgnoreCase(tag) && !feelImage.getUserKey().equals(current_user_key)){
                                                        flag = true;
                                                        break;
                                                    }
                                                }
                                                if(flag==true)
                                                    break;
                                            }
                                            if(flag==true){
                                                searchImageList.add(feelImage);
                                                flag = false;
                                                break;
                                            }
                                        }

                                        if(searchImageList.size() > 0){
                                            DiscoverListviewAdapter discoverListviewAdapter = new DiscoverListviewAdapter(DiscoverActivity.this,searchImageList);
                                            imageList.setAdapter(discoverListviewAdapter);
                                            progressBar.setVisibility(View.GONE);
                                            noImages.setVisibility(View.GONE);
                                            imageList.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchUser(final String s){
       if(current_user_key != null){
           FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
           DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
           databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if(dataSnapshot.exists()){
                       ArrayList<User> userList = new ArrayList<>();
                       ArrayList<String> userKeyList = new ArrayList<>();
                       for (DataSnapshot ds:dataSnapshot.getChildren()){
                           User user = ds.getValue(User.class);
                           if(user.getUsername().toLowerCase().contains(s.toLowerCase()) && !ds.getKey().equals(current_user_key)){
                               userList.add(user);
                               userKeyList.add(ds.getKey());
                           }
                       }

                       if(userList.size() > 0){
                           getProfileImages(userList,userKeyList);
                       }
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
       }
       else{
           getCurrentUserKeyForSearchUser(s);
       }
    }
    private void getCurrentUserKeyForSearchUser(final String s){
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
                                        FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                                        DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto").child("users");
                                        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    ArrayList<User> userList = new ArrayList<>();
                                                    ArrayList<String> userKeyList = new ArrayList<>();
                                                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                        User user = ds.getValue(User.class);
                                                        if(user.getUsername().toLowerCase().contains(s.toLowerCase()) && !ds.getKey().equals(current_user_key)){
                                                            userList.add(user);
                                                            userKeyList.add(ds.getKey());
                                                        }
                                                    }

                                                    if(userList.size() > 0){
                                                        getProfileImages(userList,userKeyList);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
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
    private void getProfileImages(final ArrayList<User> userList, final ArrayList<String> userKeyList){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("profileImages");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ArrayList<ProfileImage> profileImageArrayList = new ArrayList<>();
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        ProfileImage profileImage = ds.getValue(ProfileImage.class);
                        for (String key:userKeyList){
                            if(profileImage.getUserKey().equals(key)){
                                profileImageArrayList.add(profileImage);
                            }
                        }
                    }

                    UserListAdapter userListAdapter = new UserListAdapter(getApplicationContext(),profileImageArrayList,userList);
                    imageList.setAdapter(userListAdapter);
                    imageList.setVisibility(View.VISIBLE);
                    noImages.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openSettings(){
        final Dialog dialog = new Dialog(DiscoverActivity.this);
        dialog.setContentView(R.layout.settings);
        ListView listView = dialog.findViewById(R.id.settings_listview);
        String[] options = {"Şifre Değiştir","Kullanıcı Adını Değiştir"};
        ArrayAdapter<String> adapter=new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    dialog.dismiss();
                    final Dialog dialog1 = new Dialog(DiscoverActivity.this);
                    dialog1.setContentView(R.layout.change_password);
                    final EditText old_pass = dialog1.findViewById(R.id.change_password_old_pass);
                    final EditText new_pass = dialog1.findViewById(R.id.change_password_new_pass);
                    final EditText new_pass_again = dialog1.findViewById(R.id.change_password_new_pass_again);
                    Button update = dialog1.findViewById(R.id.change_password_update);
                    Button close_dialog = dialog1.findViewById(R.id.change_password_close);
                    close_dialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog1.dismiss();
                        }
                    });
                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(old_pass.getText() != null && new_pass.getText() != null && new_pass_again.getText() != null && new_pass.getText().toString().equals(new_pass_again.getText().toString())){

                                AlertDialog.Builder builder = new AlertDialog.Builder(DiscoverActivity.this);
                                final AlertDialog alertDialog = builder.create();
                                ProgressBar progressBar = new ProgressBar(DiscoverActivity.this);
                                alertDialog.setCancelable(false);
                                alertDialog.setView(progressBar);
                                alertDialog.show();
                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(user.getEmail(), old_pass.getText().toString());

                                user.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    user.updatePassword(new_pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                alertDialog.dismiss();
                                                                Toast.makeText(DiscoverActivity.this, "Şifre Başarıyla Güncellendi", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                alertDialog.dismiss();
                                                                Toast.makeText(DiscoverActivity.this, "Şifre Güncellenirken Bir Hata Meydana Geldi", Toast.LENGTH_SHORT).show();

                                                            }
                                                        }
                                                    });
                                                } else {
                                                    alertDialog.dismiss();
                                                    Toast.makeText(DiscoverActivity.this, "Kimlik Doğrulama Başarısız", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            else{
                                Toast.makeText(DiscoverActivity.this, "Lütfen Bilgilerinizi Kontrol Edin.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog1.show();
                }
                else if(position == 1){
                    final Dialog dialog1 = new Dialog(DiscoverActivity.this);
                    dialog1.setContentView(R.layout.change_username);
                    final EditText username_edit = dialog1.findViewById(R.id.change_username_username);
                    Button update = dialog1.findViewById(R.id.change_username_update);
                    Button close_dialog = dialog1.findViewById(R.id.change_username_close);
                    close_dialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog1.dismiss();
                        }
                    });
                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DiscoverActivity.this);
                            final AlertDialog alertDialog = builder.create();
                            ProgressBar progressBar = new ProgressBar(DiscoverActivity.this);
                            alertDialog.setView(progressBar);
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference = firebaseDatabase.getReference("FeelPhoto").child("users");
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        boolean flag = true;
                                        String temp_key = null;
                                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                                            User user = ds.getValue(User.class);
                                            if(user.getUsername().equals(username_edit.getText().toString())){
                                                flag = false;
                                                break;
                                            }
                                        }
                                        if(flag){
                                            FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                                            DatabaseReference databaseReference1 = firebaseDatabase1.getReference("FeelPhoto").child("users").child(temp_key).child("username");
                                            databaseReference1.setValue(username_edit.getText().toString());
                                            Toast.makeText(DiscoverActivity.this, "Kullanıcı Adı Başarıyla Güncellendi", Toast.LENGTH_SHORT).show();
                                            alertDialog.dismiss();
                                        }
                                        else{
                                            alertDialog.dismiss();
                                            Toast.makeText(DiscoverActivity.this, "Kullanıcı Adı Kullanılmaktadır.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    alertDialog.dismiss();
                                }
                            });
                        }
                    });
                    dialog1.show();
                }
            }
        });

        dialog.show();
    }

}
