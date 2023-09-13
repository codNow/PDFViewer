package com.sasha.pdfviewer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;


import com.sasha.pdfviewer.R;

import com.sasha.pdfviewer.adapter.FullScreenImageAdapter;
import com.sasha.pdfviewer.adapter.ImageExtractAdapter;
import com.sasha.pdfviewer.folderList.ExtractedImagesFolderActivity;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.utils.ImageUtils;


import java.util.ArrayList;


public class ImageTransformActivity extends AppCompatActivity {


    private ViewPager2 viewPager;
    private ArrayList<ImageModel> imageModels;
    int position;
    private FullScreenImageAdapter fullScreenImageAdapter;
    private String folderName = "ExtractedImages";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_folder);


        viewPager = findViewById(R.id.photoView);

        Intent intent = getIntent();


        imageModels = intent.getParcelableArrayListExtra("imageModels");
        position = getIntent().getExtras().getInt("position");


        imageModels = ImageUtils.allImageFile(this, folderName);
        fullScreenImageAdapter = new FullScreenImageAdapter(this, imageModels );

        viewPager.setAdapter(fullScreenImageAdapter);
        viewPager.setCurrentItem(position);



    }

   @Override
   public void onBackPressed() {
       super.onBackPressed();
       startActivity(new Intent(ImageTransformActivity.this, ExtractedImagesFolderActivity.class));
   }
}