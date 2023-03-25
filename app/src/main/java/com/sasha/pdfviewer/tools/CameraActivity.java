package com.sasha.pdfviewer.tools;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraState;
import androidx.camera.core.ExposureState;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.CameraAdapter;
import com.sasha.pdfviewer.model.ImageModel;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class CameraActivity extends AppCompatActivity {

    private PreviewView camera_preview;
    private Preview preview;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private ImageCapture imageCapture;
    private ImageView cameraBtn, switchBtn;
    private int CAMERA_PERMISSION_CODE = 101;
    private RecyclerView recyclerView;
    private CameraAdapter cameraAdapter;
    private ArrayList<ImageModel> uriArrayList;
    private RelativeLayout camera_background;
    private boolean isSelected = false;
    private OrientationEventListener orientationEventListener;
    private ImageAnalysis imageAnalysis;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Camera camera;
    private CameraActivity context;
    private CircleImageView imageView;
    private TextView imageCount;
    private int count = 0;
    private int deviceOrientation = 0;
    private boolean isPortrait = false;
    private ScaleGestureDetector scaleGestureDetector;
    private float currentZoomRatio = 1.0f;
    private float maxZoomRatio;
    private CameraInfo cameraInfo;
    private CameraControl cameraControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        uriArrayList = new ArrayList<>();

        camera_preview = findViewById(R.id.camera_preview);
        cameraBtn = findViewById(R.id.camera_btn);
        recyclerView = findViewById(R.id.recyclerView);
        imageView = findViewById(R.id.imageView);
        imageCount = findViewById(R.id.image_count);
        camera_background = findViewById(R.id.camera_background);
        switchBtn = findViewById(R.id.camera_switch);


        startCamera();
        cameraPermission();
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    takePicture();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;
                }
                deviceOrientation = orientation;
                if (deviceOrientation == 90 ||
                        deviceOrientation == 180 ||
                        deviceOrientation == 270){
                    isPortrait = false;
                }
                else if (deviceOrientation == 0){
                    isPortrait = true;
                }
            }
        };
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                camera.getCameraControl().setZoomRatio(camera.getCameraInfo()
                        .getZoomState().getValue().getZoomRatio() * detector.getScaleFactor());
                return true;
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,true);
        recyclerView.setLayoutManager(layoutManager);
        cameraAdapter = new CameraAdapter(this, uriArrayList);
        recyclerView.setAdapter(cameraAdapter);

    }
    @Override
    protected void onStart() {
        super.onStart();
        orientationEventListener.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientationEventListener.disable();
    }
    public void onSwitchCameraClick(View view) {
        if (cameraProvider == null) {
            return;
        }
        // Check if the current lens facing is back or front and set the new lens facing accordingly
        @SuppressLint("RestrictedApi") int lensFacing = cameraSelector.getLensFacing();
        int newLensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ?
                CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;

        // Create a new camera selector with the new lens facing
        CameraSelector newCameraSelector = new CameraSelector.Builder()
                .requireLensFacing(newLensFacing)
                .build();

        // Unbind the old use cases and bind the new ones with the new camera selector
        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this, newCameraSelector, preview, imageCapture);
        cameraSelector = newCameraSelector;
    }

    private void cameraPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]
                    {CAMERA}, CAMERA_PERMISSION_CODE);

        }
    }

    private void startCamera(){
        camera_preview = findViewById(R.id.camera_preview);
        cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            imageCapture = new ImageCapture.Builder()
                    .setTargetRotation(getWindowManager()
                            .getDefaultDisplay().getRotation())
                    .build();

            preview = new Preview.Builder().build();
            preview.setSurfaceProvider(camera_preview.getSurfaceProvider());
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
            camera = cameraProvider.bindToLifecycle(this,
                    cameraSelector, preview,imageCapture);
            cameraControl = camera.getCameraControl();
            cameraInfo = camera.getCameraInfo();
            maxZoomRatio = cameraInfo.getZoomState().getValue().getMaxZoomRatio();
            if (currentZoomRatio < maxZoomRatio) {
                currentZoomRatio += 0.1f;
                cameraControl.setZoomRatio(currentZoomRatio);
            }

            if (currentZoomRatio > 1.0f) {
                currentZoomRatio -= 0.1f;
                cameraControl.setZoomRatio(currentZoomRatio);
            }
            switchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSwitchCameraClick(view);
                }
            });
        }, ContextCompat.getMainExecutor(this));
    }
    private void takePicture() throws IOException  {
        String name = String.valueOf(System.currentTimeMillis());
        String myFolder = "CameraPhotos";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/" + myFolder);
        String folderName =
        Environment.getExternalStorageDirectory() + "/CameraPhotos/" + name;
        File folder = new File(folderName);
        if (!folder.getParentFile().exists()) {
            folder.getParentFile().mkdir();
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(this.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                Uri picUri = outputFileResults.getSavedUri();
                String imagePath = picUri.getPath();
                String imageDate = String.valueOf(System.currentTimeMillis());
                if (picUri != null){
                    ContentResolver contentResolver = getContentResolver();
                    try {
                        Bitmap image = MediaStore.Images.Media.getBitmap(contentResolver, picUri);
                        Bitmap rotateImage = rotatedBitmap(image, deviceOrientation);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        rotateImage.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                        byte[] compressedData = outputStream.toByteArray();
                        FileOutputStream fos = null;
                        fos = new FileOutputStream(new File(folderName+".jpg"));
                        fos.write(compressedData);
                        fos.flush();
                        fos.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //camera_background.setVisibility(View.VISIBLE);
                    ImageModel imageModel = new ImageModel(picUri,imagePath, imageDate, isSelected);
                    uriArrayList.add(imageModel);
                    count = count + 1;
                    Uri imageUri = imageModel.getUri();
                    imageView.setVisibility(View.VISIBLE);
                    imageCount.setVisibility(View.VISIBLE);
                    Picasso.get().load(imageUri).into(imageView);
                    imageCount.setText(String.valueOf(count));
                    cameraAdapter.notifyDataSetChanged();
                    Log.d("Orients", String.valueOf(deviceOrientation));
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(CameraActivity.this, CameraFolderActivity.class));
                        }
                    });
                }else {
                    camera_background.setVisibility(View.GONE);
                }
            }
                    @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraActivity.this,
                        exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap rotatedBitmap(Bitmap bitmap, int orientation) {
        /*switch (orientation) {
            case 90:
                return rotateImage(bitmap, 90);
            case 180:
                return rotateImage(bitmap, 180);
            case 270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;

        }*/
        if (orientation >= 315 || orientation < 45) {
            return rotateImage(bitmap, 0);
        } else if (orientation >= 45 && orientation < 135) {
            return rotateImage(bitmap, 90);
        }
        else if (orientation >=265 || orientation < 300){
            return rotateImage(bitmap, -90);
        }
        return bitmap;
    }
    private Bitmap rotateImage(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(),
                matrix, true);
    }

}