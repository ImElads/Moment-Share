package com.example.momentshare.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.momentshare.Classes.Event;
import com.example.momentshare.EventDisplayActivity;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.R;

import java.util.ArrayList;
import java.util.Date;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final ArrayList<Event> eventList;
    private final Context context;

    // Constructor
    public EventAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    // ViewHolder class
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvPermission;
        ImageView imageView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvPermission = itemView.findViewById(R.id.tv_permission);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        Model model = Model.getInstance();

        holder.tvName.setText(event.getName());
        Date date = event.getDateScheduled();
        String formattedDate = String.format("%d/%d/%d", date.getDate(), date.getMonth() + 1, date.getYear() + 1900);
        holder.tvTime.setText(formattedDate);
        if (event.isUserHost(model.getCurrentUser()))
            holder.tvPermission.setText("Host");
        else
            holder.tvPermission.setText("Participant");

        if (event.getEventImageUrl() == null)
            Glide.with(context)
                    .load(R.drawable.nice_view)
                    .circleCrop()
                    .into(holder.imageView);

        else {
            Glide.with(context)
                    .load(event.getEventImageUrl())
                    .error(R.drawable.nice_view)
                    .circleCrop()
                    .into(holder.imageView);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDisplayActivity.class);
            intent.putExtra("id", event.getId());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
