package com.example.momentshare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.NetworkChangeReceiver;
import com.example.momentshare.Classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class MainActivity extends AppCompatActivity implements Model.IModelUpdate{
    private Model model;
    private Dialog dialog;
    private NetworkChangeReceiver networkReceiver = new NetworkChangeReceiver();

    private BottomNavigationView bottomNavigationView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set default fragment (HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(new EventsFragment());
        }


        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_events){
                loadFragment(new EventsFragment());
            } else if (item.getItemId() == R.id.nav_create) {
                loadFragment(new CreateEventFragment());
            }
            else if (item.getItemId() == R.id.nav_join){
                loadFragment(new JoinEventFragment());
            }
            else{
                loadFragment(new ProfileFragment());
            }
            return true;
        });

        model = Model.getInstance();
        model.registerModelUpdate(this);

        if (model.IsUserLoggedIn()) {
            model.loadData();
        }
        else{
            showLogInDialog();
        }

        createNotificationChannel();
    }


    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            // Replace current fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);

        if (!networkReceiver.isNetworkConnected(MainActivity.this)) {
            showNoInternetDialog();
            return;
        }

        NetworkChangeReceiver.setNetworkChangeCallback(new NetworkChangeReceiver.NetworkChangeCallback() {
            @Override
            public void onNetworkLost() {
                runOnUiThread(() -> {
                    loadFragment(new EventsFragment());
                    showNoInternetDialog();
                });
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

    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    public void showLogInDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_register_login);

        dialog.setTitle("Register / Log in");
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }


        Spinner loginLogoffSpinner = dialog.findViewById(R.id.spinner_loginLogoff);
        EditText etEmail = dialog.findViewById(R.id.et_email);
        EditText etPassword = dialog.findViewById(R.id.et_password);
        EditText etUsername = dialog.findViewById(R.id.et_username);
        Button btnLogin = dialog.findViewById(R.id.btn_login);
        TextInputLayout tilUsername = dialog.findViewById(R.id.til_username);

        final boolean[] isRegistering = {true};

        String[] loginRegisterOptions = {"Register", "Login"};

        // Create an adapter using the array of options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loginRegisterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        loginLogoffSpinner.setAdapter(adapter);

        loginLogoffSpinner.setSelection(0);  // This sets "Register" as the default value

        loginLogoffSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    tilUsername.setVisibility(View.VISIBLE);
                    btnLogin.setText("Register");
                    isRegistering[0] = true;
                }
                else if (position == 1){
                    tilUsername.setVisibility(View.GONE);
                    btnLogin.setText("Login");
                    isRegistering[0] = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            boolean isValid = true;

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Valid email required");
                isValid = false;
            } else {
                etEmail.setError(null);
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password required");
                isValid = false;
            } else {
                etPassword.setError(null);
            }

            if (isRegistering[0]) {
                if (TextUtils.isEmpty(username)) {
                    etUsername.setError("Username is mandatory");
                    isValid = false;
                } else {
                    etUsername.setError(null);
                }
            }

            if (isValid) {
                if (isRegistering[0]) {
                    model.signUp(email, username, password);
                } else {
                    model.signIn(email, password);
                }
            }
        });


        dialog.show();
    }

    public void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet and try again.")
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> {
                    if (networkReceiver.isNetworkConnected(this)) {
                        dialog.dismiss();
                    } else {
                        showNoInternetDialog(); // show again
                    }
                })
                .show();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "event_channel";
            CharSequence name = "Event Reminders";
            String description = "Notifies users 30 minutes before events";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void eventDataChanged(Exception ex) {

    }

    @Override
    public void userRegistrationCompleted(Exception ex) {
        if (ex != null){
            Toast.makeText(this, "something went wrong!", Toast.LENGTH_SHORT).show();
        }
        else
            dialog.dismiss();
    }

    @Override
    public void userLoginCompleted(Exception ex) {
        if (ex != null){
            Toast.makeText(this, "something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    public void userDataLoaded(Exception ex) {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void eventDataLoaded(Exception e) {

    }

    @Override
    public void raiseMediaUploadComplete(Exception e, String url) {

    }

    @Override
    public void mediaPostChanged(Exception ex) {

    }

    @Override
    public void hostDataLoaded(Exception e, User user) {

    }

    @Override
    public void joinEventCompleted(Exception e) {

    }

    @Override
    public void createEventCompleted(Exception e, String id) {

    }

}