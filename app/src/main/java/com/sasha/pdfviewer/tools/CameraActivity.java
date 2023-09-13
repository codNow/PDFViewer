package com.sasha.pdfviewer.tools;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.CameraAdapter;
import com.sasha.pdfviewer.cropper.CropImage;
import com.sasha.pdfviewer.cropper.CropImageView;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.view.MainActivity;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class CameraActivity extends AppCompatActivity {

    private PreviewView camera_preview;
    private Preview preview;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private ImageCapture imageCapture;
    private ImageView cameraBtn, switchBtn, closeCamera;
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
    private TextView imageCount, cameraAi, cameraScanner;
    private int count = 0;
    private int deviceOrientation = 0;
    private boolean isPortrait = false;
    private ScaleGestureDetector scaleGestureDetector;
    private float currentZoomRatio = 1.0f;
    private float maxZoomRatio;
    private CameraInfo cameraInfo;
    private CameraControl cameraControl;
    private int mSelectedMode = 0;
    //private Mode mode = Mode.PHOTO;
    String folder =Environment.getExternalStorageDirectory()+
            "/AppImages/";
    private Uri picUri;


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
        closeCamera = findViewById(R.id.close_camera);
        cameraScanner = findViewById(R.id.camera_scanner);
        cameraAi = findViewById(R.id.camera_ai);



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

        cameraScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
        cameraAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        closeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

    @SuppressLint("ClickableViewAccessibility")
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
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
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
            scaleGestureDetector = new ScaleGestureDetector(this,
                    new ScaleGestureDetector.OnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    currentZoomRatio *= detector.getScaleFactor();
                    currentZoomRatio = Math.max(1f, Math.min(currentZoomRatio, maxZoomRatio));
                    camera.getCameraControl().setZoomRatio(currentZoomRatio);
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {

                }
            });
            camera_preview.setOnTouchListener((view, event) -> {
                if (event.getPointerCount() > 1) {
                    scaleGestureDetector.onTouchEvent(event);
                    return true;
                }
                return false;
            });

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
        String myFolder = "AppImages";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        String folderName =Environment.getExternalStorageDirectory()+
        "/AppImages/" + name;
        File newFolder = new File(folderName);
        if (!newFolder.getParentFile().exists()) {
            newFolder.getParentFile().mkdir();
        }


        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(this.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(newFolder).build(),
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        picUri = outputFileResults.getSavedUri();
                        String imagePath = picUri.getPath();

                        String imageDate = String.valueOf(System.currentTimeMillis());
                        if (picUri != null){
                            ContentResolver contentResolver = getContentResolver();



                            CropImage.ActivityBuilder activityBuilder
                                    = CropImage.activity(picUri);
                            activityBuilder.setGuidelines(CropImageView.Guidelines.ON);
                            activityBuilder.setMultiTouchEnabled(true);
                            activityBuilder.start(CameraActivity.this);


                        }else {
                            camera_background.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {

                    }
                }
        );

      /*  imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                Uri picUri = outputFileResults.getSavedUri();
                String imagePath = picUri.getPath();
                String imageDate = String.valueOf(System.currentTimeMillis());
                if (picUri != null){
                    ContentResolver contentResolver = getContentResolver();

                    CropImage.ActivityBuilder cropImageBuilder = CropImage.activity(picUri);
                    cropImageBuilder.setGuidelines(CropImageView.Guidelines.ON);
                    cropImageBuilder.setMultiTouchEnabled(true);
                    cropImageBuilder.start(CameraActivity.this);

                }else {
                    camera_background.setVisibility(View.GONE);
                }



            }
                    @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraActivity.this,
                        exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Bitmap cropImage = null;
                try {
                    cropImage = MediaStore.Images.Media.getBitmap(getContentResolver(),resultUri);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                if (cropImage != null){
                    Calendar calendar = Calendar.getInstance();
                    String dateFormat = DateFormat.format("yyyymmdd_hhmmss", calendar).toString();
                    String name = "IMG_"+dateFormat;
                    String folderName = folder+name;
                    File folder = new File(folderName);
                    if (!folder.getParentFile().exists()) {
                        folder.getParentFile().mkdir();
                    }
                    Bitmap rotateImage = rotatedBitmap(cropImage, deviceOrientation);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    rotateImage.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                    byte[] compressedData = outputStream.toByteArray();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(new File(folderName+".jpg"));
                        fos.write(compressedData);
                        fos.flush();
                        fos.close();

                        if (picUri != null) {
                            String path1 = picUri.getPath();
                            File imageFile = new File(path1);
                            if (imageFile.exists()) {
                                // Delete the image file from the file system
                                imageFile.delete();

                                // Delete the image file from the MediaStore database
                                ContentResolver contentResolver = getContentResolver();
                                int rowsDeleted = contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.Images.Media.DATA + "=?", new String[]{path1});
                                if (rowsDeleted == 0) {
                                    // Log an error if the image file was not deleted from the MediaStore database
                                    Log.e("TAG", "Failed to delete image file from MediaStore: " + path1);
                                } else {
                                    // Notify the MediaStore to update its database
                                    MediaScannerConnection.scanFile(CameraActivity.this, new String[]{path1}, null,
                                            (path, uri) -> {
                                                Log.d("TAG", "Image file deleted from MediaStore: " + path1);
                                            });
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                count = count + 1;
                imageView.setVisibility(View.VISIBLE);
                imageCount.setVisibility(View.VISIBLE);

                Glide.with(this).load(resultUri).into(imageView);
                imageCount.setText(String.valueOf(count));
                cameraAdapter.notifyDataSetChanged();
                Log.d("Orient", String.valueOf(deviceOrientation));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(CameraActivity.this, CameraFolderActivity.class));
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

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


    @Override
    public void onBackPressed() {
        startActivity(new Intent(CameraActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
    }
}