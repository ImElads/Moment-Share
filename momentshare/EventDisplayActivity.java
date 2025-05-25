package com.example.momentshare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.MediaPost;
import com.example.momentshare.Classes.Model;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;

public class EventDisplayActivity extends AppCompatActivity {
    private Model model;
    private Event event;
    private String id;

    TextView tvTime, tvPermission, tvName;
    LinearLayout llEventHeader;
    ImageView ivEventPicture;
    BottomNavigationView bottomNavigationView;
    FrameLayout flFragmentContainer;


    @SuppressLint({"MissingInflatedId", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_display);

        // Adjust padding for system UI (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views

        llEventHeader = findViewById(R.id.ll_event_header);
        tvTime = findViewById(R.id.tv_time);
        tvPermission = findViewById(R.id.tv_permission);
        tvName = findViewById(R.id.tv_name);
        ivEventPicture = findViewById(R.id.iv_eventPicture);
        flFragmentContainer = findViewById(R.id.fl_fragmentContainer);
        bottomNavigationView = this.findViewById(R.id.bottom_navigation_view);

        // Get model instance and event id from intent
        model = Model.getInstance();
        id = getIntent().getStringExtra("id");
        event = model.getEvent(id);

        setUpEventHeader();

        // Set up bottom navigation item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_details){
                loadFragment(new EventDetailsFragment());
                return true;
            }
            else if (item.getItemId() == R.id.nav_sharedAlbum){
                loadFragment(new EventSharedAlbumFragment());
                return true;
            }
            else
                return false;
        });


        // Default fragment load when the activity starts
        if (savedInstanceState == null) {
            loadFragment(new EventDetailsFragment());
        }

    }

    // Helper method to load a fragment dynamically into the FrameLayout
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragmentContainer, fragment)
                .commit();
    }

    private void setUpEventHeader() {
        // Format and display event date
        Date date = event.getDateScheduled();
        String formattedDate = String.format("%d/%d/%d", date.getDate(), date.getMonth() + 1, date.getYear() + 1900);
        tvTime.setText(formattedDate);

        // Set event name
        tvName.setText(event.getName());

        if (event.getEventImageUrl() == null)
            Glide.with(this)
                    .load(R.drawable.nice_view)
                    .circleCrop()
                    .into(ivEventPicture);

        else {
            Glide.with(this)
                    .load(event.getEventImageUrl())
                    .error(R.drawable.nice_view)
                    .circleCrop()
                    .into(ivEventPicture);
            // Set user permission
            if (event.isUserHost(model.getCurrentUser()))
                tvPermission.setText("Host");
            else
                tvPermission.setText("Participant");
        }
    }
    public String getEventId(){return event.getId();}
    public void deleteMediaPost(MediaPost mp){
        String mpId = mp.getId();
        model.deleteMediaPost(id, mpId);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_fragmentContainer);

        if (currentFragment instanceof EventSharedAlbumFragment) {
            ((EventSharedAlbumFragment) currentFragment).deleteMediaPost(mp);
        } else {
            // Optional: Log or toast for debugging
            Toast.makeText(this, "Shared Album fragment not active", Toast.LENGTH_SHORT).show();
        }
    }
}
