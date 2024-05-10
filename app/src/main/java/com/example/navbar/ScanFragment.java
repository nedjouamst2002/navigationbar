package com.example.navbar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class ScanFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_REQUEST_CODE = 1002;
    private static final int GALLERY_REQUEST_CODE = 1003;

    private ImageView mPhotoImageView;
    private Button mCameraButton;
    private Button mGalleryButton;
    private Button mDetectButton;
    private TextView mResultTextView;
    private Bitmap mBitmap;

    private Classifier mClassifier;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        mPhotoImageView = view.findViewById(R.id.mPhotoImageView);
        mCameraButton = view.findViewById(R.id.mCameraButton);
        mGalleryButton = view.findViewById(R.id.mGalleryButton);
        mDetectButton = view.findViewById(R.id.mDetectButton);
        mResultTextView = view.findViewById(R.id.mResultTextView);

        mClassifier = new Classifier(getActivity().getAssets(), "model.tflite", "labels.txt", 224);

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    openCamera();
                }
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectImage();
            }
        });

        return view;
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void detectImage() {
        if (mBitmap != null) {
            Classifier.Recognition category = mClassifier.recognizeImage(mBitmap).get(0);
            mResultTextView.setText(category.getTitle() + "\nConfidence: " + category.getConfidence());
        } else {
            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (data != null && data.getExtras() != null) {
                    mBitmap = (Bitmap) data.getExtras().get("data");
                    mPhotoImageView.setImageBitmap(mBitmap);
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null && data.getData() != null) {
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        mPhotoImageView.setImageBitmap(mBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
