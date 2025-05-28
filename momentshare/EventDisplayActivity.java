package com.example.momentshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.MediaPost;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.NetworkChangeReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;

public class EventDisplayActivity extends AppCompatActivity {
    private Model model;
    private Event event;
    private String id;
    private NetworkChangeReceiver networkReceiver = new NetworkChangeReceiver();

    TextView tvTime, tvPermission, tvName;
    LinearLayout llEventHeader;
    ImageView ivEventPicture;
    BottomNavigationView bottomNavigationView;
    FrameLayout flFragmentContainer;

    private final ActivityResultLauncher<String> notificationsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission()
            , new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean granted) {
                    if (granted) {

                    } else {

                    }
                }
            });


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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        // Check for SCHEDULE_EXACT_ALARM permission on Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Guide user to enable it
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent); // or show a dialog first
            }
        }

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

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);

        if (!networkReceiver.isNetworkConnected(this)) {
            finish();
            return;
        }

        NetworkChangeReceiver.setNetworkChangeCallback(new NetworkChangeReceiver.NetworkChangeCallback() {
            @Override
            public void onNetworkLost() {
                finish();
            }

            @Override
            public void onNetworkAvailable() {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
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
        model.deleteMediaPost(event, mp);
    }
}
