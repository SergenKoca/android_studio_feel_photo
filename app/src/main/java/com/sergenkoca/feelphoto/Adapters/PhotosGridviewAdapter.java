package com.sergenkoca.feelphoto.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sergenkoca.feelphoto.Models.FeelImage;
import com.sergenkoca.feelphoto.R;

import java.util.ArrayList;

public class PhotosGridviewAdapter extends BaseAdapter {

    ArrayList<FeelImage> feelImages;
    LayoutInflater layoutInflater;
    Context context;

    public PhotosGridviewAdapter(ArrayList<FeelImage> feelImages,Context context) {
        super();
        this.feelImages = feelImages;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return feelImages.size();
    }

    @Override
    public Object getItem(int position) {
        return feelImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.photos_gridview, parent, false);
        }

        ImageView imageView =  convertView.findViewById(R.id.photos_gridview_image);
        Glide.with(context).load(feelImages.get(position).getUrl()).into(imageView);

        return convertView;
    }
}
