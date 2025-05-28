package com.example.momentshare;

import android.Manifest;
import android.app.Dialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.momentshare.Adapters.MediaPostAdapter;
import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.MediaPost;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class EventSharedAlbumFragment extends Fragment implements Model.IModelUpdate {
    Model model = Model.getInstance();
    Event event;
    Dialog dialog;

    MediaPostAdapter mediaPostsAdapter;
    MediaPost tempMediaPost = new MediaPost();
    int mediaAmountLeftToUpload;

    ArrayList<String> mediaLinks = new ArrayList<>();
    ArrayList<Bitmap> selectedBitmaps = new ArrayList<>();

    Button btnNewPost;
    BottomSheetDialog bsUploadMedia;
    RecyclerView mediaPostsList;

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
                    bsUploadMedia.dismiss();
                    selectedBitmaps.add(bitmap);

                }
            });

    private final ActivityResultLauncher<String> readGalleryImagesPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean granted) {
                    if (granted) {
                        openGallery();
                    } else {
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> readGalleryImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            new ActivityResultCallback<List<Uri>>() {
                @Override
                public void onActivityResult(List<Uri> uris) {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            // Handle the selected images
                            String mimeType = getContext().getContentResolver().getType(uri);
                            if (mimeType != null && mimeType.startsWith("image/")) {
                                Bitmap bitmap = loadImageFromUri(uri);
                                if (bitmap != null) {
                                    selectedBitmaps.add(bitmap);
                                }
                            }
                        }
                    }
                }
            }
    );






    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EventSharedAlbumFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventSharedAlbumFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventSharedAlbumFragment newInstance(String param1, String param2) {
        EventSharedAlbumFragment fragment = new EventSharedAlbumFragment();
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
        return inflater.inflate(R.layout.fragment_event_shared_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model.registerModelUpdate(this);
        btnNewPost = view.findViewById(R.id.btn_newPost);
        mediaPostsList = view.findViewById(R.id.rcv_posts);



        String eventId = "";
        if (getActivity() instanceof EventDisplayActivity) {
            eventId = ((EventDisplayActivity) getActivity()).getEventId();
        }
        event = model.getEvent(eventId);
        if (event == null) {
            Toast.makeText(getContext(), "Event not found - "+eventId, Toast.LENGTH_SHORT).show();
            return;
        }
        model.loadMediaPostsForEvent(event);

        mediaPostsList.setLayoutManager(new LinearLayoutManager(getContext()));

        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreatePostDialog();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaPostsList.getAdapter() != null)
            mediaPostsList.getAdapter().notifyDataSetChanged();
    }


    private void showCreatePostDialog(){
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_create_post);

        dialog.setTitle("Create Event");
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }

        ImageButton ibAddMedia = dialog.findViewById(R.id.ib_addMedia);
        Button btn_CreatePost = dialog.findViewById(R.id.btn_createPost);
        EditText etDescription = dialog.findViewById(R.id.et_description);

        ibAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedBitmaps = new ArrayList<>();
                bsUploadMedia = new BottomSheetDialog(getContext());
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View inflatedView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_media_upload, null);
                bsUploadMedia.setContentView(inflatedView);

                bsUploadMedia.show();

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

        btn_CreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedBitmaps.isEmpty()){
                    Toast.makeText(getContext(), "You need to choose pictures", Toast.LENGTH_SHORT).show();
                    return;
                }
                tempMediaPost.setDescription(etDescription.getText().toString());

                dialog.dismiss();
                uploadMediaAndCreatePost();
            }
        });

        dialog.show();
    }

    private void uploadMediaAndCreatePost() {
        mediaAmountLeftToUpload = selectedBitmaps.size();

        // Assuming we have an array of Bitmaps or URIs to upload
        for (Bitmap bitmap : selectedBitmaps) {
            model.uploadBitmap(bitmap);
        }

        //when done the post is created
    }
    private void createPost(){
        // After upload, set the media URLs in the MediaPost object
        tempMediaPost.setMediaUrls(mediaLinks);
        selectedBitmaps = new ArrayList<>();
        mediaLinks = new ArrayList<>();

        String eventId = event.getId();
        model.addPostToEvent(eventId, tempMediaPost);
        event.getMediaPosts().add(tempMediaPost);
        mediaPostsList.getAdapter().notifyDataSetChanged();
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
            readGalleryImagesLauncher.launch(new String[]{"image/*"});
        }
        else{
            readGalleryImagesPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    private Bitmap loadImageFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
        mediaLinks.add(url);
        mediaAmountLeftToUpload --;
        if (mediaAmountLeftToUpload <= 0)
            createPost();
    }

    @Override
    public void mediaPostChanged(Exception ex) {
        if (ex != null){
            return;
        }
        if (dialog != null)
            dialog.dismiss();
        if (mediaPostsAdapter == null){
            mediaPostsAdapter = new MediaPostAdapter(getContext(), event.getMediaPosts());
        }
        mediaPostsList.setAdapter(mediaPostsAdapter);
        mediaPostsList.getAdapter().notifyDataSetChanged();

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