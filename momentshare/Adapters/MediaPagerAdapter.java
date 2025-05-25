package com.example.momentshare.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.momentshare.R;

import java.util.ArrayList;

public class MediaPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> mediaUrls;  // List of media URLs (either image or video)
    private Context context;

    public MediaPagerAdapter(Context context, ArrayList<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
    }

    @Override
    public int getItemCount() {
        if (mediaUrls.isEmpty())
            return 0;
        return mediaUrls.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_image_media, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mediaUrls.size() - 1 < position)
            return;
        String mediaUrl = mediaUrls.get(position);
        ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
        Glide.with(context).
                load(mediaUrl).
                into(imageViewHolder.imageView);
        imageViewHolder.imageView.setImageResource(R.drawable.nice_view);
    }

    // ViewHolder for Image media
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
        }
    }

}
