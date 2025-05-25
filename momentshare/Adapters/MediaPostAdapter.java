package com.example.momentshare.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.momentshare.Classes.MediaPost;
import com.example.momentshare.EventDisplayActivity;
import com.example.momentshare.R;

import java.util.ArrayList;

public class MediaPostAdapter extends RecyclerView.Adapter<MediaPostAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MediaPost> mediaPostList;

    // Constructor
    public MediaPostAdapter(Context context, ArrayList<MediaPost> mediaPostList) {
        this.context = context;
        this.mediaPostList = mediaPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_media_post, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current MediaPost item
        MediaPost currentPost = mediaPostList.get(position);

        // Bind data to views
        holder.tvUploader.setText(currentPost.getUploader().getUsername());
        holder.tvTimeSinceUpload.setText(currentPost.getTimeSinceUpload());
        String uploaderPfpUrl = currentPost.getUploader().getProfileImageUrl();
        if (uploaderPfpUrl != null && !uploaderPfpUrl.isEmpty())
            loadImageFromUrl(currentPost.getUploader().getProfileImageUrl(), holder.ivUploaderPfp);

        MediaPagerAdapter mediaPagerAdapter = new MediaPagerAdapter(context, currentPost.getMediaUrls());
        holder.vpMedia.setAdapter(mediaPagerAdapter);


        holder.tvDescription.setText(currentPost.getDescription());
        holder.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.ivMenu);
                popup.inflate(R.menu.media_post_menu);

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_delete) {
                        // Handle deletion (e.g. notify the fragment or remove from list)
                        if (context instanceof EventDisplayActivity) {
                            ((EventDisplayActivity) context).deleteMediaPost(currentPost);
                        }
                        return true;
                    }
                    return false;
                });

                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaPostList.size();
    }

    private void loadImageFromUrl(String urlString, final ImageView imageView) {

        if (urlString == null)
            Glide.with(context)
                    .load(R.drawable.person_icon)
                    .circleCrop()
                    .into(imageView);

        else {
            Glide.with(context)
                    .load(urlString)
                    .error(R.drawable.nice_view)
                    .circleCrop()
                    .into(imageView);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ViewPager2 vpMedia;
        ImageView ivUploaderPfp, ivMenu;
        TextView tvUploader, tvDescription, tvTimeSinceUpload;

        public ViewHolder(View itemView) {
            super(itemView);

            ivUploaderPfp = itemView.findViewById(R.id.iv_uploader_pfp);
            vpMedia = itemView.findViewById(R.id.vp_Media);
            tvUploader = itemView.findViewById(R.id.tv_uploader);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTimeSinceUpload = itemView.findViewById(R.id.tv_time_since_upload);
            ivMenu = itemView.findViewById(R.id.iv_menu);
        }
    }
}
