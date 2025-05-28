package com.example.momentshare.Classes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Model {
    private static final String TAG = "Model";
    // Interface for notifying data changes
    public interface IModelUpdate {
        void eventDataChanged(Exception ex);
        void userRegistrationCompleted(Exception ex);
        void userLoginCompleted(Exception ex);
        void userDataLoaded(Exception ex);
        void eventDataLoaded(Exception e);
        void raiseMediaUploadComplete(Exception e, String url);
        void mediaPostChanged(Exception ex);
        void hostDataLoaded(Exception e, User user);
        void joinEventCompleted(Exception e);
        void createEventCompleted(Exception e, String id);
    }

    private final ArrayList<IModelUpdate> modelUpdates = new ArrayList<>();

    // Register listener for model updates
    public void registerModelUpdate(IModelUpdate modelUpdate) {
        modelUpdates.add(modelUpdate);
    }

    // Raise event data change event
    private void raiseEventDataChanged(Exception ex) {
        for (IModelUpdate modelUpdate : modelUpdates) {
            modelUpdate.eventDataChanged(ex);
        }
    }
    private void raiseRegistrationCompleted(Exception ex){
        for (IModelUpdate modelUpdate : modelUpdates) {
            modelUpdate.userRegistrationCompleted(ex);
        }
    }
    private void raiseLoginCompleted(Exception e) {
        for (IModelUpdate iModelUpdate : modelUpdates) {
            iModelUpdate.userLoginCompleted(e);
        }
    }
    public void raiseUserDataLoaded(Exception ex){
        for (IModelUpdate modelUpdate : modelUpdates) {
            modelUpdate.userDataLoaded(ex);
        }
    }
    public void raiseEventDataLoaded(Exception e){
        for (IModelUpdate modelUpdate : modelUpdates) {
            modelUpdate.eventDataLoaded(e);
        }
    }
    public void raiseMediaUploadComplete(Exception e, String url){
        for (IModelUpdate modelUpdate : modelUpdates){
            modelUpdate.raiseMediaUploadComplete(e, url);
        }
    }
    private void raiseMediaPostChanged(Exception e){
        for (IModelUpdate modelUpdate : modelUpdates){
            modelUpdate.mediaPostChanged(e);
        }
    }
    private void raiseHostDataLoaded(Exception e, User user){
        for (IModelUpdate modelUpdate : modelUpdates){
            modelUpdate.hostDataLoaded(e, user);
        }
    }
    private void raiseJoinEvent(Exception e){
        for (IModelUpdate modelUpdate : modelUpdates){
            modelUpdate.joinEventCompleted(e);
        }
    }
    private void raiseCreateEventCompleted(Exception e, String id){
        for (IModelUpdate modelUpdate : modelUpdates){
            modelUpdate.createEventCompleted(e, id);
        }
    }

    private static Model model;
    private User currentUser;
    private ArrayList<Event> events = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage fbStorage;
    private CollectionReference eventsRef;

    private Model() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fbStorage = FirebaseStorage.getInstance();
        eventsRef = db.collection("events");
    }

    public static Model getInstance() {
        if (model == null) model = new Model();
        return model;
    }


    public User getCurrentUser() {
        return currentUser;
    }
    public boolean IsUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }


    public Event getEvent(String id) {
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }

    public void signIn(String email, String password) {
        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Once signed in, retrieve user data from Firestore
                        mAuth = FirebaseAuth.getInstance();
                        db = FirebaseFirestore.getInstance();
                        fbStorage = FirebaseStorage.getInstance();

                        raiseLoginCompleted(null);
                        loadData();
                    } else {
                        // Handle sign-in failure
                        Log.e("SignIn", "Sign-in failed", task.getException());
                        raiseLoginCompleted(task.getException());
                    }
                });
    }

    public void loadMediaPostsForEvent(Event event){
        eventsRef.document(event.getId()).collection("Posts").orderBy("uploadTime").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentChange  documentChange : queryDocumentSnapshots.getDocumentChanges()){
                            MediaPost mediaPost = documentChange.getDocument().toObject(MediaPost.class);
                            event.getMediaPosts().add(mediaPost);
                        }
                        raiseMediaPostChanged(null);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        raiseMediaPostChanged(e);
                    }
                });
    }


    private void loadEventsFromFB(){
        events.clear(); //to ensure no list duplication

        //find events where the user is a participant
        eventsRef.whereArrayContains("participantIds", currentUser.getId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //load events
                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                            Event event = documentChange.getDocument().toObject(Event.class);
                            event.setId(documentChange.getDocument().getId());
                            events.add(event);
                        }
                        raiseEventDataLoaded(null);
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        raiseEventDataLoaded(e);
                    }
                });
    }
    public void loadData() {
        if (mAuth.getCurrentUser() == null)
            return;
        String id = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(id);

        // Fetch the user document
        userRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentUser = document.toObject(User.class);

                            raiseUserDataLoaded(null);
                            loadEventsFromFB();
                            if (events == null)
                                events = new ArrayList<>();
                        } else {
                            // Document doesn't exist
                            raiseUserDataLoaded(task.getException());
                            Log.e("FetchUserData", "No such document for UID: " + id);
                        }
                    } else {
                        raiseUserDataLoaded(task.getException());
                        Log.e("FetchUserData", "Error fetching user data", task.getException());
                    }
                });
    }

    public void signUp(String email, String username, String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Step 2: If registration is successful, get the user's UID
                        String id = mAuth.getUid();
                        currentUser = new User(id, email, username);


                        // Step 4: Save the user data to Firestore in the "users" collection
                        DocumentReference userRef = db.collection("users").document(id);
                        userRef.set(currentUser)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        mAuth = FirebaseAuth.getInstance();
                                        db = FirebaseFirestore.getInstance();
                                        fbStorage = FirebaseStorage.getInstance();

                                        raiseRegistrationCompleted(null);
                                        Log.d("Model", "User document created successfully.");

                                    } else {
                                        // Handle Firestore errors
                                        raiseRegistrationCompleted(task1.getException());
                                        Log.e("Model", "Error creating user document", task1.getException());

                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle Firestore set errors
                                    raiseRegistrationCompleted(e);
                                    Log.e("Model", "Error saving user data to Firestore", e);
                                });
                    } else {
                        // Handle sign-up errors
                        raiseRegistrationCompleted(task.getException());
                        Log.e("Model", "Sign-up failed", task.getException());
                    }
                });
    }

    public void signOut(){
        mAuth.signOut();
        currentUser = null;
        events.clear();
    }

    public void joinEvent(String eventId, Context context) {

        // Reference the specific event document by its ID
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot eventDocument = task.getResult();
                        Event event = eventDocument.toObject(Event.class);

                        if (event == null) {
                            Log.e("JoinEvent", "Event document could not be parsed.");
                            raiseJoinEvent(new Exception("Failed to parse event data"));
                            return;
                        }

                        ArrayList<String> participantIds = event.getParticipantIds();
                        if (participantIds == null) {
                            participantIds = new ArrayList<>();
                        }

                        if (participantIds.contains(currentUser.getId())) {
                            Log.w("JoinEvent", "User is already a member of this event");
                            raiseJoinEvent(new Exception("User already joined"));
                            return;
                        }

                        // Add current user to the participant list
                        participantIds.add(currentUser.getId());
                        event.setParticipantIds(participantIds); // ensure event has updated list

                        // Avoid adding duplicate events to local list
                        if (getEvent(event.getId()) == null) {
                            events.add(event);
                        }

                        eventRef.update("participantIds", participantIds)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("JoinEvent", "User added to participants");
                                    raiseJoinEvent(null);
                                    scheduleReminderForEvent(context, event);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("JoinEvent", "Failed to update participants", e);
                                    raiseJoinEvent(e);
                                });
                    } else {
                        Exception error = task.getException() != null ? task.getException() : new Exception("Event not found or unknown error");
                        Log.e("JoinEvent", "Failed to join event", error);
                        raiseJoinEvent(error);
                    }
                });

    }

    public void createEvent(Event event, Bitmap bitmap, Context context) {
        DocumentReference newEventRef = eventsRef.document();
        String eventId = newEventRef.getId();

        // Set the event ID in the event object (if applicable)
        event.setId(eventId);

        if (bitmap == null){

            newEventRef.set(event)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Event with image saved successfully");
                        events.add(event);
                        raiseCreateEventCompleted(null, eventId);
                        scheduleReminderForEvent(context, event);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving event with image", e);
                        raiseCreateEventCompleted(e, null);
                    });
            return;
        }

        File file = bitmapToFile(bitmap);
        StorageReference storageRef = fbStorage.getReference().child("event_images/" + event.getId() + ".png");
        Uri fileUri = Uri.fromFile(file);

        // Upload file
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        event.setEventImageUrl(downloadUrl);  // Assuming Event class has setImageUrl()

                        // Save the event to Firestore
                        event.setId(newEventRef.getId());

                        newEventRef.set(event)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Event with image saved successfully");
                                    events.add(event);
                                    raiseCreateEventCompleted(null, eventId);
                                    scheduleReminderForEvent(context, event);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error saving event with image", e);
                                    raiseCreateEventCompleted(e, null);
                                });
                    });
                })
                .addOnFailureListener(e ->
                        Log.e("CreateEvent", "Failed to upload event image", e));

    }

    private void scheduleReminderForEvent(Context context, Event event) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Check if permission exists (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager == null || !alarmManager.canScheduleExactAlarms()) {
                Log.w("Reminder", "Exact alarm not permitted. Skipping reminder.");
                return;
            }
        }

        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra("eventTitle", event.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                event.getId().hashCode(), // unique ID for each event
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long reminderTimeMillis = event.getDateScheduled().getTime() - 30 * 60 * 1000; // 30 mins before

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
            );
            Log.d("Reminder", "Alarm scheduled for: " + new Date(reminderTimeMillis).toString());
        } else {
            Log.e("Reminder", "AlarmManager is null, cannot schedule reminder");
        }
    }

    public void addPostToEvent(String eventId, MediaPost mediaPost) {
        DocumentReference eventRef = eventsRef.document(eventId);


        CollectionReference postsRef = eventRef.collection("Posts");

        postsRef.add(mediaPost)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    mediaPost.setId(generatedId);

                    documentReference.update("id", generatedId)
                            .addOnSuccessListener(unused -> Log.d("Firestore", "MediaPost ID updated successfully"))
                            .addOnFailureListener(e -> Log.w("Firestore", "Failed to update MediaPost ID", e));

                    raiseMediaPostChanged(null);
                    Log.d("Firestore", "Post added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    raiseMediaPostChanged(e);
                    Log.w("Firestore", "Error adding post", e);
                });
    }

    public void deleteMediaPost(Event event, MediaPost mediaPost) {
        String postId = mediaPost.getId();
        DocumentReference postRef = db.collection("events")
                .document(event.getId())
                .collection("Posts")
                .document(postId);

        postRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post deleted successfully");
                    event.getMediaPosts().remove(mediaPost);
                    raiseMediaPostChanged(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete post", e);
                    raiseMediaPostChanged(e);
                });
    }

    public void setUsername(String username) {
        if (currentUser == null || username == null || username.trim().isEmpty()) {
            Log.e(TAG, "setUserName: currentUser is null or username is empty");
            return;
        }

        currentUser.setUsername(username); // Update local object

        DocumentReference userRef = db.collection("users").document(currentUser.getId());
        userRef.update("username", username)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Username updated successfully in Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update username in Firestore", e));
    }
    public void uploadBitmap(Bitmap bitmap) {

        // Convert Bitmap to File
        File file = bitmapToFile(bitmap);
        if (file == null) {
            return;
        }

        // Get Firebase Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + file.getName());

        Uri fileUri = Uri.fromFile(file);
        UploadTask uploadTask = storageRef.putFile(fileUri);

        // Monitor the upload task
        uploadTask.addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d("Upload", "Download URL: " + uri.toString());
                    raiseMediaUploadComplete(null, downloadUrl);
                }).addOnFailureListener(e -> Log.e("Upload", "Failed to get download URL", e))
        ).addOnFailureListener(e -> Log.e("Upload", "Upload failed", e));

    }
    public void loadHost(String userId) {
        if (userId == null || userId.isEmpty()) {
            raiseUserDataLoaded(new Exception("User ID is null or empty"));
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            raiseHostDataLoaded(null, user);
                        } else {
                            raiseHostDataLoaded(new Exception("Failed to convert document to User"), null);
                        }
                    } else {
                        raiseHostDataLoaded(new Exception("User not found"), null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user", e);
                    raiseHostDataLoaded(e, null);
                });
    }


    public void setUserProfileImage(Bitmap profileImageBitmap) {

        String currentProfileImageUrl = currentUser.getProfileImageUrl();
        if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
            // Extract the filename or reference path from the current URL
            deleteCurrentProfileImage(); // Delete the current image from Firebase Storage
        }

        // Convert the Bitmap to a File
        File profileImageFile = bitmapToFile(profileImageBitmap);

        // Get a reference to Firebase Storage
        StorageReference storageRef = fbStorage.getReference();
        // Create a path where the image will be stored, e.g., profile_pictures/<userId>/profile.png
        StorageReference profilePicRef = storageRef.child("profile_images/" + mAuth.getUid() + "/profile.jpg");

        // Upload the file to Firebase Storage
        profilePicRef.putFile(Uri.fromFile(profileImageFile))
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();

                        // Set the profile image URL in the currentUser object
                        currentUser.setProfileImageUrl(profileImageUrl);

                        // Update the profile image URL for the user in Firestore
                        DocumentReference userRef = db.collection("users").document(currentUser.getId());
                        userRef.update("profileImageUrl", profileImageUrl)
                                .addOnSuccessListener(aVoid -> Log.d("ProfilePicture", "Profile image URL updated in Firestore"))
                                .addOnFailureListener(e -> Log.e("ProfilePicture", "Error updating profile image URL in Firestore", e));
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle the error if the upload fails
                    Log.e("ProfilePicture", "Error uploading profile picture", e);
                });
    }

    private void deleteCurrentProfileImage() {
        // Get the current profile image reference in Firebase Storage
        String currentProfileImageUrl = currentUser.getProfileImageUrl();

        if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
            StorageReference storageRef = fbStorage.getReferenceFromUrl(currentProfileImageUrl);
            // Delete the file from Firebase Storage
            storageRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d("ProfilePicture", "Current profile picture deleted successfully"))
                    .addOnFailureListener(e -> Log.e("ProfilePicture", "Error deleting current profile picture", e));
        }
    }

    private File bitmapToFile(Bitmap bitmap) {
        String uniqueFileName = "image_" + System.currentTimeMillis() + ".png";
        File file = new File(System.getProperty("java.io.tmpdir"), uniqueFileName);

        try {
            // Create an OutputStream to write the Bitmap to the file
            FileOutputStream fos = new FileOutputStream(file);

            // Compress the Bitmap and write it to the output stream as PNG
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            // Close the OutputStream
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the file object
        return file;
    }


}

