package com.sasha.pdfviewer.folderList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.ImageAdapter;
import com.sasha.pdfviewer.adapter.RecentAdapter;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.model.PdfModel;
import com.sasha.pdfviewer.model.RecentModel;
import com.sasha.pdfviewer.view.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExtractedImagesFolderActivity extends AppCompatActivity {

    private ArrayList<ImageModel> imageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageAdapter imageAdapter;
    private Toolbar toolbar;
    private String folderName = "ExtractedImages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extracted_images_folder);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressbar);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("Extracted Images");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleTextAppearance);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.VISIBLE);
        new CountDownTimer(1500, 1500){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                displayImages();
                progressBar.setVisibility(View.GONE);
            }
        }.start();
    }

    private void displayImages(){
        imageList = getImagesFromFolder(folderName);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(imageAdapter);

    }

    private ArrayList<ImageModel> getImagesFromFolder(String folderName) {
        ArrayList<ImageModel> imageDataList = new ArrayList<>();

        // Get the content resolver
        ContentResolver resolver = getContentResolver();

        // Get all the images from the external storage
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%" + folderName + "%"};
        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String imageDate = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                ImageModel imageData = new ImageModel(imageUri, imagePath, imageDate);
                imageDataList.add(imageData);
            }
            cursor.close();
        }

        return imageDataList;
    }
}