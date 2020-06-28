package com.sergenkoca.feelphoto.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sergenkoca.feelphoto.Models.FeelImage;
import com.sergenkoca.feelphoto.Models.ProfileImage;
import com.sergenkoca.feelphoto.Models.User;
import com.sergenkoca.feelphoto.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends BaseAdapter {

    ArrayList<ProfileImage> profileImages;
    ArrayList<User> users;
    private Context context;
    private LayoutInflater layoutInflater;

    public UserListAdapter(Context context,ArrayList<ProfileImage> profileImages,ArrayList<User> users){
        this.context = context;
        this.profileImages = profileImages;
        this.users = users;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View customView = layoutInflater.inflate(R.layout.user_listview,null);

        CircleImageView circleImageView = customView.findViewById(R.id.user_listview_image);
        TextView username = customView.findViewById(R.id.user_listview_username);
        TextView congt_count = customView.findViewById(R.id.user_listview_congt_count);

        try {
            Glide.with(context).load(profileImages.get(position).getUrl()).into(circleImageView);
            username.setText(users.get(position).getUsername());
            congt_count.setText(users.get(position).getCongratulateCount()+" Tebrik");
        }catch (Exception e){
            System.out.println("err yakaladim.user list adapter error: "+e.getMessage());
        }


        return customView;
    }
}
