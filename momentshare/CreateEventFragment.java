package com.example.momentshare;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateEventFragment extends Fragment implements Model.IModelUpdate {
    private final Model model = Model.getInstance();
    private final Calendar calendar = Calendar.getInstance();
    private Bitmap eventBitmap;

    private Button btnCreateEvent, btnCloseDialog;
    private EditText etTitle, etDescription, etDate, etTime, etLocation;
    private ImageView ivAddEventPic;
    private BottomSheetDialog bsProfilePicDialog;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission()
            , new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean granted) {
                    openCamera();
                }
            });

    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            new ActivityResultCallback<Bitmap>() {
                @Override
                public void onActivityResult(Bitmap bitmap) {
                    if (bitmap == null) {
                        return;
                    }
                    bsProfilePicDialog.dismiss();

                    eventBitmap = bitmap;
                    ivAddEventPic.setImageBitmap(bitmap);

                }
            });

    private final ActivityResultLauncher<String> readGalleryImagesPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission()
            , new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean granted) {
                    if (granted) {
                        openGallery();
                    } else {
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private ActivityResultLauncher<String> readGalleryImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        try {
                            // Convert the URI to a Bitmap
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

                            bsProfilePicDialog.dismiss();

                            eventBitmap = bitmap;
                            ivAddEventPic.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });



    public CreateEventFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CreateEventFragment newInstance(String param1, String param2) {
        CreateEventFragment fragment = new CreateEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        btnCreateEvent = view.findViewById(R.id.btn_create_event);
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etDate = view.findViewById(R.id.et_date);
        ivAddEventPic = view.findViewById(R.id.iv_addEventPic);
        etLocation = view.findViewById(R.id.et_location);

        model.registerModelUpdate(this);

        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Event event = createEventInstance(view);
                if (event == null)
                    return;
                model.createEvent(event, eventBitmap, getContext());

                clearViews();
            }
        });



        etTime = view.findViewById(R.id.et_time);

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        (view1, year1, month1, dayOfMonth) -> {
                            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                            etDate.setText(formattedDate);
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        (view12, hourOfDay, minute1) -> {
                            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                            etTime.setText(formattedTime);
                        },
                        hour, minute, true
                );
                timePickerDialog.show();
            }
        });

        ivAddEventPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsProfilePicDialog = new BottomSheetDialog(getContext());
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View inflatedView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_media_upload, null);
                bsProfilePicDialog.setContentView(inflatedView);

                bsProfilePicDialog.show();

                ImageButton ibCamera = inflatedView.findViewById(R.id.ib_camera);
                ImageButton ibGallery = inflatedView.findViewById(R.id.ib_gallery);

                ibCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openCamera();
                    }
                });
                ibGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openGallery();
                    }
                });
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_event, container, false);

    }

    private void clearViews(){
        ivAddEventPic.setImageResource(R.drawable.add_circle);
        etDate.setText("Click to enter a date");
        etTime.setText("Click to enter a time");
        etTitle.setText("title");
        etDescription.setText("desc");
        etLocation.setText("location not set");
    }

    private Event createEventInstance(View view) {
        String title = etTitle.getText().toString().trim();
        if (title.trim().length() < 5) {
            Toast.makeText(view.getContext(), "Title must be at least 5 characters", Toast.LENGTH_SHORT).show();
            return null;
        }

        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString();
        String dateString = etDate.getText().toString().trim();
        String timeString = etTime.getText().toString().trim();

        if (dateString.isEmpty() || timeString.isEmpty()) {
            Toast.makeText(view.getContext(), "Please enter both date and time", Toast.LENGTH_SHORT).show();
            return null;
        }

        String dateTimeString = dateString + " " + timeString;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setLenient(false); // Strict parsing

        try {
            Date dateTime = sdf.parse(dateTimeString);

            // Check if date is in the past
            if (dateTime.before(new Date())) {
                Toast.makeText(view.getContext(), "Date and time must be in the future", Toast.LENGTH_SHORT).show();
                return null;
            }

            return new Event(title, description, location, dateTime);

        } catch (Exception e) {
            Toast.makeText(view.getContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private boolean isCameraPermissionGranted(){
        return ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean isReadMediaImagePermissionGranted(){
        return ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }


    private void openCamera() {
        if (isCameraPermissionGranted())
            cameraLauncher.launch(null);
            //send to settings
        else
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
    }
    private void openGallery() {
        if (isReadMediaImagePermissionGranted()){
            readGalleryImageLauncher.launch("image/*");
            //send to setting
        }
        else{
            readGalleryImagesPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    @Override
    public void eventDataChanged(Exception ex) {

    }

    @Override
    public void userRegistrationCompleted(Exception ex) {

    }

    @Override
    public void userLoginCompleted(Exception ex) {

    }

    @Override
    public void userDataLoaded(Exception ex) {

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
        if (e != null){
            Toast.makeText(getContext(), "something failed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), EventDisplayActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }
}