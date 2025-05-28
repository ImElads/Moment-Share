package com.example.momentshare;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.momentshare.Classes.Event;
import com.example.momentshare.Classes.Model;
import com.example.momentshare.Classes.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class JoinEventFragment extends Fragment implements Model.IModelUpdate {
    Model model = Model.getInstance();
    Button btnQR;
    TextInputLayout tilEventCode;
    EditText etEventCode;


    private ActivityResultLauncher<Intent> qrScanLauncher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public JoinEventFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static JoinEventFragment newInstance(String param1, String param2) {
        JoinEventFragment fragment = new JoinEventFragment();
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

        model.registerModelUpdate(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_event, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        btnQR = view.findViewById(R.id.btn_QR);
        tilEventCode = view.findViewById(R.id.til_eventCode);
        etEventCode = view.findViewById(R.id.et_eventCode);

        btnQR.setOnClickListener(v -> {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this); // Use the fragment version
            integrator.setPrompt("Scan a QR Code");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setOrientationLocked(true);
            integrator.setBarcodeImageEnabled(false);
            qrScanLauncher.launch(integrator.createScanIntent());

        });

        etEventCode.setOnClickListener(v -> {
            // Retrieve clipboard content (if any)
            String clipboardData = getClipboardData();
            if (!clipboardData.isEmpty()) {
                if (clipboardData.trim().isEmpty() || clipboardData.contains("/")) {
                    Toast.makeText(getContext(), "Invalid event ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                etEventCode.setText(clipboardData);
                model.joinEvent(clipboardData, getContext());
            } else {
                Toast.makeText(getActivity(), "No code found in clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        qrScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                    if (scanResult.getContents() != null) {
                        String scannedCode = scanResult.getContents();
                        etEventCode.setText(scannedCode);
                        model.joinEvent(scannedCode, getContext());
                    }
                }
        );
    }

    private String getClipboardData() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getContext().CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            return clip.getItemAt(0).getText().toString();
        }
        return "";
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
        etEventCode.setText("Click to paste code");
        if (e != null){
            Toast.makeText(getContext(), "failed to join event", Toast.LENGTH_SHORT).show();
            return;
        }


        Intent intent = new Intent(getActivity(), EventDisplayActivity.class);
        intent.putExtra("id", etEventCode.getText().toString());
        startActivity(intent);
    }

    @Override
    public void createEventCompleted(Exception e, String id) {

    }
}