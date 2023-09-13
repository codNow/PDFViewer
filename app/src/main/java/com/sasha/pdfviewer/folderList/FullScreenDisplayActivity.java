package com.sasha.pdfviewer.folderList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.sasha.pdfviewer.R;
import com.sasha.pdfviewer.adapter.FullScreenImageAdapter;
import com.sasha.pdfviewer.model.ImageModel;
import com.sasha.pdfviewer.utils.ImageUtils;

import java.util.ArrayList;

public class FullScreenDisplayActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ArrayList<ImageModel> imageModels;
    int position;
    private FullScreenImageAdapter fullScreenImageAdapter;
    private String folderName = "ExtractedImages";
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_display);
        overridePendingTransition(R.anim.slide_in_left_bottom_corner, 0);

        viewPager = findViewById(R.id.viewPager);

        Intent intent = getIntent();

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


        imageModels = intent.getParcelableArrayListExtra("imageModels");
        position = getIntent().getExtras().getInt("initialPosition");


        imageModels = ImageUtils.allImageFile(this, folderName);
        fullScreenImageAdapter = new FullScreenImageAdapter(this, imageModels );

        viewPager.setAdapter(fullScreenImageAdapter);

        viewPager.setCurrentItem(position, false);
        viewPager.setPageTransformer(new MyPageTransformer());

    }

    public class MyPageTransformer implements ViewPager2.PageTransformer {

        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();
            int pageHeight = page.getHeight();

            if (position < -1) {  // Off-screen to the left
                page.setAlpha(0);
            } else if (position <= 1) {  // On-screen
                // Modify the scale and alpha based on the position
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float alphaFactor = Math.max(MIN_ALPHA, 1 - Math.abs(position));

                // Apply scale and alpha transformations
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setAlpha(alphaFactor);


                // You can add more animation effects here based on the 'position' value
            } else {  // Off-screen to the right
                page.setAlpha(0);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // inside on touch event method we are calling on
        // touch event method and passing our motion event to it.
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // on below line we are creating a class for our scale
        // listener and  extending it with gesture listener.
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            // inside on scale method we are setting scale
            // for our image in our image view.
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            // on below line we are setting
            // scale x and scale y to our image view.
          /*  viewPager.setScaleX(mScaleFactor);
            viewPager.setScaleY(mScaleFactor);*/
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(FullScreenDisplayActivity.this, ExtractedImagesFolderActivity.class));
        overridePendingTransition(0,0);
    }
}