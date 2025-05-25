package com.example.momentshare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;

public class ProfileFragment extends Fragment implements Model.IModelUpdate {
    Model model = Model.getInstance();
    private Context context;

    private ImageView ivProfilePic, ivEditUsername, ivEditPhoneNumber;
    private EditText etUsername, etEmail;
    private Button btnSignOut;
    private BottomSheetDialog bsProfilePicDialog;

    private String usernameAtStart;

    private final int CAMERA_REQUEST_CODE = 100;
    private final int GALLERY_REQUEST_CODE = 99;


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
                    // Set the captured image to the ImageView
                    model.setUserProfileImage(bitmap);
                    Glide.with(context)
                            .load(bitmap)
                            .circleCrop()
                            .into(ivProfilePic);

                }
            });

    private final ActivityResultLauncher<String> readGalleryImagesPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission()
            , new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean granted) {
                    if (granted) {
                        // Open the gallery if permission is granted
                        openGallery();
                    } else {
                        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
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
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

                            bsProfilePicDialog.dismiss();

                            model.setUserProfileImage(bitmap);
                            Glide.with(context)
                                    .load(bitmap)
                                    .circleCrop()
                                    .into(ivProfilePic);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        etEmail = view.findViewById(R.id.et_email);
        etUsername = view.findViewById(R.id.et_username);
        ivProfilePic = view.findViewById(R.id.ib_profilePic);;
        btnSignOut = view.findViewById(R.id.btn_signOut);

        setUpLayoutViewValues();

        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pfpView) {
                bsProfilePicDialog = new BottomSheetDialog(context);
                LayoutInflater layoutInflater = LayoutInflater.from(context);
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

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.signOut();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showLogInDialog();
                }
            }
        });
    }

    private void setUpLayoutViewValues() {
        User user = model.getCurrentUser();

        etUsername.setText(user.getUsername());
        etEmail.setText(user.getEmail());

        if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()){
            Glide.with(context)
                    .load(R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(ivProfilePic);
            return;
        }


        Glide.with(context)
                .load(user.getProfileImageUrl())
                .circleCrop()
                .into(ivProfilePic);

    }

    @Override
    public void onStart() {
        super.onStart();
        usernameAtStart = model.getCurrentUser().getUsername();
        etUsername.setText(usernameAtStart);
    }

    @Override
    public void onPause() {
        super.onPause();
        String uname = etUsername.getText().toString();
        if (!uname.equals(usernameAtStart)){
            model.setUsername(uname);
            model.getCurrentUser().setUsername(uname);
        }
    }

    private boolean isCameraPermissionGranted(){
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean isReadMediaImagePermissionGranted(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }


    private void openCamera() {
        if (isCameraPermissionGranted())
            cameraLauncher.launch(null);
            //send to settings
        else
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
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
        setUpLayoutViewValues();
    }

    @Override
    public void userLoginCompleted(Exception ex) {

    }

    @Override
    public void userDataLoaded(Exception ex) {
        setUpLayoutViewValues();
    }

    @Override
    public void eventDataLoaded(Exception e) {

    }

    @Override
    public void raiseMediaUploadComplete(Exception e, String url) {

    }

    @Override
    public void mediaPostChanged(Exception ex, Event event) {

    }

    @Override
    public void hostDataLoaded(Exception e, User user) {

    }

    @Override
    public void joinEventCompleted(Exception e) {

    }

    @Override
    public void createEventCompleted(Exception e) {

    }
}