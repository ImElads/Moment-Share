package com.example.momentshare;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventDetailsFragment extends Fragment implements Model.IModelUpdate {
    Model model = Model.getInstance();
    Event event;
    String eventId;


    EditText etHost, etDate, etLocation, etDescription;
    ImageView ivQR;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventDetailsFragment newInstance(String param1, String param2) {
        EventDetailsFragment fragment = new EventDetailsFragment();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model.registerModelUpdate(this);

        etHost = view.findViewById(R.id.et_host);
        etLocation = view.findViewById(R.id.et_location);
        etDate = view.findViewById(R.id.et_date);
        ivQR = view.findViewById(R.id.iv_QR);
        etDescription = view.findViewById(R.id.et_description);

        eventId = "";
        if (getActivity() instanceof EventDisplayActivity) {
            eventId = ((EventDisplayActivity) getActivity()).getEventId();
        }
        event = model.getEvent(eventId);
        if (event == null) {
            Toast.makeText(getContext(), "Event not found - "+eventId, Toast.LENGTH_SHORT).show();
            return;
        }

        ivQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Event Code", eventId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "event code copied", Toast.LENGTH_SHORT).show();
            }
        });

        etDate.setText(event.getDateScheduled().toString());
        model.loadHost(event.getHostId());
        etLocation.setText(event.getLocation());
        etDescription.setText(event.getDescription());

        generateAndDisplayQRCode(eventId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    private void generateAndDisplayQRCode(String eventId) {
        try {
            // Use ZXing to generate QR code
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 200, 200);

            // Convert BitMatrix to Bitmap
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Set the generated QR code as the image source for ivQR
            ivQR.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            ivQR.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
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
        if (e != null)
            return;
        etHost.setText(user.getUsername());
    }

    @Override
    public void joinEventCompleted(Exception e) {

    }

    @Override
    public void createEventCompleted(Exception e, String id) {

    }
}