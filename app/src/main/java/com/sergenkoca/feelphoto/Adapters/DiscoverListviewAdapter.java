package com.sergenkoca.feelphoto.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sergenkoca.feelphoto.Models.FeelImage;
import com.sergenkoca.feelphoto.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DiscoverListviewAdapter extends BaseAdapter {

    private ArrayList<FeelImage> feelImages;
    private Context context;
    private LayoutInflater layoutInflater;

    public DiscoverListviewAdapter(Context context, ArrayList<FeelImage> feelImages){
        this.context = context;
        this.feelImages = feelImages;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return feelImages.size();
    }

    @Override
    public Object getItem(int i) {
        return feelImages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View customView = layoutInflater.inflate(R.layout.discover_listview,null);
        ImageView image = (ImageView) customView.findViewById(R.id.discoverListview_image);
        TextView likeCount = (TextView) customView.findViewById(R.id.discoverListview_likeCount);

        //Picasso.get().load(feelImages.get(i).getUrl()).into(image);
        Glide.with(context).load(feelImages.get(i).getUrl()).into(image);
        likeCount.setText(feelImages.get(i).getDisplayCount()+" görüntülenme");

        /*like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        return customView;
    }
}
